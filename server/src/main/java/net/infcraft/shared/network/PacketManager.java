package net.infcraft.shared.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.infcraft.shared.network.packets.IPacket;
// Imports will be added as we implement packets

/**
 * Manager for handling different packet types and their serialization
 */
public class PacketManager {

    private static Map<ConnectionState, Map<Integer, Class<? extends IPacket>>> statePacketMap = new HashMap<>();
    private static Map<Class<? extends IPacket>, Integer> packetClassToId = new HashMap<>();
    private static Map<Class<? extends IPacket>, ConnectionState> packetClassToState = new HashMap<>();

    static {
        for (ConnectionState state : ConnectionState.values()) {
            statePacketMap.put(state, new HashMap<>());
        }

        // Packets will be registered here as they are implemented
        // Example: registerPacket(ConnectionState.PLAY, 0x00, PacketKeepAlive.class);

        // Handshaking
        registerPacket(ConnectionState.HANDSHAKING, 0x00, net.infcraft.shared.network.packets.PacketHandshake.class);

        // Configuration
        registerPacket(ConnectionState.CONFIGURATION, 0x00,
                net.infcraft.shared.network.packets.PacketRegistryData.class);
        registerPacket(ConnectionState.CONFIGURATION, 0x01,
                net.infcraft.shared.network.packets.PacketFinishConfig.class); // S->C
        // Note: PacketAckFinish is also 0x01 but C->S. The current PacketManager
        // doesn't distinguish direction in the map.
        // We might need to check direction or use different IDs if they conflict.
        // However, the spec says:
        // 0x01 Finish Configuration (S -> C)
        // 0x01 Acknowledge Finish (C -> S)
        // Since they share the same ID, we can register both if we had separate maps
        // for directions,
        // or we can just register one class that handles both if they had same
        // structure (they are both empty).
        // But they are different classes.
        // For now, let's assume the server only needs to deserialize C->S and client
        // S->C.
        // But PacketManager is shared.
        // A common issue in shared packet managers.
        // We can register PacketAckFinish for now as it's what the server receives.
        // The client will need to manually handle FinishConfig or we need a better
        // registration system.
        // Let's register PacketAckFinish for 0x01 in CONFIGURATION for now, as we are
        // mostly working on server side logic?
        // Actually, let's check if we can register both. Map key is ID. We can't.
        // We should probably split registration by direction or just use one class for
        // both if possible.
        // They are both empty. Let's use PacketAckFinish for both for now or create a
        // generic PacketConfigFinish.
        // Or better, let's register PacketAckFinish.
        registerPacket(ConnectionState.CONFIGURATION, 0x01, net.infcraft.shared.network.packets.PacketAckFinish.class);

        // Login
        registerPacket(ConnectionState.LOGIN, 0x02, net.infcraft.shared.network.packets.PacketLoginSuccess.class);

        // Play - General
        registerPacket(ConnectionState.PLAY, 0x00, net.infcraft.shared.network.packets.PacketKeepAlive.class);
        // PacketLoginSuccess moved to LOGIN state
        registerPacket(ConnectionState.PLAY, 0x02, net.infcraft.shared.network.packets.PacketChatMessage.class);

        // Play - World
        registerPacket(ConnectionState.PLAY, 0x20, net.infcraft.shared.network.packets.PacketChunkData.class);
        registerPacket(ConnectionState.PLAY, 0x21, net.infcraft.shared.network.packets.PacketBlockChange.class);
        registerPacket(ConnectionState.PLAY, 0x22, net.infcraft.shared.network.packets.PacketPlayerDigging.class);
        registerPacket(ConnectionState.PLAY, 0x23, net.infcraft.shared.network.packets.PacketBlockPlacement.class);
        registerPacket(ConnectionState.PLAY, 0x24, net.infcraft.shared.network.packets.PacketHeldItemChange.class);

        // Play - Entities
        registerPacket(ConnectionState.PLAY, 0x30, net.infcraft.shared.network.packets.PacketSpawnEntity.class);
        registerPacket(ConnectionState.PLAY, 0x31, net.infcraft.shared.network.packets.PacketEntityPosition.class);
        registerPacket(ConnectionState.PLAY, 0x33,
                net.infcraft.shared.network.packets.PacketPlayerPositionRotation.class);

        // Play - Inventory
        registerPacket(ConnectionState.PLAY, 0x60, net.infcraft.shared.network.packets.PacketWindowClick.class);
        registerPacket(ConnectionState.PLAY, 0x61, net.infcraft.shared.network.packets.PacketSetSlot.class);
        registerPacket(ConnectionState.PLAY, 0x62, net.infcraft.shared.network.packets.PacketWindowItems.class);

        // Play - Sound
        registerPacket(ConnectionState.PLAY, 0x25, net.infcraft.shared.network.packets.PacketPlaySound.class);

    }

