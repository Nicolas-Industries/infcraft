
package net.infcraft.core.entity;

import net.infcraft.core.item.Item;
import net.infcraft.core.util.Mth;
import net.infcraft.server.world.ServerWorld;

public class EntityZombie extends EntityMonster {

    public EntityZombie(final ServerWorld serverWorld) {
        super(serverWorld);
        this.texture = "/assets/mob/zombie.png";
        this.moveSpeed = 0.5f;
        this.attackStrength = 5;
    }

    @Override
    public void onLivingUpdate() {
        if (this.world.isDaytime()) {
            final float entityBrightness = this.getEntityBrightness(1.0f);
            if (entityBrightness > 0.5f && this.world.canBlockSeeTheSky(Mth.floor_double(this.posX), Mth.floor_double(this.posY), Mth.floor_double(this.posZ)) && this.rand.nextFloat() * 30.0f < (entityBrightness - 0.4f) * 2.0f) {
                this.fire = 300;
            }
        }
        super.onLivingUpdate();
    }

    @Override
    protected int scoreValue() {
        return Item.feather.shiftedIndex;
    }
}
