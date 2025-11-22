
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;
import net.infcraft.server.world.ServerWorld;

public class SpongeBlock extends Block {

    protected SpongeBlock(final int integer) {
        super(integer, Material.SPONGE);
        this.blockIndexInTexture = 48;
    }

    @Override
    public void onBlockAdded(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        for (int n = 2, i = xCoord - n; i <= xCoord + n; ++i) {
            for (int j = yCoord - n; j <= yCoord + n; ++j) {
                for (int k = zCoord - n; k <= zCoord + n; ++k) {
                    if (serverWorld.getBlockMaterial(i, j, k) == Material.WATER) {
                    }
                }
            }
        }
    }

    @Override
    public void onBlockRemoval(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        for (int n = 2, i = xCoord - n; i <= xCoord + n; ++i) {
            for (int j = yCoord - n; j <= yCoord + n; ++j) {
                for (int k = zCoord - n; k <= zCoord + n; ++k) {
                    serverWorld.notifyBlocksOfNeighborChange(i, j, k, serverWorld.getBlockId(i, j, k));
                }
            }
        }
    }
}
