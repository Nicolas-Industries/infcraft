package net.opencraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.opencraft.shared.network.INetworkManager;
import net.opencraft.shared.network.PacketManager;
import net.opencraft.shared.network.packets.IPacket;

/**
 * Network manager for remote client connections
 */
public class RemoteNetworkManager implements INetworkManager {

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Queue<Object> receivedPackets;
    private boolean connectionOpen;
    private boolean hasDataToRead = false;
    private byte[] pendingPacketData;
    private int bytesReceived = 0;
    private int expectedLength = -1;

    public RemoteNetworkManager(Socket socket) throws IOException {
        this.socket = socket;
        this.receivedPackets = new ConcurrentLinkedQueue<>();
        this.connectionOpen = true;

        // Create streams for reading/writing
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void sendPacket(Object packet) throws IOException {
        if (!connectionOpen) {
            throw new IOException("Connection is closed");
        }

        if (packet instanceof IPacket) {
            byte[] serializedPacket = PacketManager.serializePacket((IPacket) packet);

            // Send the length of the packet first, then the packet data
            outputStream.writeInt(serializedPacket.length);
            outputStream.write(serializedPacket);
            outputStream.flush();
        }
    }

    @Override
    public void sendPacket(IPacket packet) throws IOException {
        if (!connectionOpen) {
            throw new IOException("Connection is closed");
        }

        byte[] serializedPacket = PacketManager.serializePacket(packet);

        // Send the length of the packet first, then the packet data
        outputStream.writeInt(serializedPacket.length);
        outputStream.write(serializedPacket);
        outputStream.flush();
    }

    @Override
    public boolean isConnectionOpen() {
        return connectionOpen && !socket.isClosed() && socket.isConnected();
    }

    @Override
    public void closeConnection() {
        connectionOpen = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    @Override
    public Object getReceivedPacket() {
        return receivedPackets.poll();
    }

    @Override
    public void processNetworkData() {
        // Non-blocking read to check for available data
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

                        // Reset for next packet
                        expectedLength = -1;
                        pendingPacketData = null;
                        bytesReceived = 0;
                    }
                }
            }
        } catch (IOException e) {
            // Connection might be closed or error occurred
            connectionOpen = false;
        } catch (ClassNotFoundException e) {
            System.err.println("Unknown packet type received: " + e.getMessage());
        }
    }

    @Override
    public boolean isLocal() {
        return false; // Remote connections are not local
    }
}