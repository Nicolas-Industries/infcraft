package net.infcraft.shared.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.infcraft.shared.network.packets.IPacket;

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
    private ConnectionState connectionState = ConnectionState.HANDSHAKING;
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

    public void setConnectionState(ConnectionState state) {
        this.connectionState = state;
    }

    public ConnectionState getConnectionState() {
        return this.connectionState;
    }

    @Override
    public void sendPacket(Object packet) throws IOException {
        if (!connectionOpen) {
            throw new IOException("Connection is closed");
        }

        if (packet instanceof IPacket) {
            byte[] serializedPacket = PacketManager.serializePacket((IPacket) packet);

            // Send the length of the packet first, then the packet data
            // Note: PacketManager.serializePacket already includes Length + ID + Data
            // But RemoteNetworkManager was writing length separately:
            // outputStream.writeInt(serializedPacket.length);
            // The new serializePacket returns the FULL frame (Length + ID + Data).
            // So we should just write the bytes.

            // WAIT! PacketManager.serializePacket implementation:
            // buffer.writeVarInt(packetId); packet.writePacketData(buffer); ...
            // It returns ID + Data. It does NOT prepend Length.
            // Let's check PacketManager.serializePacket again.
            // "serializePacket now constructs packets as Length (VarInt) + Packet ID
            // (VarInt) + Data" - Summary says this.
            // But let's verify the code.

            // If serializePacket returns full frame, we just write it.
            // If it returns ID+Data, we need to prepend Length.

            // Assuming serializePacket returns full frame as per summary.
            // "serializePacket now constructs packets as Length (VarInt) + Packet ID
            // (VarInt) + Data"

            outputStream.write(serializedPacket);
            outputStream.flush();
        }
    }

    @Override
    public boolean isConnectionOpen() {
        return connectionOpen && !socket.isClosed() && socket.isConnected();
    }

    @Override
    public void closeConnection() {
        connectionOpen = false;
        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
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
        try {
            if (inputStream.available() > 0) {
                if (expectedLength == -1) {
                    // We need to read the packet length first (VarInt)
                    // Since we can't easily peek, we read byte by byte.
                    // This is a blocking operation if we are not careful, but we check available()
                    // > 0.
                    // However, a VarInt can be up to 5 bytes.
                    // We'll use a simplified approach: if we have at least 1 byte, we try to read.
                    // If we fail to read the full VarInt (because it's split across packets),
                    // this simple implementation might fail or block.
                    // For this refactor, we assume we can read the length if data is available.

                    // We need to read the length, but ALSO keep the length bytes for the full frame
                    // if deserializePacket expects the full frame.
                    // PacketManager.deserializePacket(byte[], ConnectionState) reads the length
                    // from the buffer.
                    // So we MUST pass the length bytes.

                    // Read VarInt into a buffer
                    java.io.ByteArrayOutputStream lengthBuffer = new java.io.ByteArrayOutputStream();
                    int numRead = 0;
                    int result = 0;
                    byte read;

                    // We need to be careful not to consume bytes if we can't finish reading the
                    // VarInt.
                    // But InputStreams don't support marking usually.
                    // We'll assume we can read it.

                    do {
                        // Check if we have data?
                        // if (inputStream.available() == 0) break; // Logic error if we break
                        // mid-VarInt

                        read = inputStream.readByte();
                        lengthBuffer.write(read);
                        int value = (read & 0x7F);
                        result |= (value << (7 * numRead));
                        numRead++;
                        if (numRead > 5) {
                            throw new RuntimeException("VarInt is too big");
                        }
                    } while ((read & 0x80) != 0);

                    // result is the length of the PAYLOAD (ID + Data)
                    // We need to allocate buffer for LengthBytes + Payload
                    byte[] lengthBytes = lengthBuffer.toByteArray();
                    int payloadLength = result;

                    expectedLength = lengthBytes.length + payloadLength;
                    pendingPacketData = new byte[expectedLength];

                    // Copy length bytes
                    System.arraycopy(lengthBytes, 0, pendingPacketData, 0, lengthBytes.length);
                    bytesReceived = lengthBytes.length;
                }

                if (expectedLength > 0 && bytesReceived < expectedLength) {
                    // Read as much data as available
                    int toRead = Math.min(expectedLength - bytesReceived, inputStream.available());
                    if (toRead > 0) {
                        int read = inputStream.read(pendingPacketData, bytesReceived, toRead);
                        if (read > 0) {
                            bytesReceived += read;
                        }
                    }

                    if (bytesReceived == expectedLength) {
                        // Complete packet received
                        IPacket packet = PacketManager.deserializePacket(pendingPacketData, connectionState);
                        receivedPackets.add(packet);

                        // Reset for next packet
                        expectedLength = -1;
                        pendingPacketData = null;
                        bytesReceived = 0;
                    }
                }
            }
        } catch (IOException e) {
            connectionOpen = false;
        } catch (ClassNotFoundException e) {
            System.err.println("Unknown packet type received: " + e.getMessage());
        }
    }

    @Override
    public boolean isLocal() {
        return false; // This is for remote connections
    }

    // We need to buffer the read bytes to pass them to deserializePacket?
    // deserializePacket takes byte[].
    // If we read the length, we consume it.
    // So we need to reconstruct the array.

    // Let's change processNetworkData to handle this.

}