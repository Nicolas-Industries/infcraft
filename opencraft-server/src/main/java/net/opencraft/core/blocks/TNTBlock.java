
package net.opencraft.core.blocks;

import net.opencraft.core.blocks.material.Material;
import net.opencraft.core.entity.EntityTNTPrimed;
import net.opencraft.server.world.ServerWorld;

import java.util.Random;

public class TNTBlock extends Block {

    public TNTBlock(final int blockid, final int blockIndexInTexture) {
        super(blockid, blockIndexInTexture, Material.TNT);
    }

    @Override
    public int getBlockTextureFromSide(final int textureIndexSlot) {
        if (textureIndexSlot == 0) {
            return this.blockIndexInTexture + 2;
        }
        if (textureIndexSlot == 1) {
            return this.blockIndexInTexture + 1;
        }
        return this.blockIndexInTexture;
    }

    @Override
    public int quantityDropped(final Random random) {
        return 0;
    }

    @Override
    public void onBlockDestroyedByExplosion(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        final EntityTNTPrimed entity = new EntityTNTPrimed(serverWorld, xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f);
        entity.fuse = serverWorld.rand.nextInt(entity.fuse / 4) + entity.fuse / 8;
        serverWorld.entityJoinedWorld(entity);
    }

    @Override
    public void onBlockDestroyedByPlayer(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        final EntityTNTPrimed entityTNTPrimed = new EntityTNTPrimed(serverWorld, xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f);
        serverWorld.entityJoinedWorld(entityTNTPrimed);
        serverWorld.playSound(entityTNTPrimed, "random.fuse", 1.0f, 1.0f);
    }
}
