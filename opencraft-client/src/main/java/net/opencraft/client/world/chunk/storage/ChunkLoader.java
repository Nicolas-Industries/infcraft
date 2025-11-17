
package net.opencraft.client.world.chunk.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import net.opencraft.core.util.CompressedStreamTools;
import net.opencraft.core.entity.Entity;
import net.opencraft.core.nbt.NBTBase;
import net.opencraft.core.nbt.NBTTagCompound;
import net.opencraft.core.nbt.NBTTagList;
import net.opencraft.core.tileentity.TileEntity;
import net.opencraft.client.world.IChunkLoader;
import net.opencraft.client.world.ClientWorld;
import net.opencraft.client.world.chunk.Chunk;

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
