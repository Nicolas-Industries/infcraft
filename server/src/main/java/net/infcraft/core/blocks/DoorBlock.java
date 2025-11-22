
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;
import net.infcraft.core.input.MovingObjectPosition;
import net.infcraft.core.entity.EntityPlayer;
import net.infcraft.core.item.Item;
import net.infcraft.core.physics.AABB;
import net.infcraft.core.util.Vec3;
import net.infcraft.core.world.IBlockAccess;
import net.infcraft.core.world.World;
import net.infcraft.server.world.ServerWorld;

import java.util.Random;

import static org.joml.Math.*;

public class DoorBlock extends Block {

    protected DoorBlock(final int blockid) {
        super(blockid, Material.WOOD);
        this.blockIndexInTexture = 97;
        final float n = 0.5f;
        this.setShape(0.5f - n, 0.0f, 0.5f - n, 0.5f + n, 1.0f, 0.5f + n);
    }

    @Override
    public int getBlockTextureFromSideAndMetadata(final int textureIndexSlot, final int metadataValue) {
        if (textureIndexSlot == 0 || textureIndexSlot == 1) {
            return this.blockIndexInTexture;
        }
        final int state = this.getState(metadataValue);
        if ((state == 0 || state == 2) ^ textureIndexSlot <= 3) {
            return this.blockIndexInTexture;
        }
        int n = state / 2 + ((textureIndexSlot & 0x1) ^ state);
        n += (metadataValue & 0x4) / 4;
        int n2 = this.blockIndexInTexture - (metadataValue & 0x8) * 2;
        if ((n & 0x1) != 0x0) {
            n2 = -n2;
        }
        return n2;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 7;
    }

    @Override
    public AABB getSelectedBoundingBoxFromPool(World world, final int xCoord, final int yCoord, final int zCoord) {
        this.setBlockBoundsBasedOnState(world, xCoord, yCoord, zCoord);
        return super.getSelectedBoundingBoxFromPool(world, xCoord, yCoord, zCoord);
    }

    @Override
    public AABB getCollisionBoundingBoxFromPool(final World world, final int xCoord, final int yCoord, final int zCoord) {
        this.setBlockBoundsBasedOnState(world, xCoord, yCoord, zCoord);
        return super.getCollisionBoundingBoxFromPool(world, xCoord, yCoord, zCoord);
    }

    @Override
    public void setBlockBoundsBasedOnState(final IBlockAccess blockAccess, final int xCoord, final int yCoord, final int zCoord) {
        this.setDoorRotation(this.getState(blockAccess.getBlockMetadata(xCoord, yCoord, zCoord)));
    }

    public void setDoorRotation(final int metadataValue) {
        final float n = 0.1875f;
        this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f);
        if (metadataValue == 0) {
            this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, n);
        }
        if (metadataValue == 1) {
            this.setShape(1.0f - n, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
        if (metadataValue == 2) {
            this.setShape(0.0f, 0.0f, 1.0f - n, 1.0f, 1.0f, 1.0f);
        }
        if (metadataValue == 3) {
            this.setShape(0.0f, 0.0f, 0.0f, n, 1.0f, 1.0f);
        }
    }

    @Override
    public void onBlockClicked(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final EntityPlayer entityPlayer) {
        this.blockActivated(serverWorld, xCoord, yCoord, zCoord, entityPlayer);
    }

    @Override
    public boolean blockActivated(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final EntityPlayer entityPlayer) {
        final int blockMetadata = serverWorld.getBlockMetadata(xCoord, yCoord, zCoord);
        if ((blockMetadata & 0x8) != 0x0) {
            if (serverWorld.getBlockId(xCoord, yCoord - 1, zCoord) == this.blockID) {
                this.blockActivated(serverWorld, xCoord, yCoord - 1, zCoord, entityPlayer);
            }
            return true;
        }
        if (serverWorld.getBlockId(xCoord, yCoord + 1, zCoord) == this.blockID) {
            serverWorld.setBlockMetadataWithNotify(xCoord, yCoord + 1, zCoord, (blockMetadata ^ 0x4) + 8);
        }
        serverWorld.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, blockMetadata ^ 0x4);
        serverWorld.markBlocksDirty(xCoord, yCoord - 1, zCoord, xCoord, yCoord, zCoord);
        if (random() < 0.5) {
            serverWorld.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "random.door_open", 1.0f, serverWorld.rand.nextFloat() * 0.1f + 0.9f);
        } else {
            serverWorld.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "random.door_close", 1.0f, serverWorld.rand.nextFloat() * 0.1f + 0.9f);
        }
        return true;
    }

    @Override
    public void onNeighborBlockChange(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        final int blockMetadata = serverWorld.getBlockMetadata(xCoord, yCoord, zCoord);
        if ((blockMetadata & 0x8) != 0x0) {
            if (serverWorld.getBlockId(xCoord, yCoord - 1, zCoord) != this.blockID) {
                serverWorld.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
            }
        } else {
            boolean b = false;
            if (serverWorld.getBlockId(xCoord, yCoord + 1, zCoord) != this.blockID) {
                serverWorld.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
                b = true;
            }
            if (!serverWorld.isBlockNormalCube(xCoord, yCoord - 1, zCoord)) {
                serverWorld.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
                b = true;
                if (serverWorld.getBlockId(xCoord, yCoord + 1, zCoord) == this.blockID) {
                    serverWorld.setBlockWithNotify(xCoord, yCoord + 1, zCoord, 0);
                }
            }
            if (b) {
                this.dropBlockAsItem(serverWorld, xCoord, yCoord, zCoord, blockMetadata);
            }
        }
    }

    @Override
    public int idDropped(final int blockid, final Random random) {
        if ((blockid & 0x8) != 0x0) {
            return 0;
        }
        return Item.door.shiftedIndex;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, final int xCoord, final int yCoord, final int zCoord, final Vec3 var1, final Vec3 var2) {
        this.setBlockBoundsBasedOnState(world, xCoord, yCoord, zCoord);
        return super.collisionRayTrace(world, xCoord, yCoord, zCoord, var1, var2);
    }

    public int getState(final int state) {
        if ((state & 0x4) == 0x0) {
            return state - 1 & 0x3;
        }
        return state & 0x3;
    }

    @Override
    public boolean canPlaceBlockAt(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        return yCoord < 127 && serverWorld.isBlockNormalCube(xCoord, yCoord - 1, zCoord) && super.canPlaceBlockAt(serverWorld, xCoord, yCoord, zCoord) && super.canPlaceBlockAt(serverWorld, xCoord, yCoord + 1, zCoord);
    }
}
