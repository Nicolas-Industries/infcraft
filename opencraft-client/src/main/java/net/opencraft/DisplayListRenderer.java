package net.opencraft;

import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * Manages and renders a batch of OpenGL display lists at a given world position.
 */
public class DisplayListRenderer {

    private int worldX, worldY, worldZ;
    private float offsetX, offsetY, offsetZ;
    private final IntBuffer displayListBuffer;
    private boolean active;
    private boolean bufferFlipped;

    public DisplayListRenderer() {
        this.displayListBuffer = BufferUtils.createIntBuffer(0x10000); // 65536 capacity
        this.active = false;
        this.bufferFlipped = false;
    }

    /**
     * Begin a new batch of display lists at the given position and offset.
     */
    public void beginBatch(int x, int y, int z, double offsetX, double offsetY, double offsetZ) {
        this.active = true;
        this.displayListBuffer.clear();
        this.worldX = x;
        this.worldY = y;
        this.worldZ = z;
        this.offsetX = (float) offsetX;
        this.offsetY = (float) offsetY;
        this.offsetZ = (float) offsetZ;
    }

    /**
     * Check if this renderer is active at the given world coordinates.
     */
    public boolean isActiveAt(int x, int y, int z) {
        return this.active && x == this.worldX && y == this.worldY && z == this.worldZ;
    }

    /**
     * Add a display list ID to the buffer.
     */
    public void addDisplayList(int listId) {
        this.displayListBuffer.put(listId);
        if (this.displayListBuffer.remaining() == 0) {
            this.render();
        }
    }

    /**
     * Render all queued display lists.
     */
    public void render() {
        if (!this.active) {
            return;
        }
        if (!this.bufferFlipped) {
            this.displayListBuffer.flip();
            this.bufferFlipped = true;
        }
        if (this.displayListBuffer.remaining() > 0) {
            GL11.glPushMatrix();
            GL11.glTranslatef(this.worldX - this.offsetX,
                    this.worldY - this.offsetY,
                    this.worldZ - this.offsetZ);
            GL11.glCallLists(this.displayListBuffer);
            GL11.glPopMatrix();
        }
    }

    /**
     * Reset the renderer state.
     */
    public void reset() {
        this.active = false;
        this.bufferFlipped = false;
    }
}
