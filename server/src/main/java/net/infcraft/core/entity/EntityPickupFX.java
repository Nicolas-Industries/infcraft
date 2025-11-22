
package net.infcraft.core.entity;

import net.infcraft.core.world.World;

public class EntityPickupFX extends EntityFX {

    private Entity entityToPickUp;
    private EntityLiving entityPickingUp;
    private int age = 0;
    private int maxAge = 0;
    private float yOffs;

    public EntityPickupFX(World fe2, Entity eq2, EntityLiving ka2, float f) {
        super(fe2, eq2.posX, eq2.posY, eq2.posZ, eq2.motionX, eq2.motionY, eq2.motionZ);
        this.entityToPickUp = eq2;
        this.entityPickingUp = ka2;
        this.maxAge = 3;
        this.yOffs = f;
    }

    public void onUpdate() {
        ++this.age;
        if (this.age == this.maxAge) {
            this.setEntityDead();
        }
    }

    public int getFXLayer() {
        return 2;
    }
}
