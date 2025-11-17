package net.opencraft.shared.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A buffer for reading and writing packet data
 * This class handles serialization and deserialization of packet data
 */
public class PacketBuffer {

    private ByteArrayOutputStream byteBuffer;
    private DataOutputStream dataOutput;
    private DataInputStream dataInput;
    private byte[] internalArray;

    public PacketBuffer() {
        this.byteBuffer = new ByteArrayOutputStream();
        this.dataOutput = new DataOutputStream(byteBuffer);
    }

    public PacketBuffer(byte[] data) {
        this.internalArray = data;
        this.dataInput = new DataInputStream(new ByteArrayInputStream(data));
    }

    // Write methods
    public void writeInt(int value) throws IOException {
        dataOutput.writeInt(value);
    }

    public void writeLong(long value) throws IOException {
        dataOutput.writeLong(value);
    }

    public void writeFloat(float value) throws IOException {
        dataOutput.writeFloat(value);
    }

    public void writeDouble(double value) throws IOException {
        dataOutput.writeDouble(value);
    }

    public void writeBoolean(boolean value) throws IOException {
        dataOutput.writeBoolean(value);
    }

    public void writeString(String value) throws IOException {
        if (value == null) {
            writeInt(-1);
        } else {
            byte[] bytes = value.getBytes("UTF-8");
            writeInt(bytes.length);
            dataOutput.write(bytes);
        }
    }

    // Read methods
    public int readInt() throws IOException {
        if (dataInput != null) {
            return dataInput.readInt();
        }
        return 0;
    }

    public long readLong() throws IOException {
        if (dataInput != null) {
            return dataInput.readLong();
        }
        return 0L;
    }

    public float readFloat() throws IOException {
        if (dataInput != null) {
            return dataInput.readFloat();
        }
        return 0.0f;
    }

    public double readDouble() throws IOException {
        if (dataInput != null) {
            return dataInput.readDouble();
        }
        return 0.0;
    }

    public boolean readBoolean() throws IOException {
        if (dataInput != null) {
            return dataInput.readBoolean();
        }
        return false;
    }

    public String readString() throws IOException {
        int length = readInt();
        if (length < 0) {
            return null;
        }
        byte[] bytes = new byte[length];
        dataInput.readFully(bytes);
        return new String(bytes, "UTF-8");
    }

    /**
     * Get the written data as a byte array
     */
    public byte[] array() {
        if (byteBuffer != null) {
            return byteBuffer.toByteArray();
        }
        return internalArray;
    }

    /**
     * Get the size of the buffer
     */
    public int readableBytes() {
        if (internalArray != null) {
            return internalArray.length;
        }
        if (byteBuffer != null) {
            return byteBuffer.size();
        }
        return 0;
    }
}