
package net.opencraft.core.blocks.material;

import net.opencraft.core.blocks.material.Material;

public class MaterialTransparent extends Material {

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean isBlockGrass() {
        return false;
    }

    @Override
    public boolean getIsSolid() {
        return false;
    }
}
