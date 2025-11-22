
package net.infcraft.core.world;

import net.infcraft.core.world.chunk.Chunk;
import net.infcraft.server.world.ServerWorld;

public interface IChunkLoader {

    Chunk loadChunk(final ServerWorld fe, final int integer2, final int integer3);

    void saveChunk(final ServerWorld fe, final Chunk jw);

    void saveExtraChunkData(final ServerWorld fe, final Chunk jw);

    void chunkTick();

    void saveExtraData();
}
