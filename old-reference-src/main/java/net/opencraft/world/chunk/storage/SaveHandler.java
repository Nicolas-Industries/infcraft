
package net.opencraft.client.world.chunk.storage;

import net.opencraft.client.world.World;
import net.opencraft.client.world.IChunkProvider;
import net.opencraft.client.canvas.CanvasIsomPreview;
import net.opencraft.client.world.chunk.ChunkProviderClient;
import java.io.File;

public class SaveHandler extends World {

    public final /* synthetic */ CanvasIsomPreview q;

    public SaveHandler(final CanvasIsomPreview be, final File file, final String string) {
        super(file, string);
        this.q = be;
    }

    @Override
    protected IChunkProvider a(final File file) {
        return new ChunkProviderClient(this, new ChunkLoader(file, false));
    }
}
