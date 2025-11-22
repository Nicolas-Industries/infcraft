
package net.infcraft.core.entity;

import net.infcraft.server.world.ServerWorld;

public class EntityGiant extends EntityMonster {

    public EntityGiant(final ServerWorld serverWorld) {
        super(serverWorld);
        this.texture = "/assets/mob/zombie.png";
        this.moveSpeed = 0.5f;
        this.attackStrength = 50;
        this.health *= 10;
        this.yOffset *= 6.0f;
        this.setSize(this.width * 6.0f, this.height * 6.0f);
    }

    @Override
    protected float getBlockPathWeight(final int xCoord, final int yCoord, final int zCoord) {
        return this.world.getLightBrightness(xCoord, yCoord, zCoord) - 0.5f;
    }
}
