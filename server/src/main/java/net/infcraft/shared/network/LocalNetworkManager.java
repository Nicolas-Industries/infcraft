package net.infcraft.shared.network;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.infcraft.core.entity.EntityPlayer;

/**
 * Network manager for local/integrated server communication
 * This allows the client to communicate with the integrated server as if it were remote
 */
public class LocalNetworkManager implements INetworkManager {

    private Queue<Object> outboundPackets;
    private Queue<Object> inboundPackets;
    private boolean connectionOpen;
    private EntityPlayer player;


    public LocalNetworkManager() {
        this.outboundPackets = new ConcurrentLinkedQueue<>();
        this.inboundPackets = new ConcurrentLinkedQueue<>();
        this.connectionOpen = true;
    }

    @Override
    public void sendPacket(Object packet) throws IOException {
        if (!connectionOpen) {
            throw new IOException("Connection is closed");
        }

        // Add packet to outbound queue
        outboundPackets.add(packet);

        // In a real implementation, this would route the packet to the server
        // For now, this is handled by the server's NetworkSystem when it polls this manager
    }

    @Override
    public boolean isConnectionOpen() {
        return connectionOpen;
    }

    @Override
    public void closeConnection() {
        connectionOpen = false;
        outboundPackets.clear();
        inboundPackets.clear();
    }

    @Override
    public Object getReceivedPacket() {
        return inboundPackets.poll();
    }

    @Override
    public void processNetworkData() {
        // Process any pending server-to-client packets
        // In a real implementation, this would involve more complex synchronization
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    /**
     * Add a packet from the server to be received by the client
     */
    public void addServerPacket(Object packet) {
        inboundPackets.add(packet);
    }

    /**
     * Get outbound packets (for server to process)
     */
    public Queue<Object> getOutboundPackets() {
        return outboundPackets;
    }

    /**
     * Get inbound packets (for client to process)
     */
    public Queue<Object> getInboundPackets() {
        return inboundPackets;
    }

    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}