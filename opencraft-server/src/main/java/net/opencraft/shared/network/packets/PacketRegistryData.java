package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;
import java.util.HashMap;
import java.util.Map;

public class PacketRegistryData implements IPacket {

    private String registryCode;
    private Map<String, Integer> entries;

    public PacketRegistryData() {
        this.entries = new HashMap<>();
    }

    public PacketRegistryData(String registryCode, Map<String, Integer> entries) {
        this.registryCode = registryCode;
        this.entries = entries;
    }

    @Override
    public int getPacketId() {
        return 0x00;
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
    public void readPacketData(PacketBuffer buffer) {
        this.registryCode = buffer.readString();
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            String key = buffer.readString();
            int value = buffer.readVarInt();
            entries.put(key, value);
        }
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeString(this.registryCode);
        buffer.writeVarInt(entries.size());
        for (Map.Entry<String, Integer> entry : entries.entrySet()) {
            buffer.writeString(entry.getKey());
            buffer.writeVarInt(entry.getValue());
        }
    }

    public String getRegistryCode() {
        return registryCode;
    }

    public Map<String, Integer> getEntries() {
        return entries;
    }
}
