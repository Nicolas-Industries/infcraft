
package net.opencraft.client.entity;

import net.opencraft.client.OpenCraft;
import net.opencraft.client.Session;
import net.opencraft.client.input.MovementInput;
import net.opencraft.core.inventory.IInventory;
import net.opencraft.client.world.ClientWorld;
import net.opencraft.core.entity.Entity;
import net.opencraft.core.entity.EntityLiving;
import net.opencraft.core.entity.EntityPickupFX;
import net.opencraft.core.item.ItemStack;
import net.opencraft.core.nbt.NBTBase;
import net.opencraft.core.nbt.NBTTagCompound;
import net.opencraft.core.nbt.NBTTagList;
import net.opencraft.client.renderer.gui.*;
import net.opencraft.core.entity.EntityPlayer;
import net.opencraft.core.tileentity.TileEntityFurnace;
import net.opencraft.core.tileentity.TileEntitySign;
import net.opencraft.core.world.World;

public class EntityPlayerSP extends EntityPlayer {

    public MovementInput movementInput;
    private OpenCraft mc;

    // Smooth interpolation for server reconciliation
    private double targetX, targetY, targetZ;
    private float targetYaw, targetPitch;

    // Flag to prevent sending position updates before server has spawned us
    public boolean hasSpawnedInWorld = false;
    private boolean isInterpolating = false;
    private static final double INTERPOLATION_FACTOR = 0.3; // 30% per frame
    private static final double INTERPOLATION_THRESHOLD = 0.01; // Stop when this close

    public EntityPlayerSP(final OpenCraft aw, final ClientWorld fe, final Session gg) {
        super(fe);
        this.mc = aw;
        if (fe != null) {
            if (fe.player != null) {
                fe.setEntityDead(fe.player);
            }
            fe.player = this;
        }
        if (gg != null && gg.username != null && gg.username.length() > 0) {
            this.skinUrl = "http://www.minecraft.net/skin/" + gg.username + ".png";
        }
        this.username = gg.username;
    }

    public void updatePlayerActionState() {
        movementInput.updatePlayerMoveState();
        this.moveStrafing = this.movementInput.moveStrafe;
        this.moveForward = this.movementInput.moveForward;
        this.isJumping = this.movementInput.jump;
    }

    @Override
    public void preparePlayerToSpawn() {
        this.yOffset = 1.62f;
        this.setSize(0.6f, 1.8f);

        super.preparePlayerToSpawn();

        this.health = 20;
        this.deathTime = 0;
        this.hasSpawnedInWorld = false; // Reset spawn flag when respawning or switching worlds
    }

