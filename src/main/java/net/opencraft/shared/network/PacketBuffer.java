package net.opencraft.shared.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.opencraft.nbt.NBTBase;
import net.opencraft.nbt.NBTTagCompound;

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
    
    public void writeNBTTagCompound(NBTTagCompound tag) throws IOException {
        if (tag == null) {
            writeBoolean(false);
        } else {
            writeBoolean(true);
            // In a real implementation, this would serialize the NBT tag
            // For now, just write a dummy value
            writeString(tag.toString());
        }
    }
    
    // Read methods
    public int readInt() throws IOException {
        if (dataInput != null) {
            return dataInput.readInt();
        }
        return 0;
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
    
    public NBTTagCompound readNBTTagCompound() throws IOException {
        boolean hasTag = readBoolean();
        if (!hasTag) {
            return null;
        }
        // In a real implementation, this would deserialize the NBT tag
        // For now, return a new tag
        return new NBTTagCompound();
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