
package net.infcraft.client.renderer.gui;

import java.util.ArrayList;
import java.util.List;

import net.infcraft.client.config.GameSettings;
import net.infcraft.core.inventory.IInventory;
import net.infcraft.core.inventory.Slot;
import net.infcraft.client.renderer.entity.RenderHelper;
import net.infcraft.client.renderer.entity.RenderItem;

import net.infcraft.core.item.ItemStack;
import org.lwjgl.opengl.GL11;

public abstract class GuiContainer extends GuiScreen {

    private static RenderItem itemRenderer;
    // private ItemStack itemStack; // Removed local itemStack
    public int xSize;
    public int ySize;
    protected List<Slot> inventorySlots;

    public GuiContainer() {
        // this.itemStack = null;
        this.xSize = 176;
        this.ySize = 166;
        this.inventorySlots = new ArrayList<>();
    }

    @Override
    public void drawScreen(final int integer1, final int integer2, final float float3) {
        this.drawDefaultBackground();
        final int n = (this.width - this.xSize) / 2;
        final int n2 = (this.height - this.ySize) / 2;
        this.drawGuiContainerBackgroundLayer(float3);
        GL11.glPushMatrix();
        GL11.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) n, (float) n2, 0.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(32826);
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            final Slot gq = this.inventorySlots.get(i);
            this.inventorySlots(gq);
            if (isMouseOverSlot(gq, integer1, integer2)) {
                GL11.glDisable(2896);
                GL11.glDisable(2929);
                final int xPos = gq.xPos;
                final int yPos = gq.yPos;
                this.drawGradientRect(xPos, yPos, xPos + 16, yPos + 16, -2130706433, -2130706433);
                GL11.glEnable(2896);
                GL11.glEnable(2929);
            }
        }

        ItemStack cursorItem = this.id.player.inventory.getCursorItem();
        if (cursorItem != null) {
            GL11.glTranslatef(0.0f, 0.0f, 32.0f);
            GuiContainer.itemRenderer.drawItemIntoGui(this.fontRenderer, this.id.renderer, cursorItem,
                    integer1 - n - 8, integer2 - n2 - 8);
            GuiContainer.itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.id.renderer, cursorItem,
                    integer1 - n - 8, integer2 - n2 - 8);
        }
        GL11.glDisable(32826);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(2896);
        GL11.glDisable(2929);
        this.drawGuiContainerForegroundLayer();
        GL11.glEnable(2896);
        GL11.glEnable(2929);
        GL11.glPopMatrix();
    }

    protected void drawGuiContainerForegroundLayer() {
    }

    protected abstract void drawGuiContainerBackgroundLayer(final float float1);

    private void inventorySlots(final Slot gq) {
        final IInventory inventory = gq.inventory;
        final int slotIndex = gq.slotIndex;
        final int xPos = gq.xPos;
        final int yPos = gq.yPos;
        final ItemStack stackInSlot = inventory.getStackInSlot(slotIndex);
        if (stackInSlot == null) {
            final int backgroundIconIndex = gq.getBackgroundIconIndex();
            if (backgroundIconIndex >= 0) {
                GL11.glDisable(2896);
                this.id.renderer.bindTexture(this.id.renderer.loadTexture("/assets/gui/items.png"));
                this.drawTexturedModalRect(xPos, yPos, backgroundIconIndex % 16 * 16, backgroundIconIndex / 16 * 16, 16,
                        16);
                GL11.glEnable(2896);
                return;
            }
        }
        GuiContainer.itemRenderer.drawItemIntoGui(this.fontRenderer, this.id.renderer, stackInSlot, xPos, yPos);
        GuiContainer.itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.id.renderer, stackInSlot, xPos,
                yPos);
    }

    private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        mouseX -= guiLeft;
        mouseY -= guiTop;
        return mouseX >= slot.xPos - 1 && mouseX < slot.xPos + 16 + 1 && mouseY >= slot.yPos - 1
                && mouseY < slot.yPos + 16 + 1;
    }

    private Slot getSlotAtPosition(final int x, final int y) {
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            final Slot slot = this.inventorySlots.get(i);
            if (isMouseOverSlot(slot, x, y)) {
                return slot;
            }
        }
        return null;
    }

    @Override
    protected void onMouseButtonPressed(final int x, final int y, final int mouseButtonNumber) {
        System.out.println("Mouse button pressed: " + mouseButtonNumber);
        if (mouseButtonNumber == 0 || mouseButtonNumber == 1) {
            final Slot clickedSlot = this.getSlotAtPosition(x, y);
            int slotId = -1;
            if (clickedSlot != null) {
                slotId = clickedSlot.slotIndex;
            }

            // Send click packet to server
            // Window ID 0 is player inventory
            // Slot ID is the raw slot index (which maps to our window slots)
            // Button 0 = Left Click, 1 = Right Click
            // Shift = 0 (for now)
            // Item = null (server tracks it)

            // Only send if we clicked a slot or if we are clicking outside (drop)
            // For now, just handle slot clicks
            if (clickedSlot != null) {
                net.infcraft.shared.network.packets.PacketWindowClick clickPacket = new net.infcraft.shared.network.packets.PacketWindowClick(
                        0, slotId, mouseButtonNumber, 0, null, (short) 0);

                // Send packet
                if (this.id.getClientNetworkManager() != null) {
                    try {
                        this.id.getClientNetworkManager().sendPacket(clickPacket);
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onMouseButtonReleased(final int x, final int y, final int button) {
        if (button == 0) {
        }
    }

    @Override
    protected void keyTyped(final char character, final int integer) {
        if (integer == 1 || integer == this.id.options.keyBindings.get(GameSettings.PlayerInput.INVENTORY)) {
            // this.id.displayGuiScreen(null);
        }
    }

    @Override
    public void onGuiClosed() {
        // if (this.itemStack != null) {
        // this.id.player.dropPlayerItem(this.itemStack);
        // }
    }

    public void onCraftMatrixChanged(final IInventory kd) {
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    static {
        GuiContainer.itemRenderer = new RenderItem();// (RenderItem)RenderItem.RenderCreeper();
    }
}
