package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;
import java.io.IOException;

public class PacketPlaySound implements IPacket {

    private String soundName;
    private double x;
    private double y;
    private double z;
    private float volume;
    private float pitch;

    public PacketPlaySound() {
    }

    public PacketPlaySound(String soundName, double x, double y, double z, float volume, float pitch) {
        this.soundName = soundName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() {
        return 0x25;
    }

    @Override
    public boolean isServerToClient() {
        return true;
    }

    @Override
    public boolean isClientToServer() {
        return false;
    }

    @Override
    public void readPacketData(PacketBuffer buffer) throws IOException {
        this.soundName = buffer.readString();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        this.volume = buffer.readFloat();
        this.pitch = buffer.readFloat();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) throws IOException {
        buffer.writeString(this.soundName);
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
        buffer.writeFloat(this.volume);
        buffer.writeFloat(this.pitch);
    }

    public String getSoundName() {
        return soundName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
