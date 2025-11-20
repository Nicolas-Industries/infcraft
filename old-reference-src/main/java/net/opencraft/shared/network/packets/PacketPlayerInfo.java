package net.opencraft.shared.network.packets;

/**
 * Packet for player join/leave notifications
 */
public class PacketPlayerInfo implements IPacket {
    private String playerName;
    private int playerId;
    private boolean isJoining;

    public PacketPlayerInfo() {
        // Default constructor for deserialization
    }

    public PacketPlayerInfo(String playerName, int playerId, boolean isJoining) {
        this.playerName = playerName;
        this.playerId = playerId;
        this.isJoining = isJoining;
    }

    @Override
    public int getPacketId() {
        return 0x38; // Player info packet ID (using Minecraft's protocol as reference)
    }

    @Override
    public boolean isServerToClient() {
        return true;  // Server sends player info to clients
    }

    @Override
    public boolean isClientToServer() {
        return false; // This is server to client
    }

    // Getters
    public String getPlayerName() { return playerName; }
    public int getPlayerId() { return playerId; }
    public boolean isJoining() { return isJoining; }

    // Setters for deserialization
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public void setJoining(boolean isJoining) { this.isJoining = isJoining; }
}