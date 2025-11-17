
package net.opencraft.core.entity;

import net.opencraft.core.nbt.NBTTagCompound;
import net.opencraft.core.util.Mth;
import net.opencraft.server.world.ServerWorld;

public class EntityMonster extends EntityCreature {

    protected int attackStrength;

    public EntityMonster(final ServerWorld serverWorld) {
        super(serverWorld);
        this.attackStrength = 2;
        this.health = 20;
    }

    @Override
    public void onLivingUpdate() {
        if (this.getEntityBrightness(1.0f) > 0.5f) {
            this.entityAge += 2;
        }
        super.onLivingUpdate();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (((ServerWorld)this.world).difficultySetting == 0) {
            this.setEntityDead();
        }
    }

    @Override
    protected Entity findPlayerToAttack() {
        // TODO: implement monster AI for multiple players
//        final double distanceSqToEntity = this.world.player.getDistanceSqToEntity(this);
//        final double n = 16.0;
//        if (distanceSqToEntity < n * n && this.canEntityBeSeen(this.world.player)) {
//            return ((ServerWorld)this.world).player;
//        }
        return null;
    }

    @Override
    public boolean attackEntityFrom(final Entity entity, final int nya1) {
        if (super.attackEntityFrom(entity, nya1)) {
            if (entity != this) {
                this.playerToAttack = entity;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void attackEntity(final Entity entity, final float xCoord) {
        if (xCoord < 2.5 && entity.boundingBox.maxY > this.boundingBox.minY && entity.boundingBox.minY < this.boundingBox.maxY) {
            this.attackTime = 20;
            entity.attackEntityFrom(this, this.attackStrength);
        }
    }

    @Override
    protected float getBlockPathWeight(final int xCoord, final int yCoord, final int zCoord) {
        return 0.5f - this.world.getLightBrightness(xCoord, yCoord, zCoord);
    }

    @Override
    public void writeEntityToNBT(final NBTTagCompound nbtTagCompound) {
        super.writeEntityToNBT(nbtTagCompound);
    }

    @Override
    public void readEntityFromNBT(final NBTTagCompound nbtTagCompound) {
        super.readEntityFromNBT(nbtTagCompound);
    }

    @Override
    public boolean getCanSpawnHere(final double nya1, final double nya2, final double nya3) {
        return this.world.getBlockLightValue(Mth.floor_double(nya1), Mth.floor_double(nya2), Mth.floor_double(nya3)) <= this.rand.nextInt(8) && super.getCanSpawnHere(nya1, nya2, nya3);
    }
}
