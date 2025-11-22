package net.infcraft.shared.network.packets;

/**
 * Packet for sending world state updates from server to client
 */
public class PacketWorldState implements IPacket {

    private long worldTime;
    private boolean isRaining;
    private float rainStrength;
    private boolean isThundering;
    private float thunderStrength;

    // No-arg constructor for deserialization
    public PacketWorldState() {
    }

    public PacketWorldState(long worldTime, boolean isRaining, float rainStrength,
            boolean isThundering, float thunderStrength) {
        this.worldTime = worldTime;
        this.isRaining = isRaining;
        this.rainStrength = rainStrength;
        this.isThundering = isThundering;
        this.thunderStrength = thunderStrength;
    }

    @Override
    public int getPacketId() {
        return 0x0C; // World state packet ID
    }

    @Override
    public boolean isServerToClient() {
        return true;
    }

    @Override
    public boolean isClientToServer() {
        return false;
    }

    // Getters and setters
    public long getWorldTime() {
        return worldTime;
    }

    public void setWorldTime(long worldTime) {
        this.worldTime = worldTime;
    }

    public boolean isRaining() {
        return isRaining;
    }

    public void setRaining(boolean raining) {
        isRaining = raining;
    }

    public float getRainStrength() {
        return rainStrength;
    }

    public void setRainStrength(float rainStrength) {
        this.rainStrength = rainStrength;
    }

    public boolean isThundering() {
        return isThundering;
    }

    public void setThundering(boolean thundering) {
        isThundering = thundering;
    }

    public float getThunderStrength() {
        return thunderStrength;
    }

    public void setThunderStrength(float thunderStrength) {
        this.thunderStrength = thunderStrength;
    }

    @Override
    public void readPacketData(net.infcraft.shared.network.PacketBuffer buffer) throws java.io.IOException {
        this.worldTime = buffer.readLong();
        this.isRaining = buffer.readBoolean();
        this.rainStrength = buffer.readFloat();
        this.isThundering = buffer.readBoolean();
        this.thunderStrength = buffer.readFloat();
    }

    @Override
    public void writePacketData(net.infcraft.shared.network.PacketBuffer buffer) throws java.io.IOException {
        buffer.writeLong(worldTime);
        buffer.writeBoolean(isRaining);
        buffer.writeFloat(rainStrength);
        buffer.writeBoolean(isThundering);
        buffer.writeFloat(thunderStrength);
    }
}