    /**
     * Register a packet type with its ID and State
     */
    public static void registerPacket(ConnectionState state, int packetId, Class<? extends IPacket> packetClass) {
        statePacketMap.get(state).put(packetId, packetClass);
        packetClassToId.put(packetClass, packetId);
        packetClassToState.put(packetClass, state);
    }

    /**
     * Serialize a packet to bytes
     * Framing: Length (VarInt) + Packet ID (VarInt) + Data
     */
    public static byte[] serializePacket(IPacket packet) throws IOException {
        Integer packetId = packetClassToId.get(packet.getClass());
        if (packetId == null) {
            throw new IOException("Unregistered packet: " + packet.getClass().getSimpleName());
        }

        // 1. Write Packet ID + Data to a temporary buffer to calculate length
        PacketBuffer dataBuffer = new PacketBuffer();
        dataBuffer.writeVarInt(packetId);
        packet.writePacketData(dataBuffer);
        byte[] packetData = dataBuffer.array();

        // 2. Write Length + Packet Data to final buffer
        PacketBuffer finalBuffer = new PacketBuffer();
        finalBuffer.writeVarInt(packetData.length);
        finalBuffer.writeBytes(packetData);

        return finalBuffer.array();
    }

    /**
     * Deserialize a packet from bytes
     * Expects the full frame: Length + ID + Data
     * Note: In a real TCP stream, we'd need a framing decoder.
     * This method assumes 'data' contains exactly one full packet frame or the
     * payload.
     * 
     * For simplicity in this refactor, we assume the input 'data' starts with the
     * Packet ID
     * (Length already handled/stripped by the transport layer or we read it here).
     * 
     * However, the spec says "Every packet sent over the wire MUST follow this
     * structure... receiver relies on Length".
     * If this method is called with raw wire bytes, we should read Length first.
     */
    public static IPacket deserializePacket(byte[] data, ConnectionState state)
            throws IOException, ClassNotFoundException {
        PacketBuffer buffer = new PacketBuffer(data);

        // Read Length (we assume the buffer starts with Length)
        int length = buffer.readVarInt();

        // Check if we have enough data (simple check, might not be sufficient for
        // partial buffers)
        if (buffer.readableBytes() < length) {
            // In a real Netty handler, we would wait for more bytes.
            // Here we might just proceed if we assume 'data' is the full frame.
        }

        // Read Packet ID
        int packetId = buffer.readVarInt();

        Class<? extends IPacket> packetClass = statePacketMap.get(state).get(packetId);
        if (packetClass == null) {
            throw new IOException("Unknown packet ID: " + packetId + " in state: " + state);
        }

        IPacket packet;
        try {
            packet = packetClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | java.lang.reflect.InvocationTargetException
                | NoSuchMethodException e) {
            throw new IOException("Could not instantiate packet: " + e.getMessage());
        }

        // Read Data
        packet.readPacketData(buffer);

        return packet;
    }

    public static int getPacketId(Class<? extends IPacket> packetClass) {
        return packetClassToId.getOrDefault(packetClass, -1);
    }
}