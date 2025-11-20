package net.opencraft.shared.network;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Network manager for local/integrated server communication
 * This allows the client to communicate with the integrated server as if it were remote
 */
public class LocalNetworkManager implements INetworkManager {
    
    private Queue<Object> outboundPackets;
    private Queue<Object> inboundPackets;
    private boolean connectionOpen;
    private Object serverWorld; // Reference to the server world for local communication
    
    public LocalNetworkManager(Object serverWorldRef) {
        this.outboundPackets = new ConcurrentLinkedQueue<>();
        this.inboundPackets = new ConcurrentLinkedQueue<>();
        this.connectionOpen = true;
        this.serverWorld = serverWorldRef;
    }
    
    @Override
    public void sendPacket(Object packet) throws IOException {
        if (!connectionOpen) {
            throw new IOException("Connection is closed");
        }
        
        // Add packet to outbound queue
        outboundPackets.add(packet);
        
        // In local communication, immediately process the packet on the server side
        processPacketOnServer(packet);
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
    
    /**
     * Process a packet on the server side
     */
    private void processPacketOnServer(Object packet) {
        // In a real implementation, this would route the packet to the appropriate
        // server handler, but for now we'll just add it to the inbound queue
        // for simulation purposes
        inboundPackets.add(packet);
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
}