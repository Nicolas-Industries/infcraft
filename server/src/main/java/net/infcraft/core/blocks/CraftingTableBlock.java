
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;
import net.infcraft.core.entity.EntityPlayer;
import net.infcraft.server.world.ServerWorld;

public class CraftingTableBlock extends Block {

    protected CraftingTableBlock(final int blockid) {
        super(blockid, Material.WOOD);
        this.blockIndexInTexture = 59;
    }

    @Override
    public int getBlockTextureFromSide(final int textureIndexSlot) {
        if (textureIndexSlot == 1) {
            return this.blockIndexInTexture - 16;
        }
        if (textureIndexSlot == 0) {
            return Block.planks.getBlockTextureFromSide(0);
        }
        if (textureIndexSlot == 2 || textureIndexSlot == 4) {
            return this.blockIndexInTexture + 1;
        }
        return this.blockIndexInTexture;
    }

    @Override
    public boolean blockActivated(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final EntityPlayer entityPlayer) {
        entityPlayer.displayWorkbenchGUI();
        return true;
    }
}
