package net.opencraft.shared.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.opencraft.shared.network.packets.IPacket;
import net.opencraft.shared.network.packets.PacketBlockChange;
import net.opencraft.shared.network.packets.PacketPlayerInfo;
import net.opencraft.shared.network.packets.PacketPlayerPosition;
import net.opencraft.shared.network.packets.PacketEffect;
import net.opencraft.shared.network.packets.PacketEntityState;
import net.opencraft.shared.network.packets.PacketWorldState;

/**
 * Manager for handling different packet types and their serialization
 */
public class PacketManager {
    
    private static Map<Integer, Class<? extends IPacket>> packetTypesToClass = new HashMap<>();
    private static Map<Class<? extends IPacket>, Integer> packetClassToTypes = new HashMap<>();
    
    static {
        // Register packet types
        registerPacket(0x00, PacketPlayerPosition.class);
        registerPacket(0x06, PacketBlockChange.class);
        registerPacket(0x38, PacketPlayerInfo.class);
        registerPacket(0x0A, PacketEffect.class);
        registerPacket(0x0B, PacketEntityState.class);
        registerPacket(0x0C, PacketWorldState.class);
        // Add more packet registrations here as needed
    }
    
    /**
     * Register a packet type with its ID
     */
    public static void registerPacket(int packetId, Class<? extends IPacket> packetClass) {
        packetTypesToClass.put(packetId, packetClass);
        packetClassToTypes.put(packetClass, packetId);
    }
    
    /**
     * Serialize a packet to bytes
     */
    public static byte[] serializePacket(IPacket packet) throws IOException {
        PacketBuffer buffer = new PacketBuffer();

        // Write packet ID first
        buffer.writeInt(packet.getPacketId());

        // Write packet-specific data based on packet type
        if (packet instanceof PacketPlayerPosition) {
            PacketPlayerPosition p = (PacketPlayerPosition) packet;
            buffer.writeDouble(p.getX());
            buffer.writeDouble(p.getY());
            buffer.writeDouble(p.getZ());
            buffer.writeFloat(p.getYaw());
            buffer.writeFloat(p.getPitch());
            buffer.writeBoolean(p.isOnGround());
        } else if (packet instanceof PacketBlockChange) {
            PacketBlockChange p = (PacketBlockChange) packet;
            buffer.writeInt(p.getX());
            buffer.writeInt(p.getY());
            buffer.writeInt(p.getZ());
            buffer.writeInt(p.getBlockId());
            buffer.writeInt(p.getMetadata());
        } else if (packet instanceof PacketPlayerInfo) {
            PacketPlayerInfo p = (PacketPlayerInfo) packet;
            buffer.writeString(p.getPlayerName());
            buffer.writeInt(p.getPlayerId());
            buffer.writeBoolean(p.isJoining());
        } else if (packet instanceof PacketEffect) {
            PacketEffect p = (PacketEffect) packet;
            buffer.writeInt(p.getEffectType().getId());
            buffer.writeDouble(p.getX());
            buffer.writeDouble(p.getY());
            buffer.writeDouble(p.getZ());
            buffer.writeDouble(p.getVelocityX());
            buffer.writeDouble(p.getVelocityY());
            buffer.writeDouble(p.getVelocityZ());
            buffer.writeFloat(p.getVolume());
            buffer.writeFloat(p.getPitch());
        } else if (packet instanceof PacketEntityState) {
            PacketEntityState p = (PacketEntityState) packet;
            buffer.writeInt(p.getEntities().size());
            for (PacketEntityState.EntityData entity : p.getEntities()) {
                buffer.writeInt(entity.entityId);
                buffer.writeInt(entity.entityType);
                buffer.writeDouble(entity.x);
                buffer.writeDouble(entity.y);
                buffer.writeDouble(entity.z);
                buffer.writeFloat(entity.yaw);
                buffer.writeFloat(entity.pitch);
                buffer.writeDouble(entity.velocityX);
                buffer.writeDouble(entity.velocityY);
                buffer.writeDouble(entity.velocityZ);
                buffer.writeFloat(entity.health);
                buffer.writeInt(entity.metadata);
            }
        } else if (packet instanceof PacketWorldState) {
            PacketWorldState p = (PacketWorldState) packet;
            buffer.writeLong(p.getWorldTime());
            buffer.writeBoolean(p.isRaining());
            buffer.writeFloat(p.getRainStrength());
            buffer.writeBoolean(p.isThundering());
            buffer.writeFloat(p.getThunderStrength());
        } else {
            // Unknown packet type - throw exception
            throw new IOException("Unknown packet type: " + packet.getClass().getSimpleName());
        }

        return buffer.array();
    }

