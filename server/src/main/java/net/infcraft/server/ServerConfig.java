package net.infcraft.server;

import java.io.*;
import java.util.Properties;

/**
 * Server configuration loaded from server.properties file
 * For integrated servers, settings can be passed directly from client
 */
public class ServerConfig {

    // Default values
    private static final int DEFAULT_RENDER_DISTANCE = 10;
    private static final int DEFAULT_MAX_PLAYERS = 8;
    private static final int DEFAULT_SERVER_PORT = 25565;
    private static final String DEFAULT_SERVER_NAME = "InfCraft Server";

    // Configuration fields
    private int renderDistance;
    private int maxPlayers;
    private int serverPort;
    private String serverName;

    /**
     * Create config with default values
     */
    public ServerConfig() {
        this.renderDistance = DEFAULT_RENDER_DISTANCE;
        this.maxPlayers = DEFAULT_MAX_PLAYERS;
        this.serverPort = DEFAULT_SERVER_PORT;
        this.serverName = DEFAULT_SERVER_NAME;
    }

    /**
     * Create config with custom render distance (for integrated server)
     */
    public ServerConfig(int renderDistance) {
        this();
        this.renderDistance = renderDistance;
    }

    /**
     * Load configuration from server.properties file
     * Creates file with defaults if it doesn't exist
     */
    public static ServerConfig loadFromFile(File file) {
        ServerConfig config = new ServerConfig();

        if (!file.exists()) {
            System.out.println("server.properties not found, creating with default values");
            config.saveToFile(file);
            return config;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);

            config.renderDistance = Integer
                    .parseInt(props.getProperty("render-distance", String.valueOf(DEFAULT_RENDER_DISTANCE)));
            config.maxPlayers = Integer.parseInt(props.getProperty("max-players", String.valueOf(DEFAULT_MAX_PLAYERS)));
            config.serverPort = Integer.parseInt(props.getProperty("server-port", String.valueOf(DEFAULT_SERVER_PORT)));
            config.serverName = props.getProperty("server-name", DEFAULT_SERVER_NAME);

            System.out.println("Loaded server configuration:");
            System.out.println("  Render Distance: " + config.renderDistance);
            System.out.println("  Max Players: " + config.maxPlayers);
            System.out.println("  Server Port: " + config.serverPort);
            System.out.println("  Server Name: " + config.serverName);

        } catch (IOException e) {
            System.err.println("Error loading server.properties: " + e.getMessage());
            System.out.println("Using default configuration");
        } catch (NumberFormatException e) {
            System.err.println("Invalid number in server.properties: " + e.getMessage());
            System.out.println("Using default values for invalid entries");
        }

        return config;
    }

    /**
     * Save configuration to server.properties file
     */
    public void saveToFile(File file) {
        Properties props = new Properties();
        props.setProperty("render-distance", String.valueOf(renderDistance));
        props.setProperty("max-players", String.valueOf(maxPlayers));
        props.setProperty("server-port", String.valueOf(serverPort));
        props.setProperty("server-name", serverName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "InfCraft Server Configuration");
            System.out.println("Saved server configuration to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving server.properties: " + e.getMessage());
        }
    }

    // Getters
    public int getRenderDistance() {
        return renderDistance;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerName() {
        return serverName;
    }

    // Setters
    public void setRenderDistance(int renderDistance) {
        this.renderDistance = Math.max(2, Math.min(32, renderDistance)); // Clamp between 2-32
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = Math.max(1, maxPlayers);
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
