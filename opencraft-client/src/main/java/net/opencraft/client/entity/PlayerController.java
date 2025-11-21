
package net.opencraft.client.entity;

import net.opencraft.client.OpenCraft;
import net.opencraft.client.world.ClientWorld;
import net.opencraft.core.blocks.Block;
import net.opencraft.core.entity.EntityPlayer;

public class PlayerController {

    protected final OpenCraft mc;
    public boolean field_1064_b;

    // Progressive breaking state
    private int currentBlockX = -1;
    private int currentBlockY = -1;
    private int currentBlockZ = -1;
    private float curBlockDamageMP = 0.0f;
    private float prevBlockDamageMP = 0.0f;
    private float blockHitDelay = 0.0f;
    private boolean isHittingBlock = false;

    public PlayerController(final OpenCraft aw) {
        this.field_1064_b = false;
        this.mc = aw;
    }

    public void a() {
    }

    public void func_717_a(final ClientWorld fe) {
    }

    public void clickBlock(final int xCoord, final int yCoord, final int zCoord) {
        // Send start digging packet
        net.opencraft.shared.network.packets.PacketPlayerDigging packet = new net.opencraft.shared.network.packets.PacketPlayerDigging(
                0, xCoord, yCoord, zCoord, this.mc.objectMouseOver.sideHit);
        try {
            this.mc.getClientNetworkManager().sendPacket(packet);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        int blockId = this.mc.clientWorld.getBlockId(xCoord, yCoord, zCoord);
        if (blockId > 0 && this.curBlockDamageMP == 0.0f) {
            Block block = Block.blocksList[blockId];
            block.onBlockClicked(null, xCoord, yCoord, zCoord, this.mc.player);
        }

        if (blockId > 0 && Block.blocksList[blockId].blockStrength(this.mc.player) >= 1.0f) {
            this.sendBlockRemoved(xCoord, yCoord, zCoord);
        } else {
            this.isHittingBlock = true;
            this.currentBlockX = xCoord;
            this.currentBlockY = yCoord;
            this.currentBlockZ = zCoord;
            this.curBlockDamageMP = 0.0f;
            this.prevBlockDamageMP = 0.0f;
            this.blockHitDelay = 0.0f;
        }
    }

    public boolean sendBlockRemoved(final int xCoord, final int yCoord, final int zCoord) {
        // Send block broken packet
        net.opencraft.shared.network.packets.PacketPlayerDigging packet = new net.opencraft.shared.network.packets.PacketPlayerDigging(
                2, xCoord, yCoord, zCoord, this.mc.objectMouseOver.sideHit);
        try {
            this.mc.getClientNetworkManager().sendPacket(packet);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        this.mc.effectRenderer.addBlockDestroyEffects(xCoord, yCoord, zCoord);
        final ClientWorld theWorld = this.mc.clientWorld;
        final Block block = Block.blocksList[theWorld.getBlockId(xCoord, yCoord, zCoord)];
        final int blockMetadata = theWorld.getBlockMetadata(xCoord, yCoord, zCoord);
        final boolean setBlockWithNotify = theWorld.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
        if (block != null && setBlockWithNotify) {
            this.mc.sndManager.playSound(block.digSound.digSoundDir(), xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f,
                    (block.stepSound.soundVolume() + 1.0f) / 2.0f, block.stepSound.soundPitch() * 0.8f);
            // FIXME: cant call this because it requires server context
            // block.onBlockDestroyedByPlayer(theWorld, xCoord, yCoord, zCoord,
            // blockMetadata);
        }
        return setBlockWithNotify;
    }

    public void sendBlockRemoving(final int xCoord, final int yCoord, final int zCoord, final int side) {
        if (!this.isHittingBlock || xCoord != this.currentBlockX || yCoord != this.currentBlockY
                || zCoord != this.currentBlockZ) {
            this.clickBlock(xCoord, yCoord, zCoord);
            return;
        }

        if (this.blockHitDelay > 0.0f) {
            this.blockHitDelay--;
            return;
        }

        int blockId = this.mc.clientWorld.getBlockId(xCoord, yCoord, zCoord);
        if (blockId == 0) {
            this.isHittingBlock = false;
            return;
        }

        Block block = Block.blocksList[blockId];
        this.curBlockDamageMP += block.blockStrength(this.mc.player);

        if (this.curBlockDamageMP >= 1.0f) {
            this.sendBlockRemoved(xCoord, yCoord, zCoord);
            this.curBlockDamageMP = 0.0f;
            this.prevBlockDamageMP = 0.0f;
            this.blockHitDelay = 5.0f;
        }
    }

    public void resetBlockRemoving() {
        this.curBlockDamageMP = 0.0f;
        this.isHittingBlock = false;
    }

    public void setPartialTime(final float float1) {
    }

    public float getBlockReachDistance() {
        return 5.0f;
    }

    public void flipPlayer(final EntityPlayer gi) {
    }

    public void updateController() {
        this.prevBlockDamageMP = this.curBlockDamageMP;
    }

    public boolean shouldDrawHUD() {
        return true;
    }

    public void func_6473_b(final EntityPlayer gi) {
    }
}
