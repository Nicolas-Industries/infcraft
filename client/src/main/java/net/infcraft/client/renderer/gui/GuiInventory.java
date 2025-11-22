
package net.infcraft.client.renderer.gui;

import net.infcraft.core.inventory.IInventory;
import net.infcraft.core.inventory.InventoryCraftResult;
import net.infcraft.core.inventory.InventoryCrafting;
import net.infcraft.core.inventory.Slot;
import net.infcraft.core.inventory.SlotArmor;
import net.infcraft.core.inventory.SlotCrafting;
import net.infcraft.core.inventory.recipe.CraftingManager;
import net.infcraft.client.renderer.entity.RenderHelper;
import net.infcraft.client.renderer.entity.RenderManager;

import net.infcraft.core.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class GuiInventory extends GuiContainer {

    private InventoryCrafting inventoryCrafting;
    private IInventory iInventory;
    private float xSize_lo;
    private float ySize_lo;

    public GuiInventory(final net.infcraft.core.inventory.IInventory kd) {
        this.inventoryCrafting = new InventoryCrafting(2, 2);
        this.iInventory = new InventoryCraftResult();
        this.allowUserInput = true;
        this.inventorySlots.add(new SlotCrafting(this.inventoryCrafting, this.iInventory, 0, 144, 36));
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                this.inventorySlots.add(new Slot(this.inventoryCrafting, j + i * 2, 88 + j * 18, 26 + i * 18));
            }
        }
        for (int i = 0; i < 4; ++i) {
            final int j = i;
            this.inventorySlots.add(new SlotArmor(kd, kd.getSizeInventory() - 1 - i, 8, 8 + i * 18, j));
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.inventorySlots.add(new Slot(kd, j + (i + 1) * 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.inventorySlots.add(new Slot(kd, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        for (int i = 0; i < this.inventoryCrafting.getSizeInventory(); ++i) {
            final ItemStack stackInSlot = this.inventoryCrafting.getStackInSlot(i);
            if (stackInSlot != null) {
                this.id.player.dropPlayerItem(stackInSlot);
            }
        }
    }
    //@Override TODO: switch to events
    public void onCraftMatrixChanged(final IInventory kd) {
        final int[] arr = new int[9];
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int itemID = -1;
                if (i < 2 && j < 2) {
                    final ItemStack stackInSlot = this.inventoryCrafting.getStackInSlot(i + j * 2);
                    if (stackInSlot != null) {
                        itemID = stackInSlot.itemID;
                    }
                }
                arr[i + j * 3] = itemID;
            }
        }
        this.iInventory.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(arr));
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString2("Crafting", 86, 16, 4210752);
    }

    @Override
    public void drawScreen(final int integer1, final int integer2, final float float3) {
        super.drawScreen(integer1, integer2, float3);
        this.xSize_lo = (float) integer1;
        this.ySize_lo = (float) integer2;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float float1) {
        final int texture = this.id.renderer.loadTexture("/assets/gui/inventory.png");
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.id.renderer.bindTexture(texture);
        final int integer1 = (this.width - this.xSize) / 2;
        final int integer2 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(integer1, integer2, 0, 0, this.xSize, this.ySize);
        GL11.glEnable(32826);
        GL11.glEnable(2903);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (integer1 + 51), (float) (integer2 + 75), 50.0f);
        final float n = 30.0f;
        GL11.glScalef(-n, n, n);
        GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);
        final float renderYawOffset = this.id.player.renderYawOffset;
        final float rotationYaw = this.id.player.rotationYaw;
        final float rotationPitch = this.id.player.rotationPitch;
        final float n2 = integer1 + 51 - this.xSize_lo;
        final float n3 = integer2 + 75 - 50 - this.ySize_lo;
        GL11.glRotatef(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0f, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(-(float) Math.atan((double) (n3 / 40.0f)) * 20.0f, 1.0f, 0.0f, 0.0f);
        this.id.player.renderYawOffset = (float) Math.atan((double) (n2 / 40.0f)) * 20.0f;
        this.id.player.rotationYaw = (float) Math.atan((double) (n2 / 40.0f)) * 40.0f;
        this.id.player.rotationPitch = -(float) Math.atan((double) (n3 / 40.0f)) * 20.0f;
        GL11.glTranslatef(0.0f, 0.0f, 0.0f);
        RenderManager.instance.renderEntityWithPosYaw(this.id.player, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        this.id.player.renderYawOffset = renderYawOffset;
        this.id.player.rotationYaw = rotationYaw;
        this.id.player.rotationPitch = rotationPitch;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(32826);
    }
}
