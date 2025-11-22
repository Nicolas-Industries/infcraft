package net.infcraft.client.entity;

import net.infcraft.core.entity.EntityPlayer;
import net.infcraft.core.world.World;

public class EntityPlayerRemote extends EntityPlayer {

    public EntityPlayerRemote(World world, String username) {
        super(world);
        this.username = username;
        this.yOffset = 1.62f;
        this.setSize(0.6f, 1.8f);
        this.health = 20;
        this.deathTime = 0;
    }

    @Override
    public void onLivingUpdate() {
        // Update movement and animation but don't handle input like local player
        super.onLivingUpdate();
    }

    @Override
    public void onUpdate() {
        // Don't update as frequently as the local player
        super.onUpdate();
    }

    // Other players don't handle player input directly
    // Their position is controlled by server packets
}