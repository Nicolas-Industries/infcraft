package net.opencraft.client.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.opencraft.OpenCraft;
import net.opencraft.entity.EntityPlayerSP;
import net.opencraft.shared.network.INetworkManager;
import net.opencraft.shared.network.PacketManager;
import net.opencraft.shared.network.packets.IPacket;
import net.opencraft.shared.network.packets.PacketBlockChange;
import net.opencraft.shared.network.packets.PacketPlayerInfo;
import net.opencraft.shared.network.packets.PacketPlayerPosition;

/**
 * Client-side network manager for handling communication with servers
 */
public class ClientNetworkManager implements INetworkManager {
    
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Queue<Object> receivedPackets;
    private boolean connectionOpen;
    private boolean connectedToIntegratedServer = false;
    private OpenCraft gameInstance;
    
    // For remote connections
    private String serverAddress;
    private int serverPort;
    
    // For receiving packets
    private ExecutorService packetProcessor;
    private volatile boolean running = false;
    
    private byte[] pendingPacketData;
    private int bytesReceived = 0;
    private int expectedLength = -1;

    // Network statistics for optimization
    private long lastPingTime = 0;
    private int ping = 0;
    private long lastPacketReceivedTime = 0;

    public ClientNetworkManager(OpenCraft gameInstance) {
        this.gameInstance = gameInstance;
        this.receivedPackets = new ConcurrentLinkedQueue<>();
        this.packetProcessor = Executors.newSingleThreadExecutor();
        this.connectionOpen = false;
        this.lastPacketReceivedTime = System.currentTimeMillis();
    }
    
    /**
     * Connect to an integrated server (running in the same process)
     */
    public void connectToIntegratedServer() {
        connectedToIntegratedServer = true;
        connectionOpen = true;
        System.out.println("Connected to integrated server");
    }
    
