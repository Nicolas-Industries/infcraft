package net.opencraft.shared.network.packets;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet for sending entity state updates from server to client
 * Can contain multiple entities for efficiency
 */
public class PacketEntityState implements IPacket {
    
    public static class EntityData {
        public int entityId;
        public int entityType;
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;
        public double velocityX;
        public double velocityY;
        public double velocityZ;
        public float health;
        public int metadata; // For additional entity-specific data
        
        public EntityData() {
        }
        
        public EntityData(int entityId, int entityType, double x, double y, double z,
                         float yaw, float pitch, double velocityX, double velocityY, double velocityZ,
                         float health, int metadata) {
            this.entityId = entityId;
            this.entityType = entityType;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.velocityZ = velocityZ;
            this.health = health;
            this.metadata = metadata;
        }
    }
    
    private List<EntityData> entities;
    
    // No-arg constructor for deserialization
    public PacketEntityState() {
        this.entities = new ArrayList<>();
    }
    
    public PacketEntityState(List<EntityData> entities) {
        this.entities = entities;
    }
    
    @Override
    public int getPacketId() {
        return 0x0B; // Entity state packet ID
    }
    
    @Override
    public boolean isServerToClient() {
        return true;
    }
    
    @Override
    public boolean isClientToServer() {
        return false;
    }
    
    public List<EntityData> getEntities() {
        return entities;
    }
    
    public void setEntities(List<EntityData> entities) {
        this.entities = entities;
    }
    
    public void addEntity(EntityData entity) {
        this.entities.add(entity);
    }
}
