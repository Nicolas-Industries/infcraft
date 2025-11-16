
package net.opencraft.core.entity;

import net.opencraft.core.blocks.Block;
import net.opencraft.core.world.World;

public class EntityDiggingFX extends EntityFX {

    public EntityDiggingFX(final World fe, final double double2, final double double3, final double double4, final double double5, final double double6, final double double7, final Block gs) {
        super(fe, double2, double3, double4, double5, double6, double7);
        this.particleTextureIndex = gs.blockIndexInTexture;
        this.particleGravity = gs.blockParticleGravity;
        final float particleRed = 0.6f;
        this.particleBlue = particleRed;
        this.particleGreen = particleRed;
        this.particleRed = particleRed;
        this.particleScale /= 2.0f;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

}