    @Override
    public void onLivingUpdate() {
        // Prevent falling into void if chunk is not loaded
        int chunkX = net.opencraft.core.util.Mth.floor_double(posX) >> 4;
        int chunkZ = net.opencraft.core.util.Mth.floor_double(posZ) >> 4;
        if (world != null && world instanceof ClientWorld) {
            net.opencraft.client.world.chunk.Chunk chunk = ((ClientWorld) world).getChunkFromChunkCoords(chunkX,
                    chunkZ);
            if (chunk != null && !chunk.isChunkLoaded) {
                // Freeze player until chunk loads
                System.out.println("CLIENT: Chunk not loaded at (" + chunkX + ", " + chunkZ
                        + "), freezing player. hasSpawnedInWorld=" + hasSpawnedInWorld);
                motionX = 0;
                motionY = 0;
                motionZ = 0;
                return;
            }
        }

        // Debug: Log player state every 2 seconds
        if (ticksExisted % 40 == 0) {
            System.out.println("CLIENT: Player state - pos: (" + String.format("%.2f", posX) + ", " +
                    String.format("%.2f", posY) + ", " + String.format("%.2f", posZ) +
                    "), motion: (" + String.format("%.3f", motionX) + ", " +
                    String.format("%.3f", motionY) + ", " + String.format("%.3f", motionZ) +
                    "), hasSpawned: " + hasSpawnedInWorld + ", onGround: " + onGround);
        }

        this.movementInput.updatePlayerMoveState();
        super.onLivingUpdate();

        // Apply smooth interpolation if reconciling with server
        if (isInterpolating) {
            applyInterpolation();
        } else if (hasSpawnedInWorld) {
            // Only send position updates after we've been spawned by the server
            // This prevents spamming position packets from 0,0,0 before server teleports us
            if (this.posX != this.prevPosX || this.posY != this.prevPosY || this.posZ != this.prevPosZ
                    || this.rotationYaw != this.prevRotationYaw || this.rotationPitch != this.prevRotationPitch) {
                try {
                    this.mc.getClientNetworkManager()
                            .sendPacket(new net.opencraft.shared.network.packets.PacketPlayerPosition(
                                    this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch,
                                    this.onGround));
                } catch (Exception e) {
                    System.err.println("CLIENT: Error sending position packet:");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Set target position for smooth interpolation (server reconciliation)
     */
    public void setPositionSmooth(double x, double y, double z, float yaw, float pitch) {
        targetX = x;
        targetY = y;
        targetZ = z;
        targetYaw = yaw;
        targetPitch = pitch;
        isInterpolating = true;
    }

    /**
     * Apply smooth interpolation towards server position
     */
    private void applyInterpolation() {
        // Interpolate position
        posX += (targetX - posX) * INTERPOLATION_FACTOR;
        posY += (targetY - posY) * INTERPOLATION_FACTOR;
        posZ += (targetZ - posZ) * INTERPOLATION_FACTOR;

        // Rotation interpolation removed to prevent fighting with client mouse input
        // The local player's rotation should be authoritative on the client side
        // rotationYaw += (targetYaw - rotationYaw) * INTERPOLATION_FACTOR;
        // rotationPitch += (targetPitch - rotationPitch) * INTERPOLATION_FACTOR;

        // Check if close enough to stop interpolating
        double distSq = (targetX - posX) * (targetX - posX) +
                (targetY - posY) * (targetY - posY) +
                (targetZ - posZ) * (targetZ - posZ);

        if (distSq < INTERPOLATION_THRESHOLD * INTERPOLATION_THRESHOLD) {
            // Snap to exact position
            posX = targetX;
            posY = targetY;
            posZ = targetZ;
            rotationYaw = targetYaw;
            rotationPitch = targetPitch;
            isInterpolating = false;
        }
    }

    @Override
    public void writeEntityToNBT(final NBTTagCompound nbtTagCompound) {
        super.writeEntityToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("Score", this.score);
        nbtTagCompound.setTag("Inventory", (NBTBase) this.inventory.writeToNBT(new NBTTagList()));
    }

    @Override
    public void readEntityFromNBT(final NBTTagCompound nbtTagCompound) {
        super.readEntityFromNBT(nbtTagCompound);
        this.score = nbtTagCompound.getInteger("Score");
        this.inventory.readFromNBT(nbtTagCompound.getTagList("Inventory"));
    }

    @Override
    public void displayGUIChest(final IInventory kd) {
        this.mc.displayGuiScreen(new GuiChest(this.inventory, kd));
    }

    @Override
    public void displayGUIEditSign(final TileEntitySign jn) {
        this.mc.displayGuiScreen(new GuiEditSign(jn));
    }

    @Override
    public void displayWorkbenchGUI() {
        this.mc.displayGuiScreen(new GuiCrafting(this.inventory));
    }

    @Override
    public void displayGUIFurnace(final TileEntityFurnace el) {
        this.mc.displayGuiScreen(new GuiFurnace(this.inventory, el));
    }

    public ItemStack getCurrentEquippedItem() {
        return this.inventory.getCurrentItem();
    }

    public void displayGUIInventory() {
        this.inventory.setInventorySlotContents(this.inventory.currentItem, null);
    }

    public void a(final Entity eq) {
        final int damageVsEntity = this.inventory.getDamageVsEntity(eq);
        if (damageVsEntity > 0) {
            eq.attackEntityFrom(this, damageVsEntity);
            final ItemStack currentEquippedItem = this.getCurrentEquippedItem();
            if (currentEquippedItem != null && eq instanceof EntityLiving) {
                currentEquippedItem.hitEntity((EntityLiving) eq);
                if (currentEquippedItem.stackSize <= 0) {
                    currentEquippedItem.onItemDestroyedByUse(this);
                    this.displayGUIInventory();
                }
            }
        }
    }

    @Override
    public void onItemPickup(final Entity eq) {
        // this.mc.effectRenderer.addEffect(new EntityPickupFX(this.mc.clientWorld, eq,
        // this, -0.5f));
    }

    public int getPlayerArmorValue() {
        return this.inventory.getTotalArmorValue();
    }

    @Override
    public void c(final Entity eq) {
        if (eq.interact(this)) {
            return;
        }
        final ItemStack currentEquippedItem = this.getCurrentEquippedItem();
        if (currentEquippedItem != null && eq instanceof EntityLiving) {
            currentEquippedItem.useItemOnEntity((EntityLiving) eq);
            if (currentEquippedItem.stackSize <= 0) {
                currentEquippedItem.onItemDestroyedByUse(this);
                this.displayGUIInventory();
            }
        }
    }
}
