package net.opencraft.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.opencraft.shared.network.INetworkManager;
import net.opencraft.shared.network.LocalNetworkManager;
import net.opencraft.shared.network.PacketManager;
import net.opencraft.shared.network.packets.IPacket;

/**
 * Network system for the integrated server
 * Handles both local (integrated) connections and remote connections
 */
public class NetworkSystem {

    private Server server;
    private ExecutorService networkThreadPool;
    private boolean isRunning;

    // For remote connections
    private ServerSocket serverSocket;
    private Future<?> connectionAcceptorTask;

    // Track connected clients
    private Map<Socket, INetworkManager> connectedClients;

    public NetworkSystem(Server server) {
        this.server = server;
        this.networkThreadPool = Executors.newFixedThreadPool(4); // Thread pool for network operations
        this.isRunning = false;
        this.connectedClients = new ConcurrentHashMap<>();
    }
    
    /**
     * Initialize the integrated server network
     */
    public void initializeIntegratedServer() {
        isRunning = true;

        // For integrated server, we start with local connections only
        // The client will connect to the server running in the same process
        System.out.println("Integrated server network initialized");
    }

    /**
     * Start listening for remote connections
     */
    public void startRemoteServer(int port) {
        try {
            serverSocket = new ServerSocket(port);

            // Start accepting connections in a separate thread
            connectionAcceptorTask = networkThreadPool.submit(this::acceptConnections);

            System.out.println("Server listening on port " + port);
        } catch (IOException e) {
            System.err.println("Failed to start server on port " + port + ": " + e.getMessage());
        }
    }

    /**
     * Accept incoming connections
     */
    private void acceptConnections() {
        while (isRunning && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleNewConnection(clientSocket);
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
                break; // Exit if there's an error and we're still supposed to be running
            }
        }
    }

    /**
     * Handle a new client connection
     */
    private void handleNewConnection(Socket clientSocket) throws IOException {
        // In a real implementation, we'd perform handshake here
        // For now, we'll just create a basic network manager for the client
        INetworkManager networkManager = new RemoteNetworkManager(clientSocket);
        connectedClients.put(clientSocket, networkManager);

        System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

        // Process client data asynchronously
        networkThreadPool.submit(() -> processClientData(clientSocket, networkManager));
    }

    /**
     * Process data from a connected client
     */
    private void processClientData(Socket clientSocket, INetworkManager networkManager) {
        while (isRunning && networkManager.isConnectionOpen()) {
            try {
                networkManager.processNetworkData();

                // Process any received packets
                Object packet;
                while ((packet = networkManager.getReceivedPacket()) != null) {
                    if (packet instanceof IPacket) {
                        processPacketFromClient((IPacket) packet, clientSocket);
                    }
                }

                // Small delay to prevent excessive CPU usage
                Thread.sleep(1);
            } catch (Exception e) {
                System.err.println("Error processing client data: " + e.getMessage());
                break;
            }
        }

        // Client disconnected, clean up
        disconnectClient(clientSocket);
    }

    /**
     * Process a packet from a specific client
     */
    private void processPacketFromClient(IPacket packet, Socket clientSocket) {
        System.out.println("Received packet from client: " + packet.getClass().getSimpleName());

        // Handle the packet - in a real implementation, this would involve
        // processing different types of packets and updating the game state
        // based on the packet contents
    }

    /**
     * Disconnect a client
     */
    private void disconnectClient(Socket clientSocket) {
        INetworkManager networkManager = connectedClients.remove(clientSocket);
        if (networkManager != null) {
            networkManager.closeConnection();
        }

        try {
            if (!clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }

        System.out.println("Client disconnected: " + clientSocket.toString());
    }
    
    /**
     * Create a network manager for local/integrated server communication
     */
    public INetworkManager createLocalConnection() {
        return new LocalNetworkManager();
    }
    
    /**
     * Handle server-side packet processing
     */
    public void processServerPackets(INetworkManager networkManager) {
        // Process all received packets from clients
        Object packet;
        while ((packet = networkManager.getReceivedPacket()) != null) {
            if (packet instanceof IPacket) {
                processPacket((IPacket) packet, networkManager);
            }
        }
    }

    /**
     * Send world initialization to a new client
     */
    public void sendWorldInitToClient(INetworkManager networkManager) {
        if (server != null && server.world != null) {
            try {
                net.opencraft.shared.network.packets.PacketWorldInit worldInitPacket =
                    server.world.createWorldInitPacket();
                networkManager.sendPacket(worldInitPacket);
            } catch (Exception e) {
                System.err.println("Error sending world init to client: " + e.getMessage());
            }
        }
    }

    /**
     * Process a packet coming from a client (public method for LocalNetworkManager to call)
     */
    public void processPacket(Object packet, INetworkManager networkManager) {
        if (packet instanceof IPacket) {
            processPacket((IPacket) packet, networkManager);
        }
    }

    /**
     * Process an incoming packet from a client
     */
    private void processPacket(IPacket packet, INetworkManager networkManager) {
        // In a full implementation, this would route packets to appropriate handlers
        // For now, just log the packet type
        System.out.println("Processing packet: " + packet.getClass().getSimpleName() +
                          " (ID: 0x" + Integer.toHexString(packet.getPacketId()) + ")");

        // Example of handling specific packet types
        if (packet instanceof net.opencraft.shared.network.packets.PacketPlayerPosition) {
            // Handle player position updates
            handlePlayerPositionPacket((net.opencraft.shared.network.packets.PacketPlayerPosition) packet);
        }
    }
    
    /**
     * Handle player position packet
     */
    private void handlePlayerPositionPacket(net.opencraft.shared.network.packets.PacketPlayerPosition packet) {
        // Update player position in the world
        // This is server-side logic to validate and process the position update
        System.out.println("Player position update: (" + 
                          packet.getX() + ", " + 
                          packet.getY() + ", " + 
                          packet.getZ() + ")");
    }
    
    /**
     * Send a packet to all connected clients
     */
    public void sendPacketToAll(IPacket packet) {
        try {
            // Serialize the packet once
            byte[] serializedPacket = PacketManager.serializePacket(packet);

            // Send to all remote clients
            for (Map.Entry<Socket, INetworkManager> entry : connectedClients.entrySet()) {
                try {
                    INetworkManager networkManager = entry.getValue();
                    if (networkManager.isConnectionOpen()) {
                        networkManager.sendPacket(packet);
                    }
                } catch (IOException e) {
                    System.err.println("Error sending packet to client: " + e.getMessage());
                    // Remove the broken connection
                    disconnectClient(entry.getKey());
                }
            }

            // For integrated server, also send to the local client
            // This would be handled by the LocalNetworkManager if one exists
        } catch (IOException e) {
            System.err.println("Error serializing packet: " + e.getMessage());
        }
    }
    
    /**
     * Stop the network system
     */
    public void stop() {
        isRunning = false;
        networkThreadPool.shutdown();
    }
    
    /**
     * Get the network thread pool
     */
    public ExecutorService getNetworkThreadPool() {
        return networkThreadPool;
    }
}