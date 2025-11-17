
package net.opencraft.core.blocks.material;

import net.opencraft.core.blocks.material.Material;

public class MaterialLiquid extends Material {

    @Override
    public boolean isLiquid() {
        return true;
    }

    @Override
    public boolean isSolid() {
        return false;
    }
}
