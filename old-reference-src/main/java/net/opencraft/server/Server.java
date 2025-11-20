package net.opencraft.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.opencraft.Session;
import net.opencraft.client.entity.EntityPlayer;
import net.opencraft.client.world.World;
import net.opencraft.core.Server;
import net.opencraft.shared.network.INetworkManager;
import net.opencraft.shared.network.LocalNetworkManager;

/**
 * A simple server class that maintains compatibility with the original codebase
 * This acts as a wrapper for the core server implementation
 */
public class Server {

    private net.opencraft.core.Server coreServer;
    private List<EntityPlayer> players;
    private boolean isRunning;
    private String worldName;
    private File saveDir;

    // Server configuration
    private int maxPlayers;
    private int serverPort;

    public Server(String worldName, File saveDir) {
        this.worldName = worldName;
        this.saveDir = saveDir;
        this.players = new ArrayList<>();
        this.maxPlayers = 8; // Default max players
        this.serverPort = 25565; // Default Minecraft server port
        this.isRunning = false;

        // Initialize the core server
        this.coreServer = new net.opencraft.core.Server(worldName, saveDir);
    }

    /**
     * Start the server
     */
    public void start() {
        coreServer.start();
        isRunning = true;

        System.out.println("OpenCraft server started on port " + serverPort);
    }

    /**
     * Stop the server
     */
    public void stop() {
        coreServer.stop();
        isRunning = false;

        // Disconnect all players
        for (EntityPlayer player : new ArrayList<>(players)) {
            disconnectPlayer(player);
        }

        System.out.println("OpenCraft server stopped");
    }

    /**
     * Add a player to the server
     */
    public boolean addPlayer(String username, Session session) {
        if (players.size() >= maxPlayers) {
            return false; // Server is full
        }

        // Add player to core server and local list
        String uuid = session != null ? session.sessionId : "uuid-" + username; // Generate UUID if not provided
        boolean success = coreServer.addPlayer(username, uuid);

        if (success) {
            System.out.println("Player " + username + " joined the game");
        }

        return success;
    }

    /**
     * Remove a player from the server
     */
    public void disconnectPlayer(EntityPlayer player) {
        // Remove from core server
        if (coreServer.getWorld() != null) {
            coreServer.getWorld().setEntityDead(player);
        }

        players.remove(player);
        System.out.println("Player " + player.username + " left the game");
    }

    /**
     * Get the server world
     */
    public World getWorld() {
        // Return the client world for compatibility with the original codebase
        // For proper server-client communication, we need to handle this differently
        return null; // Placeholder until proper integration
    }

    /**
     * Check if the server is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get the core server instance
     */
    public net.opencraft.core.Server getCoreServer() {
        return coreServer;
    }
}