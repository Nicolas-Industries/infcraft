
package net.opencraft.client.world;

import net.opencraft.core.blocks.material.Material;
import net.opencraft.client.tileentity.TileEntity;

public interface IBlockAccess {

    int getBlockId(final int xCoord, final int yCoord, final int zCoord);

    TileEntity getBlockTileEntity(final int xCoord, final int yCoord, final int zCoord);

    float getLightBrightness(final int nya1, final int nya2, final int nya3);

    int getBlockMetadata(final int xCoord, final int yCoord, final int zCoord);

    Material getBlockMaterial(final int nya1, final int nya2, final int nya3);

    boolean isBlockNormalCube(final int xCoord, final int yCoord, final int zCoord);
}
