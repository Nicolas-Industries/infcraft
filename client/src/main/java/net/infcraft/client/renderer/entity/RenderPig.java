
package net.infcraft.client.renderer.entity;

import net.infcraft.client.renderer.entity.models.ModelBase;
import net.infcraft.core.entity.EntityPig;

public class RenderPig extends RenderLiving {

    public RenderPig(final ModelBase it1, final ModelBase it2, final float float3) {
        super(it1, float3);
        this.setRenderPassModel(it2);
    }

    protected boolean shouldRenderPass(final EntityPig eVar, final int i) {
        this.loadTexture("/assets/mob/saddle.png");
        return i == 0 && eVar.getSaddled;
    }
}
