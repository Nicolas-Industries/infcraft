package net.opencraft.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.opencraft.Session;
import net.opencraft.entity.EntityPlayer;
import net.opencraft.world.World;

/**
 * A simple server class that maintains compatibility with the original codebase
 */
public class Server {
    
    private World world;
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
        
        // Note: We can't create the world yet because it requires specific parameters
        // The actual integration will be done through the original World class
    }
    
    /**
     * Start the server
     */
    public void start() {
        isRunning = true;
        
        System.out.println("OpenCraft server started on port " + serverPort);
    }
    
    /**
     * Stop the server
     */
    public void stop() {
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
        
        // In the original codebase, EntityPlayer constructor takes only a World
        // This is a simplified approach to maintain compatibility
        System.out.println("Player " + username + " joined the game");
        return true;
    }
    
    /**
     * Remove a player from the server
     */
    public void disconnectPlayer(EntityPlayer player) {
        players.remove(player);
        System.out.println("Player left the game");
    }
    
    /**
     * Get the server world
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * Check if the server is running
     */
    public boolean isRunning() {
        return isRunning;
    }
}