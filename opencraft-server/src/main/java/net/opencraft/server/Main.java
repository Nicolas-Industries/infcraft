package net.opencraft.server;

/**
 * Main server class that starts the OpenCraft server
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting OpenCraft Server...");
        
        Server server = new Server();
        server.start();
        
        // Keep the server running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server interrupted, shutting down...");
            server.stop();
        }
    }
}