
package net.opencraft.client.world;

import net.opencraft.client.world.chunk.Chunk;

public interface IChunkLoader {

    Chunk loadChunk(final World fe, final int integer2, final int integer3);

    void saveChunk(final World fe, final Chunk jw);

    void saveExtraChunkData(final World fe, final Chunk jw);

    void chunkTick();

    void saveExtraData();
}