    /**
     * Compress packet data to reduce network traffic (placeholder implementation)
     */
    public static byte[] compressPacketData(byte[] data) {
        // In a real implementation, this would use compression like gzip
        // For now, just return the original data
        return data;
    }

    /**
     * Decompress packet data (placeholder implementation)
     */
    public static byte[] decompressPacketData(byte[] data) {
        // In a real implementation, this would decompress the data
        // For now, just return the original data
        return data;
    }
    
    /**
     * Deserialize a packet from bytes
     */
    public static IPacket deserializePacket(byte[] data) throws IOException, ClassNotFoundException {
        PacketBuffer buffer = new PacketBuffer(data);
        
        int packetId = buffer.readInt();
        
        Class<? extends IPacket> packetClass = packetTypesToClass.get(packetId);
        if (packetClass == null) {
            throw new IOException("Unknown packet ID: " + packetId);
        }
        
        IPacket packet;
        try {
            packet = packetClass.newInstance();
        } catch (InstantiationException e) {
            throw new IOException("Could not instantiate packet: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IOException("Could not access packet constructor: " + e.getMessage());
        }
        
        // Deserialize packet-specific data based on packet type
        if (packet instanceof PacketPlayerPosition) {
            PacketPlayerPosition p = (PacketPlayerPosition) packet;
            p.setX(buffer.readDouble());
            p.setY(buffer.readDouble());
            p.setZ(buffer.readDouble());
            p.setYaw(buffer.readFloat());
            p.setPitch(buffer.readFloat());
            p.setOnGround(buffer.readBoolean());
        } else if (packet instanceof PacketBlockChange) {
            PacketBlockChange p = (PacketBlockChange) packet;
            p.setX(buffer.readInt());
            p.setY(buffer.readInt());
            p.setZ(buffer.readInt());
            p.setBlockId(buffer.readInt());
            p.setMetadata(buffer.readInt());
        } else if (packet instanceof PacketPlayerInfo) {
            PacketPlayerInfo p = (PacketPlayerInfo) packet;
            p.setPlayerName(buffer.readString());
            p.setPlayerId(buffer.readInt());
            p.setJoining(buffer.readBoolean());
        } else if (packet instanceof PacketEffect) {
            PacketEffect p = (PacketEffect) packet;
            p.setEffectType(EffectType.getById(buffer.readInt()));
            p.setX(buffer.readDouble());
            p.setY(buffer.readDouble());
            p.setZ(buffer.readDouble());
            p.setVelocityX(buffer.readDouble());
            p.setVelocityY(buffer.readDouble());
            p.setVelocityZ(buffer.readDouble());
            p.setVolume(buffer.readFloat());
            p.setPitch(buffer.readFloat());
        } else if (packet instanceof PacketEntityState) {
            PacketEntityState p = (PacketEntityState) packet;
            int entityCount = buffer.readInt();
            for (int i = 0; i < entityCount; i++) {
                PacketEntityState.EntityData entity = new PacketEntityState.EntityData();
                entity.entityId = buffer.readInt();
                entity.entityType = buffer.readInt();
                entity.x = buffer.readDouble();
                entity.y = buffer.readDouble();
                entity.z = buffer.readDouble();
                entity.yaw = buffer.readFloat();
                entity.pitch = buffer.readFloat();
                entity.velocityX = buffer.readDouble();
                entity.velocityY = buffer.readDouble();
                entity.velocityZ = buffer.readDouble();
                entity.health = buffer.readFloat();
                entity.metadata = buffer.readInt();
                p.addEntity(entity);
            }
        } else if (packet instanceof PacketWorldState) {
            PacketWorldState p = (PacketWorldState) packet;
            p.setWorldTime(buffer.readLong());
            p.setRaining(buffer.readBoolean());
            p.setRainStrength(buffer.readFloat());
            p.setThundering(buffer.readBoolean());
            p.setThunderStrength(buffer.readFloat());
        }
        
        // Return the packet - in a real implementation, we'd have set all the fields
        return packet;
    }
    
    /**
     * Get the packet ID for a packet class
     */
    public static int getPacketId(Class<? extends IPacket> packetClass) {
        return packetClassToTypes.getOrDefault(packetClass, -1);
    }
    
    /**
     * Check if a packet ID is registered
     */
    public static boolean isPacketRegistered(int packetId) {
        return packetTypesToClass.containsKey(packetId);
    }
}