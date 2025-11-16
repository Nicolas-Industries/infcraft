package net.opencraft;

import net.opencraft.blocks.material.Material;
import net.opencraft.entity.Entity;
import net.opencraft.entity.EntityLiving;
import net.opencraft.renderer.gui.IProgressUpdate;
import net.opencraft.util.Mth;
import net.opencraft.world.World;
import net.opencraft.world.chunk.ChunkPosition;

/**
 * Handles spawning of entities in the world.
 */
public class EntitySpawner {

    private final int maxEntityCount;
    private final Class entityClass;
    private final Class[] spawnableClasses;

    public EntitySpawner(final int maxEntityCount, final Class entityClass, final Class[] spawnableClasses) {
        this.maxEntityCount = maxEntityCount;
        this.entityClass = entityClass;
        this.spawnableClasses = spawnableClasses;
    }

    /**
     * Attempt to spawn entities if below the maximum count.
     */
    public void trySpawn(World world) {
        if (world.countEntities(this.entityClass) < this.maxEntityCount) {
            for (int i = 0; i < 10; ++i) {
                this.spawnEntities(world, 1, world.player, null);
            }
        }
    }

    /**
     * Pick a random spawn position near given coordinates.
     */
    protected ChunkPosition getRandomSpawnPosition(World world, int baseX, int baseZ) {
        return new ChunkPosition(
                baseX + world.rand.nextInt(256) - 128,
                world.rand.nextInt(128),
                baseZ + world.rand.nextInt(256) - 128
        );
    }

    /**
     * Try to spawn entities near a source entity.
     * @return number of entities successfully spawned
     */
    private int spawnEntities(World world, int count, Entity source, IProgressUpdate progress) {
        int spawnedCount = 0;

        int entityX = Mth.floor_double(source.posX);
        int entityZ = Mth.floor_double(source.posZ);

        int classIndex = world.rand.nextInt(this.spawnableClasses.length);
        ChunkPosition spawnPos = this.getRandomSpawnPosition(world, entityX, entityZ);

        int x = spawnPos.x;
        int y = spawnPos.y;
        int z = spawnPos.z;

        // Must be air and not a solid cube
        if (world.isBlockNormalCube(x, y, z)) return 0;
        if (world.getBlockMaterial(x, y, z) != Material.AIR) return 0;

        // Try multiple attempts around the chosen position
        for (int attempt = 0; attempt < 3; ++attempt) {
            int spawnX = x;
            int spawnY = y;
            int spawnZ = z;
            final int range = 6;

            for (int j = 0; j < 3; ++j) {
                spawnX += world.rand.nextInt(range) - world.rand.nextInt(range);
                spawnY += world.rand.nextInt(1) - world.rand.nextInt(1);
                spawnZ += world.rand.nextInt(range) - world.rand.nextInt(range);

                // Check block suitability
                if (world.isBlockNormalCube(spawnX, spawnY - 1, spawnZ)
                        && !world.isBlockNormalCube(spawnX, spawnY, spawnZ)
                        && !world.getBlockMaterial(spawnX, spawnY, spawnZ).isLiquid()
                        && !world.isBlockNormalCube(spawnX, spawnY + 1, spawnZ)) {

                    float posX = spawnX + 0.5f;
                    float posY = spawnY + 1.0f;
                    float posZ = spawnZ + 0.5f;

                    // Check distance from source or player
                    if (source != null) {
                        double dx = posX - source.posX;
                        double dy = posY - source.posY;
                        double dz = posZ - source.posZ;
                        if (dx * dx + dy * dy + dz * dz < 1024.0) continue;
                    } else {
                        float dxPlayer = posX - world.x;
                        float dyPlayer = posY - world.y;
                        float dzPlayer = posZ - world.z;
                        if (dxPlayer * dxPlayer + dyPlayer * dyPlayer + dzPlayer * dzPlayer < 1024.0f) continue;
                    }

                    // Instantiate entity
                    EntityLiving entity;
                    try {
                        entity = (EntityLiving) this.spawnableClasses[classIndex]
                                .getConstructor(new Class[]{World.class})
                                .newInstance(new Object[]{world});
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return spawnedCount;
                    }

                    entity.setPositionAndRotation(posX, posY, posZ, world.rand.nextFloat() * 360.0f, 0.0f);

                    // Spawn if valid
                    if (entity.getCanSpawnHere(posX, posY, posZ)) {
                        ++spawnedCount;
                        world.entityJoinedWorld(entity);
                    }
                }
            }
        }
        return spawnedCount;
    }
}