    /**
     * Connect to a remote server
     */
    public boolean connectToRemoteServer(String address, int port) {
        try {
            socket = new Socket(address, port);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            
            serverAddress = address;
            serverPort = port;
            connectionOpen = true;
            running = true;
            
            // Start receiving packets in a separate thread
            packetProcessor.submit(this::receivePackets);
            
            System.out.println("Connected to remote server: " + address + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            connectionOpen = false;
            return false;
        }
    }
    
    @Override
    public void sendPacket(Object packet) throws IOException {
        if (!connectionOpen) {
            throw new IOException("Not connected to server");
        }
        
        if (packet instanceof IPacket) {
            byte[] serializedPacket = PacketManager.serializePacket((IPacket) packet);
            
            if (connectedToIntegratedServer) {
                // For integrated server, directly process through local network manager
                // This would be handled differently in the full implementation
                System.out.println("Sending packet to integrated server: " + 
                                 packet.getClass().getSimpleName());
            } else {
                // Send to remote server
                outputStream.writeInt(serializedPacket.length);
                outputStream.write(serializedPacket);
                outputStream.flush();
            }
        }
    }
    
    @Override
    public boolean isConnectionOpen() {
        if (connectedToIntegratedServer) {
            return connectionOpen;
        } else {
            return connectionOpen && socket != null && !socket.isClosed() && socket.isConnected();
        }
    }
    
    @Override
    public void closeConnection() {
        running = false;
        connectionOpen = false;
        
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        
        packetProcessor.shutdown();
    }
    
    @Override
    public Object getReceivedPacket() {
        return receivedPackets.poll();
    }
    
    @Override
    public void processNetworkData() {
        // Process received packets
        Object packet;
        while ((packet = getReceivedPacket()) != null) {
            processReceivedPacket(packet);
        }

        // Check connection health
        checkConnectionHealth();
    }

    /**
     * Check the health of the connection
     */
    private void checkConnectionHealth() {
        if (!connectedToIntegratedServer && System.currentTimeMillis() - lastPacketReceivedTime > 30000) { // 30 seconds
            // No packets received in 30 seconds, connection might be dead
            System.out.println("Connection timeout: No packets received for 30 seconds");
            connectionOpen = false;
        }
    }

    /**
     * Get the connection ping in milliseconds
     */
    public int getPing() {
        return ping;
    }

    /**
     * Check if connection is healthy
     */
    public boolean isConnectionHealthy() {
        long timeSinceLastPacket = System.currentTimeMillis() - lastPacketReceivedTime;
        return timeSinceLastPacket < 10000; // Healthy if received packet in last 10 seconds
    }
    
    /**
     * Process a received packet
     */
    private void processReceivedPacket(Object packetObj) {
        if (!(packetObj instanceof IPacket)) {
            return;
        }
        
        IPacket packet = (IPacket) packetObj;
        
        switch (packet.getPacketId()) {
            case 0x00: // Player position packet
                if (packet instanceof PacketPlayerPosition) {
                    handlePlayerPositionPacket((PacketPlayerPosition) packet);
                }
                break;
            case 0x06: // Block change packet
                if (packet instanceof PacketBlockChange) {
                    handleBlockChangePacket((PacketBlockChange) packet);
                }
                break;
            case 0x38: // Player info packet
                if (packet instanceof PacketPlayerInfo) {
                    handlePlayerInfoPacket((PacketPlayerInfo) packet);
                }
                break;
            default:
                System.out.println("Unknown packet ID: 0x" + 
                                 Integer.toHexString(packet.getPacketId()));
                break;
        }
    }
    
    /**
     * Handle player position packet
     */
    private void handlePlayerPositionPacket(PacketPlayerPosition packet) {
        // Update other players' positions in the world
        if (gameInstance != null && gameInstance.player != null) {
            // For now, we'll update the local player position
            // In multiplayer, this would update other players' positions
            System.out.println("Received player position update: (" + 
                             packet.getX() + ", " + packet.getY() + ", " + packet.getZ() + ")");
        }
    }
    
    /**
     * Handle block change packet
     */
    private void handleBlockChangePacket(PacketBlockChange packet) {
        // Update block in the client world
        if (gameInstance != null && gameInstance.world != null) {
            // In a real implementation, this would update the client's world representation
            System.out.println("Received block change: (" + 
                             packet.getX() + ", " + packet.getY() + ", " + packet.getZ() + 
                             ") -> Block ID: " + packet.getBlockId());
        }
    }
    
    /**
     * Handle player info packet
     */
    private void handlePlayerInfoPacket(PacketPlayerInfo packet) {
        // Handle player join/leave notifications
        if (packet.isJoining()) {
            System.out.println("Player joined: " + packet.getPlayerName());
        } else {
            System.out.println("Player left: " + packet.getPlayerName());
        }
    }
    
    /**
     * Receive packets from the server in a separate thread
     */
    private void receivePackets() {
        while (running && isConnectionOpen()) {
            try {
                if (inputStream.available() > 0) {
                    if (expectedLength == -1) {
                        // We need to read the packet length first
                        if (inputStream.available() >= 4) { // int is 4 bytes
                            expectedLength = inputStream.readInt();
                            pendingPacketData = new byte[expectedLength];
                            bytesReceived = 0;
                        }
                    }

                    if (expectedLength > 0 && bytesReceived < expectedLength) {
                        // Read as much data as available
                        int toRead = Math.min(expectedLength - bytesReceived, inputStream.available());
                        int read = inputStream.read(pendingPacketData, bytesReceived, toRead);

                        if (read > 0) {
                            bytesReceived += read;
                        }

                        if (bytesReceived == expectedLength) {
                            // Complete packet received
                            IPacket packet = PacketManager.deserializePacket(pendingPacketData);
                            receivedPackets.add(packet);

                            // Update network statistics
                            lastPacketReceivedTime = System.currentTimeMillis();

                            // Reset for next packet
                            expectedLength = -1;
                            pendingPacketData = null;
                            bytesReceived = 0;
                        }
                    }
                }

                Thread.sleep(1); // Small delay to prevent excessive CPU usage
            } catch (Exception e) {
                if (running) {
                    System.err.println("Error receiving packet: " + e.getMessage());
                }
                break; // Exit if there's an error
            }
        }
    }
    
    /**
     * Check if connected to integrated server
     */
    public boolean isConnectedToIntegrated() {
        return connectedToIntegratedServer;
    }
    
    /**
     * Get the server address
     */
    public String getServerAddress() {
        return serverAddress;
    }
    
    /**
     * Get the server port
     */
    public int getServerPort() {
        return serverPort;
    }
}