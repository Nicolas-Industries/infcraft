package net.opencraft.server;

import java.io.File;

public class ServerMain {
    
    public static void main(String[] args) {
        System.out.println("Starting OpenCraft Server...");
        
        // Default world name
        String worldName = "world";
        if (args.length > 0) {
            worldName = args[0];
        }
        
        // Create server instance
        File saveDir = new File("server_data");
        Server server = new Server(worldName, saveDir);
        
        // Configure server properties
        server.setServerName("OpenCraft Dedicated Server");
        server.setMaxPlayers(20);
        server.setServerPort(25565);
        server.setOnlineMode(false); // For testing
        
        try {
            // Start the server
            server.start();
            
            System.out.println("Server started successfully!");
            System.out.println("Listening on port " + server.getServerPort());
            System.out.println("Press Ctrl+C to stop the server.");
            
            // Keep the server running
            synchronized(server) {
                while (server.isRunning()) {
                    try {
                        server.wait();
                    } catch (InterruptedException e) {
                        System.out.println("Server interrupted, shutting down...");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean shutdown
            System.out.println("Shutting down server...");
            server.stop();
        }
    }
}