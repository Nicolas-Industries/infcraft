package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.EffectType;

/**
 * Packet for sending effect events (particles, sounds) from server to client
 */
public class PacketEffect implements IPacket {
    
    private EffectType effectType;
    private double x;
    private double y;
    private double z;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private float volume;
    private float pitch;
    
    // No-arg constructor for deserialization
    public PacketEffect() {
    }
    
    /**
     * Constructor for particle effects
     */
    public PacketEffect(EffectType effectType, double x, double y, double z, 
                       double velocityX, double velocityY, double velocityZ) {
        if (!effectType.isParticle()) {
            throw new IllegalArgumentException("Effect type must be a particle");
        }
        this.effectType = effectType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.volume = 1.0f;
        this.pitch = 1.0f;
    }
    
    /**
     * Constructor for sound effects
     */
    public PacketEffect(EffectType effectType, double x, double y, double z, 
                       float volume, float pitch) {
        if (!effectType.isSound()) {
            throw new IllegalArgumentException("Effect type must be a sound");
        }
        this.effectType = effectType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocityX = 0;
        this.velocityY = 0;
        this.velocityZ = 0;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    @Override
    public int getPacketId() {
        return 0x0A; // Effect packet ID
    }
    
    @Override
    public boolean isServerToClient() {
        return true;
    }
    
    @Override
    public boolean isClientToServer() {
        return false;
    }
    
    // Getters and setters
    public EffectType getEffectType() {
        return effectType;
    }
    
    public void setEffectType(EffectType effectType) {
        this.effectType = effectType;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getZ() {
        return z;
    }
    
    public void setZ(double z) {
        this.z = z;
    }
    
    public double getVelocityX() {
        return velocityX;
    }
    
    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }
    
    public double getVelocityY() {
        return velocityY;
    }
    
    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }
    
    public double getVelocityZ() {
        return velocityZ;
    }
    
    public void setVelocityZ(double velocityZ) {
        this.velocityZ = velocityZ;
    }
    
    public float getVolume() {
        return volume;
    }
    
    public void setVolume(float volume) {
        this.volume = volume;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
