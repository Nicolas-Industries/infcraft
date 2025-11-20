package net.opencraft.shared.network.packets;

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
}