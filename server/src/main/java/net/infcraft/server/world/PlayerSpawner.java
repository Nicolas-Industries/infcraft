
package net.infcraft.server.world;

import net.infcraft.core.entity.EntityPlayer;
import net.infcraft.core.world.chunk.ChunkPosition;

public class PlayerSpawner extends EntitySpawner {
    public final /* synthetic */ EntityPlayer a;

    public PlayerSpawner(final EntityPlayer bb, final int integer, final Class class3, final Class[] arr) {
        super(integer, class3, arr);
        this.a = bb;
    }

    @Override
    protected net.infcraft.core.world.chunk.ChunkPosition a(final ServerWorld fe, final int integer2, final int integer3) {
        return new ChunkPosition(integer2 + fe.rand.nextInt(256) - 128, fe.rand.nextInt(fe.rand.nextInt(fe.rand.nextInt(112) + 8) + 8), integer3 + fe.rand.nextInt(256) - 128);
    }
}
