package net.opencraft;

import net.opencraft.client.entity.PlayerControllerSP;
import net.opencraft.world.World;
import net.opencraft.world.chunk.ChunkPosition;

/**
 * Specialized spawner that uses the player's controller
 * and custom spawn position logic.
 */
public class PlayerEntitySpawner extends EntitySpawner {

    public final PlayerControllerSP playerController;

    public PlayerEntitySpawner(final PlayerControllerSP playerController,
                               final int maxEntityCount,
                               final Class entityClass,
                               final Class[] spawnableClasses) {
        super(maxEntityCount, entityClass, spawnableClasses);
        this.playerController = playerController;
    }

    /**
     * Override: choose a random spawn position with a more complex Y distribution.
     */
    @Override
    protected ChunkPosition getRandomSpawnPosition(final World world, final int baseX, final int baseZ) {
        return new ChunkPosition(
                baseX + world.rand.nextInt(256) - 128,
                world.rand.nextInt(world.rand.nextInt(world.rand.nextInt(112) + 8) + 8),
                baseZ + world.rand.nextInt(256) - 128
        );
    }
}
