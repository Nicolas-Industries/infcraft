package net.opencraft.shared.network;

public enum ConnectionState {
    HANDSHAKING,
    CONFIGURATION,
    PLAY,
    STATUS,
    LOGIN;

    public static ConnectionState getById(int id) {
        switch (id) {
            case 0:
                return HANDSHAKING;
            case 1:
                return CONFIGURATION; // Or Status depending on context, but for Handshake packet next state:
                                      // 1=Config, 2=Status
            case 2:
                return STATUS;
            case 3:
                return LOGIN; // Not strictly in spec but useful? Spec says Handshake -> Config or Status.
            default:
                return HANDSHAKING;
        }
    }
}
