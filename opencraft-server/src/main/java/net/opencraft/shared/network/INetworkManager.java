package net.opencraft.shared.network;

import java.io.IOException;
import net.opencraft.shared.network.packets.IPacket;

/**
 * Interface for network communication between client and server
 */
public interface INetworkManager {
    /**
     * Send a packet to the connected peer
     */
    void sendPacket(Object packet) throws IOException;

    /**
     * Check if the connection is still active
     */
    boolean isConnectionOpen();

    /**
     * Close the connection
     */
    void closeConnection();

    /**
     * Get the most recent packet received
     */
    Object getReceivedPacket();

    /**
     * Process network events
     */
    void processNetworkData();

    /**
     * Check if network manager is for a local (integrated) server
     */
    boolean isLocal();
}