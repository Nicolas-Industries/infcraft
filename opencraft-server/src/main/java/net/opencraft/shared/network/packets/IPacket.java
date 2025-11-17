package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

/**
 * Base interface for all network packets
 */
public interface IPacket {
    /**
     * Get the ID of this packet type
     */
    int getPacketId();

    /**
     * Check if this is a server-to-client packet
     */
    boolean isServerToClient();

    /**
     * Check if this is a client-to-server packet
     */
    boolean isClientToServer();

    /**
     * Read packet data from buffer
     */
    void readPacketData(PacketBuffer buffer) throws java.io.IOException;

    /**
     * Write packet data to buffer
     */
    void writePacketData(PacketBuffer buffer) throws java.io.IOException;
}