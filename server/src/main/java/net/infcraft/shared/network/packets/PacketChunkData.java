package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;
import net.infcraft.core.world.chunk.Chunk;
import java.util.ArrayList;
import java.util.List;

/**
 * Packet to send chunk data from server to client
 */
public class PacketChunkData implements IPacket {

    private int chunkX;
    private int chunkZ;
    private int primaryBitMask;
    private byte[] data;

    // Default constructor for deserialization
    public PacketChunkData() {
    }

    public PacketChunkData(Chunk chunk) {
        this.chunkX = chunk.xPosition;
        this.chunkZ = chunk.zPosition;

        PacketBuffer dataBuffer = new PacketBuffer();
        int mask = 0;

        // Assuming 128 height, so 8 sections of 16 blocks high
        for (int i = 0; i < 8; i++) {
            // Check if section is empty (simplified check, in real impl we'd check if all
            // blocks are air)
            // For now, we send all sections if they exist in the byte array
            // Chunk data is 32768 bytes (16*16*128).
            // Section i corresponds to bytes i*4096 to (i+1)*4096

            if (chunk.blocks != null && chunk.blocks.length >= (i + 1) * 4096) {
                mask |= (1 << i);
                writeSection(dataBuffer, chunk.blocks, i * 4096);
            }
        }

        this.primaryBitMask = mask;
        this.data = dataBuffer.array();
    }

    private void writeSection(PacketBuffer buffer, byte[] blocks, int offset) {
        // Count non-air blocks
        int blockCount = 0;
        for (int j = 0; j < 4096; j++) {
            if (blocks[offset + j] != 0) {
                blockCount++;
            }
        }
        buffer.writeShort(blockCount);

        // Palette
        // For simplicity, we use a direct palette if we have many blocks, or single
        // block if uniform
        // But the spec says: Palette Length (u8). If 0 -> Single Block. If > 0 ->
        // Palette + Data.

        // Let's collect unique blocks
        List<Byte> palette = new ArrayList<>();
        for (int j = 0; j < 4096; j++) {
            byte b = blocks[offset + j];
            if (!palette.contains(b)) {
                palette.add(b);
            }
        }

        buffer.writeByte(palette.size());

        if (palette.size() == 0) {
            // Should not happen if 4096 blocks, unless all are air (0) and we didn't add 0?
            // If all air, palette size is 1 (0).
            // If we found no blocks, it means something is wrong or we handle it as single
            // block 0.
            buffer.writeVarInt(0);
        } else if (palette.size() == 1) {
            // Single block section
            buffer.writeVarInt(palette.get(0) & 0xFF);
        } else {
            // Palette + Data
            for (Byte b : palette) {
                buffer.writeVarInt(b & 0xFF);
            }

            // Data Array: 4096 indices into Palette
            // Currently spec says "4096 indices... simple u8 per block"
            // We map each block to its palette index
            for (int j = 0; j < 4096; j++) {
                byte b = blocks[offset + j];
                int index = palette.indexOf(b);
                buffer.writeByte(index);
            }
        }
    }

    @Override
    public void readPacketData(PacketBuffer buffer) {
        chunkX = buffer.readInt();
        chunkZ = buffer.readInt();
        primaryBitMask = buffer.readVarInt();
        int size = buffer.readVarInt();
        data = new byte[size];
        buffer.readBytes(data);
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        buffer.writeVarInt(primaryBitMask);
        buffer.writeVarInt(data.length);
        buffer.writeBytes(data);
    }

    @Override
    public int getPacketId() {
        return 0x20;
    }

    @Override
    public boolean isServerToClient() {
        return true;
    }

    @Override
    public boolean isClientToServer() {
        return false;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getPrimaryBitMask() {
        return primaryBitMask;
    }

    public byte[] getData() {
        return data;
    }
}