
package net.infcraft.client.renderer.tileentity;

import net.infcraft.client.renderer.entity.Renderer;
import net.infcraft.client.renderer.font.FontRenderer;
import net.infcraft.core.tileentity.TileEntity;

public abstract class TileEntitySpecialRenderer<T extends TileEntity> {

    protected TileEntityRenderer tileEntityRenderer;

    public abstract void renderTileEntityMobSpawner(final T bk, final double double2, final double double3, final double double4, final float float5);

    protected void bindTextureByName(final String string) {
        final Renderer renderEngine = this.tileEntityRenderer.renderEngine;
        renderEngine.bindTexture(renderEngine.loadTexture(string));
    }

    public void setTileEntityRenderer(final TileEntityRenderer js) {
        this.tileEntityRenderer = js;
    }

    public FontRenderer getFontRenderer() {
        return this.tileEntityRenderer.getFontRenderer();
    }
}
