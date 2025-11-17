
package net.opencraft.core.world;

import net.opencraft.core.world.chunk.Chunk;
import net.opencraft.server.world.ServerWorld;

public interface IChunkLoader {

    Chunk loadChunk(final ServerWorld fe, final int integer2, final int integer3);

    void saveChunk(final ServerWorld fe, final Chunk jw);

    void saveExtraChunkData(final ServerWorld fe, final Chunk jw);

    void chunkTick();

    void saveExtraData();
}
