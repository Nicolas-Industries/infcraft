package net.infcraft.client.world.chunk;

import net.infcraft.client.world.ClientWorld;
import net.infcraft.core.entity.Entity;
import net.infcraft.core.enums.EnumSkyBlock;

/**
 * Represents an empty, immutable chunk used when the client hasn't received
 * chunk data yet.
 * This prevents the creation of mutable placeholder chunks and ensures safe
 * "void" handling.
 */
public class EmptyChunk extends Chunk {

    public EmptyChunk(ClientWorld world, int x, int z) {
        super(world, x, z);
        this.neverSave = true;
        this.isChunkLoaded = false; // Crucial: Physics will freeze players in this chunk
    }

    @Override
    public int getBlockID(int x, int y, int z) {
        return 0; // Always return Air
    }

    @Override
    public int getSavedLightValue(EnumSkyBlock type, int x, int y, int z) {
        return type.defaultLightValue; // Return default light (usually dark for block, bright for sky)
    }

    @Override
    public int getBlockLightValue(int x, int y, int z, int skylightSubtracted) {
        return 0;
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        return 0; // Empty chunks have no metadata
    }

    @Override
    public void addEntity(Entity entity) {
        // Do nothing, entities shouldn't be added to empty chunks
    }

    @Override
    public void removeEntity(Entity entity) {
        // Do nothing
    }

    @Override
    public void removeEntityAtIndex(Entity entity, int index) {
        // Do nothing
    }

    @Override
    public boolean canBlockSeeTheSky(int x, int y, int z) {
        return false;
    }

    @Override
    public void onChunkLoad() {
        // Do nothing
    }

    @Override
    public void onChunkUnload() {
        // Do nothing
    }

    @Override
    public void setChunkModified() {
        // Do nothing
    }
}
