
package net.infcraft.client.entity;

import net.infcraft.client.InfCraft;
import net.infcraft.client.world.ClientWorld;
import net.infcraft.core.blocks.Block;
import net.infcraft.core.entity.EntityPlayer;

public class PlayerController {

    protected final InfCraft mc;
    public boolean field_1064_b;

    // Progressive breaking state
    private int currentBlockX = -1;
    private int currentBlockY = -1;
    private int currentBlockZ = -1;
    private float curBlockDamageMP = 0.0f;
    private float prevBlockDamageMP = 0.0f;
    private float blockHitDelay = 0.0f;
    private boolean isHittingBlock = false;

    public PlayerController(final InfCraft aw) {
        this.field_1064_b = false;
        this.mc = aw;
    }

    public void a() {
    }

    public void func_717_a(final ClientWorld fe) {
    }

    private int soundDelay = 0;

    public void clickBlock(final int xCoord, final int yCoord, final int zCoord) {
        // Send start digging packet
        net.infcraft.shared.network.packets.PacketPlayerDigging packet = new net.infcraft.shared.network.packets.PacketPlayerDigging(
                0, xCoord, yCoord, zCoord, this.mc.objectMouseOver.sideHit);
        try {
            this.mc.getClientNetworkManager().sendPacket(packet);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        int blockId = this.mc.clientWorld.getBlockId(xCoord, yCoord, zCoord);
        if (blockId > 0 && this.curBlockDamageMP == 0.0f) {
            Block block = Block.blocksList[blockId];
            block.onBlockClicked(null, xCoord, yCoord, zCoord, this.mc.player);
        }

        if (blockId > 0 && Block.blocksList[blockId].blockStrength(this.mc.player) >= 1.0f) {
            this.sendBlockRemoved(xCoord, yCoord, zCoord);
        } else {
            this.isHittingBlock = true;
            this.currentBlockX = xCoord;
            this.currentBlockY = yCoord;
            this.currentBlockZ = zCoord;
            this.curBlockDamageMP = 0.0f;
            this.prevBlockDamageMP = 0.0f;
            this.blockHitDelay = 0.0f;
            this.soundDelay = 0;
        }
    }

    public boolean sendBlockRemoved(final int xCoord, final int yCoord, final int zCoord) {
        // Send block broken packet
        net.infcraft.shared.network.packets.PacketPlayerDigging packet = new net.infcraft.shared.network.packets.PacketPlayerDigging(
                2, xCoord, yCoord, zCoord, this.mc.objectMouseOver.sideHit);
        try {
            this.mc.getClientNetworkManager().sendPacket(packet);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        this.mc.effectRenderer.addBlockDestroyEffects(xCoord, yCoord, zCoord);
        final ClientWorld theWorld = this.mc.clientWorld;
        final Block block = Block.blocksList[theWorld.getBlockId(xCoord, yCoord, zCoord)];
        final int blockMetadata = theWorld.getBlockMetadata(xCoord, yCoord, zCoord);
        final boolean setBlockWithNotify = theWorld.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
        if (block != null && setBlockWithNotify) {
            this.mc.sndManager.playSound(block.digSound.digSoundDir(), xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f,
                    (block.digSound.soundVolume() + 1.0f) / 2.0f, block.digSound.soundPitch() * 0.8f);
            // FIXME: cant call this because it requires server context
            // block.onBlockDestroyedByPlayer(theWorld, xCoord, yCoord, zCoord,
            // blockMetadata);
        }
        return setBlockWithNotify;
    }

    public void sendBlockRemoving(final int xCoord, final int yCoord, final int zCoord, final int side) {
        if (!this.isHittingBlock || xCoord != this.currentBlockX || yCoord != this.currentBlockY
                || zCoord != this.currentBlockZ) {
            System.out.println("DEBUG: Switching blocks or starting new dig. isHitting=" + this.isHittingBlock +
                    " current=(" + this.currentBlockX + "," + this.currentBlockY + "," + this.currentBlockZ + ")" +
                    " new=(" + xCoord + "," + yCoord + "," + zCoord + ")");
            this.clickBlock(xCoord, yCoord, zCoord);
            return;
        }

        if (this.blockHitDelay > 0.0f) {
            System.out.println("DEBUG: Block hit delay active: " + this.blockHitDelay);
            this.blockHitDelay--;
            return;
        }

        int blockId = this.mc.clientWorld.getBlockId(xCoord, yCoord, zCoord);
        if (blockId == 0) {
            // Block was removed (by server or another player), reset state
            System.out.println("DEBUG: Block is air, resetting state");
            this.isHittingBlock = false;
            this.curBlockDamageMP = 0.0f;
            this.prevBlockDamageMP = 0.0f;
            return;
        }

        Block block = Block.blocksList[blockId];
        if (this.soundDelay > 0) {
            --this.soundDelay;
        } else {
            String soundName = block.stepSound.stepSoundDir2();
            float volume = (block.stepSound.soundVolume() + 1.0f) / 8.0f;
            float pitch = block.stepSound.soundPitch() * 0.5f;
            System.out.println("DEBUG: Playing step sound: " + soundName + " volume=" + volume + " pitch=" + pitch);
            this.mc.sndManager.playSound(soundName, xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, volume, pitch);
            this.soundDelay = 3;
        }
        float strength = block.blockStrength(this.mc.player);
        this.curBlockDamageMP += strength;

        if (this.curBlockDamageMP % 0.1f < strength) { // Log occasionally
            System.out.println("Block Breaking: " + xCoord + "," + yCoord + "," + zCoord +
                    " Strength: " + strength +
                    " Damage: " + this.curBlockDamageMP);
        }

        if (this.curBlockDamageMP >= 1.0f) {
            System.out.println("DEBUG: Block fully damaged, sending remove packet");
            this.isHittingBlock = false; // Reset state before sending packet
            this.sendBlockRemoved(xCoord, yCoord, zCoord);
            this.curBlockDamageMP = 0.0f;
            this.prevBlockDamageMP = 0.0f;
            this.blockHitDelay = 5.0f;
        }
    }

    public void resetBlockRemoving() {
        this.curBlockDamageMP = 0.0f;
        this.isHittingBlock = false;
    }

    public void setPartialTime(final float float1) {
    }

    public float getBlockReachDistance() {
        return 5.0f;
    }

    public void flipPlayer(final EntityPlayer gi) {
    }

    public void updateController() {
        this.prevBlockDamageMP = this.curBlockDamageMP;
        // check and play music
        this.mc.sndManager.playRandomMusicIfReady();
    }

    public boolean shouldDrawHUD() {
        return true;
    }

    public void func_6473_b(final EntityPlayer gi) {
    }

    public float getCurBlockDamageMP() {
        return this.curBlockDamageMP;
    }
}
