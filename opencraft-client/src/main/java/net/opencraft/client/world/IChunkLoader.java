
package net.opencraft.client.world;

import net.opencraft.client.world.chunk.Chunk;

public interface IChunkLoader {

    Chunk loadChunk(final ClientWorld fe, final int integer2, final int integer3);

    void saveChunk(final ClientWorld fe, final Chunk jw);

    void saveExtraChunkData(final ClientWorld fe, final Chunk jw);

    void chunkTick();

    void saveExtraData();
}
