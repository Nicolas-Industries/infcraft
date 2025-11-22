
package net.infcraft.client.world.chunk.storage;

import net.infcraft.client.world.IChunkLoader;
import net.infcraft.client.world.ClientWorld;
import net.infcraft.client.world.chunk.Chunk;

public class ChunkLoader implements IChunkLoader {

    public ChunkLoader() {}

    public Chunk loadChunk(final ClientWorld fe, final int x, final int z) {
        // TODO: load chunk from server
        return null;
    }

    public void saveChunk(final ClientWorld fe, final Chunk jw) {
        System.out.println("Attempt to save chunk " + jw.xPosition + ", " + jw.zPosition + " on client ignored");
    }

    public void chunkTick() {
    }

    public void saveExtraData() {
    }

    public void saveExtraChunkData(final ClientWorld fe, final Chunk jw) {
    }
}
