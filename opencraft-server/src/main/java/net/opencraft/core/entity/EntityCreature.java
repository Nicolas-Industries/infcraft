
package net.opencraft.core.entity;

import net.opencraft.core.pathfinder.PathEntity;
import net.opencraft.core.util.Mth;
import net.opencraft.core.util.Vec3;
import net.opencraft.server.world.ServerWorld;

import static org.joml.Math.*;

public class EntityCreature extends EntityLiving {

    private PathEntity pathToEntity;
    protected Entity playerToAttack;
    protected boolean hasAttacked;

    public EntityCreature(final ServerWorld serverWorld) {
        super(serverWorld);
        this.hasAttacked = false;
    }

    protected boolean canEntityBeSeen(final Entity entity) {
        return this.world.rayTraceBlocks(Vec3.newTemp(this.posX, this.posY + this.getEyeHeight(), this.posZ), Vec3.newTemp(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ)) == null;
    }

    @Override
    protected void updatePlayerActionState() {
        this.hasAttacked = false;
        final float float5 = 16.0f;
        if (this.playerToAttack == null) {
            this.playerToAttack = this.findPlayerToAttack();
            if (this.playerToAttack != null) {
                this.pathToEntity = this.world.getPathToEntity(this, this.playerToAttack, float5);
            }
        } else if (!this.playerToAttack.isEntityAlive()) {
            this.playerToAttack = null;
        } else {
            final float distanceToEntity = this.playerToAttack.getDistanceToEntity(this);
            if (this.canEntityBeSeen(this.playerToAttack)) {
                this.attackEntity(this.playerToAttack, distanceToEntity);
            }
        }
        if (!this.hasAttacked && this.playerToAttack != null && (this.pathToEntity == null || this.rand.nextInt(20) == 0)) {
            this.pathToEntity = this.world.getPathToEntity(this, this.playerToAttack, float5);
        } else if (this.pathToEntity == null || this.pathToEntity.isFinished() || this.rand.nextInt(400) == 0) { // Reduced random pathfinding from 1% to 0.25%
            // Only set random paths for hostile mobs that don't have a target
            // Passive mobs (like animals) should not wander aimlessly as much
            if (this.shouldWander()) {
                int floor_double = -1;
                int yCoord = -1;
                int zCoord = -1;
                float n = -99999.0f;
                for (int i = 0; i < 15; ++i) { // Reduced search attempts
                    final int floor_double2 = Mth.floor_double(this.posX + this.rand.nextInt(13) - 6.0); // Smaller range: -6 to +6
                    final int floor_double3 = Mth.floor_double(this.posY + this.rand.nextInt(5) - 2.0); // Smaller Y range
                    final int floor_double4 = Mth.floor_double(this.posZ + this.rand.nextInt(13) - 6.0);
                    final float blockPathWeight = this.getBlockPathWeight(floor_double2, floor_double3, floor_double4);
                    if (blockPathWeight > n) {
                        n = blockPathWeight;
                        floor_double = floor_double2;
                        yCoord = floor_double3;
                        zCoord = floor_double4;
                    }
                }
                if (floor_double > 0) {
                    this.pathToEntity = this.world.getEntityPathToXYZ(this, floor_double, yCoord, zCoord, float5);
                }
            }
        }
        int floor_double = Mth.floor_double(this.boundingBox.minY);
        final boolean handleWaterMovement = this.handleWaterMovement();
        final boolean handleLavaMovement = this.handleLavaMovement();
        if (this.pathToEntity == null) {
            // When no path, only occasionally use default behavior, otherwise just handle basic movement
            if (this.rand.nextInt(200) == 0) {
                super.updatePlayerActionState();
            }
            // Continue with no path - no movement
        } else {
            Vec3 vec3D = this.pathToEntity.getPosition(this);
            final float n2 = this.width * 2.0f;
            while (vec3D != null && vec3D.distanceSquared(this.posX, this.posY, this.posZ) < n2 * n2 && vec3D.y <= floor_double) {
                this.pathToEntity.incrementPathIndex();
                if (this.pathToEntity.isFinished()) {
                    vec3D = null;
                    this.pathToEntity = null;
                } else {
                    vec3D = this.pathToEntity.getPosition(this);
                }
            }
            this.isJumping = false;
            if (vec3D != null) {
                final double n3 = vec3D.x - this.posX;
                final double n4 = vec3D.z - this.posZ;
                final double n5 = vec3D.y - floor_double;
                // atan2 returns radians, but rotationYaw is in degrees
                this.rotationYaw = (float) (toDegrees(atan2(n4, n3))) - 90.0f;
                this.moveForward = this.moveSpeed;
                if (this.hasAttacked && this.playerToAttack != null) {
                    // Add smarter combat movement based on mob type
                    if (this.shouldCombatStrafe()) {
                        final double n6 = this.playerToAttack.posX - this.posX;
                        final double n7 = this.playerToAttack.posZ - this.posZ;
                        final float rotationYaw = this.rotationYaw;
                        // atan2 returns radians, convert to degrees
                        this.rotationYaw = (float) (toDegrees(atan2(n7, n6))) - 90.0f;
                        // Ensure angle calculation is in degrees
                        final float n8 = toRadians(rotationYaw - this.rotationYaw + 90.0f);
                        this.moveStrafing = -sin(n8) * this.moveForward;
                        this.moveForward = cos(n8) * this.moveForward;
                    }
                }

                // Enhanced jumping detection - only jump when on ground and need to climb
                if (this.onGround && n5 > 0.0 && n5 <= 1.1) {
                    // Only set jump flag when actually on ground and need to climb up
                    this.isJumping = true;
                }
            }
        }
        if (this.rand.nextFloat() < 0.8f && (handleWaterMovement || handleLavaMovement)) {
            this.isJumping = true;
        }
    }

    protected void attackEntity(final Entity entity, final float xCoord) {
    }

    protected float getBlockPathWeight(final int xCoord, final int yCoord, final int zCoord) {
        return 0.0f;
    }

    protected Entity findPlayerToAttack() {
        return null;
    }

    // Method to determine if this creature should wander randomly
    // Override in subclasses to customize behavior
    protected boolean shouldWander() {
        // By default, all creatures can wander, but this can be overridden by passive mobs
        return true;
    }

    // Method to determine if this creature should strafe during combat
    // Passive animals should not strafe, aggressive mobs might
    protected boolean shouldCombatStrafe() {
        // Default to true for aggressive creatures, false for passive
        return !(this instanceof EntityAnimal);
    }

    @Override
    public boolean getCanSpawnHere(final double nya1, final double nya2, final double nya3) {
        return super.getCanSpawnHere(nya1, nya2, nya3) && this.getBlockPathWeight((int) nya1, (int) nya2, (int) nya3) >= 0.0f;
    }
}
