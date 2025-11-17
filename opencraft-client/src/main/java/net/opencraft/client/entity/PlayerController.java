
package net.opencraft.client.entity;

import net.opencraft.client.OpenCraft;
import net.opencraft.client.world.ClientWorld;
import net.opencraft.core.blocks.Block;
import net.opencraft.core.entity.EntityPlayer;

public class PlayerController {

    protected final OpenCraft mc;
    public boolean field_1064_b;

    public PlayerController(final OpenCraft aw) {
        this.field_1064_b = false;
        this.mc = aw;
    }

    public void a() {
    }

    public void func_717_a(final ClientWorld fe) {
    }

    public void clickBlock(final int xCoord, final int yCoord, final int zCoord) {
        this.sendBlockRemoved(xCoord, yCoord, zCoord);
    }

    public boolean sendBlockRemoved(final int xCoord, final int yCoord, final int zCoord) {
        this.mc.effectRenderer.addBlockDestroyEffects(xCoord, yCoord, zCoord);
        final ClientWorld theWorld = this.mc.clientWorld;
        final Block block = Block.blocksList[theWorld.getBlockId(xCoord, yCoord, zCoord)];
        final int blockMetadata = theWorld.getBlockMetadata(xCoord, yCoord, zCoord);
        final boolean setBlockWithNotify = theWorld.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
        if (block != null && setBlockWithNotify) {
            this.mc.sndManager.playSound(block.digSound.digSoundDir(), xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, (block.stepSound.soundVolume() + 1.0f) / 2.0f, block.stepSound.soundPitch() * 0.8f);
            //FIXME: cant call this because it requires server context
            //block.onBlockDestroyedByPlayer(theWorld, xCoord, yCoord, zCoord, blockMetadata);
        }
        return setBlockWithNotify;
    }

    public void sendBlockRemoving(final int integer1, final int integer2, final int integer3, final int integer4) {
    }

    public void resetBlockRemoving() {
    }

    public void setPartialTime(final float float1) {
    }

    public float getBlockReachDistance() {
        return 5.0f;
    }

    public void flipPlayer(final EntityPlayer gi) {
    }

    public void updateController() {
    }

    public boolean shouldDrawHUD() {
        return true;
    }

    public void func_6473_b(final EntityPlayer gi) {
    }
}
