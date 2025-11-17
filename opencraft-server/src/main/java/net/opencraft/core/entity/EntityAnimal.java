
package net.opencraft.core.entity;

import net.opencraft.core.blocks.Block;
import net.opencraft.core.nbt.NBTTagCompound;
import net.opencraft.core.util.Mth;
import net.opencraft.server.world.ServerWorld;

public abstract class EntityAnimal extends EntityCreature {

    public EntityAnimal(final ServerWorld serverWorld) {
        super(serverWorld);
    }

    @Override
    protected boolean shouldWander() {
        // Passive animals should wander more frequently but consider their current location
        // If they're on a good spot (grass), they should be less likely to wander far
        int currentBlockBelow = this.world.getBlockId(
            (int) this.posX,
            (int) (this.posY - 1.0),
            (int) this.posZ
        );

        // If on grass, reduce wandering tendency; if not on grass, increase it
        if (currentBlockBelow == net.opencraft.core.blocks.Block.grass.blockID) {
            // On grass, 30% chance to wander (prefer staying in good spot)
            return this.rand.nextInt(10) < 3;
        } else {
            // Not on grass, 80% chance to wander (search for better spot)
            return this.rand.nextInt(10) < 8;
        }
    }

    @Override
    protected float getBlockPathWeight(final int xCoord, final int yCoord, final int zCoord) {
        if (this.world.getBlockId(xCoord, yCoord - 1, zCoord) == Block.grass.blockID) {
            return 10.0f;
        }
        return this.world.getLightBrightness(xCoord, yCoord, zCoord) - 0.5f;
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
        return this.world.getBlockLightValue(Mth.floor_double(nya1), Mth.floor_double(nya2), Mth.floor_double(nya3)) > 8 && super.getCanSpawnHere(nya1, nya2, nya3);
    }
}
