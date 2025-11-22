package net.infcraft.client.world.chunk;

import java.util.HashMap;
import java.util.Map;

import net.infcraft.client.renderer.gui.IProgressUpdate;
import net.infcraft.client.world.ClientWorld;
import net.infcraft.client.world.IChunkLoader;
import net.infcraft.client.world.IChunkProvider;

public class ChunkProviderClient implements IChunkProvider {

    private Map<Long, Chunk> chunkMap; // Use HashMap instead of fixed array
    private ClientWorld worldObj;
    private Chunk emptyChunk;

    public ChunkProviderClient(final ClientWorld fe, final IChunkLoader bg) {
        this.chunkMap = new HashMap<>();
        this.worldObj = fe;
        this.emptyChunk = new EmptyChunk(fe, 0, 0); // Shared empty chunk instance
    }

    /**
     * Convert chunk coordinates to a unique long key
     */
    private long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    public boolean chunkExists(final int chunkX, final int chunkZ) {
        long key = getChunkKey(chunkX, chunkZ);
        Chunk chunk = this.chunkMap.get(key);
        return chunk != null && chunk.isAtLocation(chunkX, chunkZ);
    }

    public Chunk provideChunk(final int chunkX, final int chunkZ) {
        long key = getChunkKey(chunkX, chunkZ);
        Chunk chunk = this.chunkMap.get(key);

        if (chunk == null) {
            // Return the shared empty chunk instead of creating a new one
            // This prevents memory spam and ensures consistent "void" behavior
            return this.emptyChunk;
        }

        return chunk;
    }

    public void populate(final IChunkProvider ch, final int integer2, final int integer3) {
    }

    public boolean saveChunks(final boolean boolean1, final IProgressUpdate jd) {
        return true;
    }

    public boolean unload100OldestChunks() {
        return false;
    }

    public boolean canSave() {
        return false;
    }

    /**
     * Load a chunk from packet data received from the server
     * This is the primary way chunks are loaded on the client
     */
    public void loadChunkFromPacket(int chunkX, int chunkZ, byte[] packetData) {
        // Decode the palette-encoded data from the packet
        byte[] decodedBlocks = decodeChunkData(packetData);

        long key = getChunkKey(chunkX, chunkZ);
        Chunk chunk = new Chunk(this.worldObj, decodedBlocks, chunkX, chunkZ);
        chunk.q = true;
        chunk.neverSave = true; // Client chunks should never save to disk
        chunk.isChunkLoaded = true; // Mark as loaded so entities know it's safe
        chunk.generateSkylightMap(); // Calculate height map and lighting
        this.chunkMap.put(key, chunk);

        // Notify world that chunk is loaded to update rendering
        this.worldObj.markBlocksDirty(chunkX * 16, 0, chunkZ * 16, chunkX * 16 + 16, 128, chunkZ * 16 + 16);
    }

    /**
     * Decode palette-encoded chunk data from packet format to raw block array
     * Packet format: sections with palette encoding
     * Output: 16x16x128 byte array (32768 bytes)
     */
    private byte[] decodeChunkData(byte[] packetData) {
        byte[] blocks = new byte[32768]; // 16x16x128

        try {
            net.infcraft.shared.network.PacketBuffer buffer = new net.infcraft.shared.network.PacketBuffer(
                    packetData);

            // Read 8 sections (each 16x16x16)
            for (int sectionIndex = 0; sectionIndex < 8; sectionIndex++) {
                int sectionOffset = sectionIndex * 4096; // Each section is 4096 blocks

                // Check if we have more data to read
                if (buffer.readableBytes() < 2) {
                    // No more sections, fill rest with air (0)
                    break;
                }

                // Read block count (short)
                int blockCount = buffer.readShort();

                // Read palette length (byte)
                int paletteLength = buffer.readByte() & 0xFF;

                if (paletteLength == 0) {
                    // Single block section (shouldn't happen with blockCount check, but handle it)
                    int blockId = buffer.readVarInt();
                    for (int i = 0; i < 4096; i++) {
                        blocks[sectionOffset + i] = (byte) blockId;
                    }
                } else if (paletteLength == 1) {
                    // Single block section
                    int blockId = buffer.readVarInt();
                    for (int i = 0; i < 4096; i++) {
                        blocks[sectionOffset + i] = (byte) blockId;
                    }
                } else {
                    // Palette + Data
                    int[] palette = new int[paletteLength];
                    for (int i = 0; i < paletteLength; i++) {
                        palette[i] = buffer.readVarInt();
                    }

                    // Read 4096 palette indices
                    for (int i = 0; i < 4096; i++) {
                        int paletteIndex = buffer.readByte() & 0xFF;
                        if (paletteIndex < palette.length) {
                            blocks[sectionOffset + i] = (byte) palette[paletteIndex];
                        } else {
                            blocks[sectionOffset + i] = 0; // Air if invalid index
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error decoding chunk data: " + e.getMessage());
            e.printStackTrace();
            // Return air-filled chunk on error
            return new byte[32768];
        }

        return blocks;
    }
}