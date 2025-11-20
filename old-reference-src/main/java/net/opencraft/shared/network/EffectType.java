package net.opencraft.shared.network;

/**
 * Enum defining all effect types that can be sent from server to client
 */
public enum EffectType {
    // Particle effects
    PARTICLE_SMOKE(0, true),
    PARTICLE_FIRE(1, true),
    PARTICLE_EXPLOSION(2, true),
    PARTICLE_SPLASH(3, true),
    PARTICLE_BUBBLE(4, true),
    PARTICLE_LAVA(5, true),
    PARTICLE_LARGE_SMOKE(6, true),
    PARTICLE_FLAME(7, true),
    
    // Sound effects - Mobs
    SOUND_ZOMBIE_IDLE(100, false),
    SOUND_ZOMBIE_HURT(101, false),
    SOUND_ZOMBIE_DEATH(102, false),
    SOUND_SKELETON_IDLE(103, false),
    SOUND_SKELETON_HURT(104, false),
    SOUND_SKELETON_DEATH(105, false),
    SOUND_SPIDER_IDLE(106, false),
    SOUND_SPIDER_HURT(107, false),
    SOUND_SPIDER_DEATH(108, false),
    
    // Sound effects - Ambient
    SOUND_FIZZ(200, false),
    SOUND_EXPLOSION(201, false),
    SOUND_BLOCK_DIG(202, false),
    SOUND_BLOCK_STEP(203, false);
    
    private final int id;
    private final boolean isParticle;
    
    EffectType(int id, boolean isParticle) {
        this.id = id;
        this.isParticle = isParticle;
    }
    
    public int getId() {
        return id;
    }
    
    public boolean isParticle() {
        return isParticle;
    }
    
    public boolean isSound() {
        return !isParticle;
    }
    
    /**
     * Get effect type by ID
     */
    public static EffectType getById(int id) {
        for (EffectType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}
