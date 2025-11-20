
package net.opencraft.client.renderer.entity;

import java.util.Comparator;

import net.opencraft.client.entity.EntityPlayerSP;
import net.opencraft.client.world.WorldRenderer;
import net.opencraft.core.entity.EntityPlayer;

public class RenderSorter implements Comparator<WorldRenderer> {

    private EntityPlayerSP entity;

    public RenderSorter(final EntityPlayerSP gi) {
        this.entity = gi;
    }

    public int compare(final WorldRenderer dl1, final WorldRenderer dl2) {
        final boolean isInFrustum = dl1.isInFrustum;
        final boolean isInFrustum2 = dl2.isInFrustum;
        if (isInFrustum && !isInFrustum2) {
            return 1;
        }
        if (isInFrustum2 && !isInFrustum) {
            return -1;
        }
        float dist1 = dl1.chunkIndex(this.entity);
        float dist2 = dl2.chunkIndex(this.entity);
        if (dist1 < dist2) {
            return 1;  // Farther chunks come first
        } else if (dist1 > dist2) {
            return -1; // Closer chunks come later
        } else {
            return 0;  // Equal distances
        }
    }
}
