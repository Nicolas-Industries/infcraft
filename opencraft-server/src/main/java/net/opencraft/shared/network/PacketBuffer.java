package net.opencraft.shared.network;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * A buffer for reading and writing packet data
 * This class handles serialization and deserialization of packet data
 * adhering to the new protocol spec (Little-Endian, VarInts).
 */
public class PacketBuffer {

    private ByteBuffer buffer;

    public PacketBuffer() {
        this.buffer = ByteBuffer.allocate(1024 * 64); // Start with a reasonable size
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public PacketBuffer(byte[] data) {
        this.buffer = ByteBuffer.wrap(data);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void ensureCapacity(int needed) {
        if (buffer.remaining() < needed) {
            int newSize = Math.max(buffer.capacity() * 2, buffer.capacity() + needed);
            ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
            newBuffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;
        }
    }

    // Write methods

    public void writeBoolean(boolean value) {
        ensureCapacity(1);
        buffer.put((byte) (value ? 1 : 0));
    }

    public void writeByte(int value) {
        ensureCapacity(1);
        buffer.put((byte) value);
    }

    public void writeShort(int value) {
        ensureCapacity(2);
        buffer.putShort((short) value);
    }

    public void writeInt(int value) {
        ensureCapacity(4);
        buffer.putInt(value);
    }

    public void writeLong(long value) {
        ensureCapacity(8);
        buffer.putLong(value);
    }

    public void writeFloat(float value) {
        ensureCapacity(4);
        buffer.putFloat(value);
    }

    public void writeDouble(double value) {
        ensureCapacity(8);
        buffer.putDouble(value);
    }

    public void writeVarInt(int value) {
        ensureCapacity(5);
        while ((value & 0xFFFFFF80) != 0) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }

    public void writeString(String value) {
        if (value == null) {
            writeVarInt(0);
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        ensureCapacity(bytes.length);
        buffer.put(bytes);
    }

    public void writeUUID(UUID uuid) {
        ensureCapacity(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
    }

    public void writeBytes(byte[] bytes) {
        ensureCapacity(bytes.length);
        buffer.put(bytes);
    }

    // Read methods

    public boolean readBoolean() {
        return buffer.get() != 0;
    }

    public byte readByte() {
        return buffer.get();
    }

    public short readShort() {
        return buffer.getShort();
    }

    public int readInt() {
        return buffer.getInt();
    }

    public long readLong() {
        return buffer.getLong();
    }

    public float readFloat() {
        return buffer.getFloat();
    }

    public double readDouble() {
        return buffer.getDouble();
    }

    public int readVarInt() {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = buffer.get();
            value |= (currentByte & 0x7F) << position;

            if ((currentByte & 0x80) == 0)
                break;

            position += 7;

            if (position >= 32)
                throw new RuntimeException("VarInt is too big");
        }

        return value;
    }

    public String readString() {
        int length = readVarInt();
        if (length < 0) {
            throw new RuntimeException("String length is less than zero: " + length);
        }
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public UUID readUUID() {
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public void readBytes(byte[] bytes) {
        buffer.get(bytes);
    }

    public void writePosition(int x, int y, int z) {
        long pos = ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (long) (y & 0xFFF);
        writeLong(pos);
    }

    public long readPosition() {
        return readLong();
    }

    public static int getPositionX(long pos) {
        return (int) (pos >> 38);
    }

    public static int getPositionY(long pos) {
        return (int) (pos & 0xFFF);
    }

    public static int getPositionZ(long pos) {
        return (int) ((pos >> 12) & 0x3FFFFFF);
    }

    /**
     * Get the written data as a byte array
     */
    public byte[] array() {
        if (buffer.hasArray()) {
            // If we are writing, we need to return only the valid part
            // But since we are using a single buffer for both read/write in this simple
            // impl,
            // we need to be careful.
            // For writing: position is at the end of data.
            // For reading: position is current read index.

            // Assuming this is called after writing to get the packet bytes:
            byte[] ret = new byte[buffer.position()];
            ByteBuffer dup = buffer.duplicate();
            dup.flip();
            dup.get(ret);
            return ret;
        }
        return new byte[0];
    }

    /**
     * Get the size of the buffer
     */
    public int readableBytes() {
        return buffer.remaining();
    }
}