
package net.opencraft.client.renderer.entity;

import java.util.Comparator;
import net.opencraft.client.world.WorldRenderer;
import net.opencraft.core.entity.Entity;

public class EntitySorter implements Comparator<WorldRenderer> {

    private Entity a;

    public EntitySorter(final Entity eq) {
        this.a = eq;
    }

    @Override
    public int compare(final WorldRenderer dl1, final WorldRenderer dl2) {
        return (dl1.chunkIndex(this.a) < dl2.chunkIndex(this.a)) ? -1 : 1;
    }
}
