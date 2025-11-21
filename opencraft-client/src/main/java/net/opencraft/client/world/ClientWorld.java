
package net.opencraft.client.world;

import static org.joml.Math.*;

import java.io.*;
import java.util.*;

import net.opencraft.client.world.chunk.Chunk;
import net.opencraft.client.world.chunk.ChunkCache;
import net.opencraft.client.world.chunk.ChunkProviderClient;
import net.opencraft.client.world.chunk.storage.ChunkLoader;
import net.opencraft.core.blocks.Block;
import net.opencraft.core.blocks.material.Material;
import net.opencraft.core.enums.EnumSkyBlock;
import net.opencraft.core.input.MovingObjectPosition;
import net.opencraft.core.entity.Entity;
import net.opencraft.core.nbt.NBTTagCompound;
import net.opencraft.core.pathfinder.PathEntity;
import net.opencraft.core.physics.AABB;
import net.opencraft.client.renderer.gui.IProgressUpdate;
import net.opencraft.core.tileentity.TileEntity;
import net.opencraft.core.util.CompressedStreamTools;
import net.opencraft.core.util.Mth;
import net.opencraft.core.util.Vec3;
import net.opencraft.core.world.NextTickListEntry;
import net.opencraft.core.world.World;

public class ClientWorld implements World {

    public static float[] lightBrightnessTable;
    private List lightingToUpdate;
    private List loadedEntityList;
    private List unloadedEntityList;
    private TreeSet scheduledTickTreeSet;
    private Set scheduledTickSet;
    public List loadedTileEntityList;
    public long getWorldTime;
    private long v;
    private long w;
    private long field_1019_F;
    public int skylightSubtracted;
    private int randInt;
    private int zz;
    public boolean editingBlocks;
    public Entity player;
    public int difficultySetting;
    public Object h;
    public Random rand;
    public int x;
    public int y;
    public int z;
    public boolean isNewWorld;
    public List worldAccesses;
    private IChunkProvider chunkProvider;
    public long n;
    private NBTTagCompound D;
    public long setSizeOnDisk;
    public final String p;
    private List field_1012_M;
    private File C;

    static {
        ClientWorld.lightBrightnessTable = new float[16];
        final float n = 0.05f;
        for (int i = 0; i <= 15; ++i) {
            final float n2 = 1.0f - i / 15.0f;
            ClientWorld.lightBrightnessTable[i] = (1.0f - n2) / (n2 * 3.0f + 1.0f) * (1.0f - n) + n;
        }
    }

    public ClientWorld(final File file, final String string) {
        this(file, string, new Random().nextLong());
    }

    public ClientWorld(final File file, final String string, final long long3) {
        this.lightingToUpdate = new ArrayList();
        this.loadedEntityList = new ArrayList();
        this.unloadedEntityList = new ArrayList();
        this.scheduledTickTreeSet = new TreeSet();
        this.scheduledTickSet = new HashSet();
        this.loadedTileEntityList = new ArrayList();
        this.getWorldTime = 0L;
        this.v = 8961023L;
        this.w = 12638463L;
        this.field_1019_F = 16777215L;
        this.skylightSubtracted = 0;
        this.randInt = new Random().nextInt();
        this.zz = 1013904223;
        this.editingBlocks = false;
        this.rand = new Random();
        this.isNewWorld = false;
        this.worldAccesses = new ArrayList();
        this.n = 0L;
        this.setSizeOnDisk = 0L;
        this.field_1012_M = new ArrayList();
        this.p = string;
        file.mkdirs();
        (this.C = new File(file, string)).mkdirs();
        final File file2 = new File(this.C, "level.dat");
        this.isNewWorld = !file2.exists();
        if (file2.exists()) {
            try {
                final NBTTagCompound compoundTag = CompressedStreamTools
                        .loadGzippedCompoundFromOutputStream(new FileInputStream(file2)).getCompoundTag("Data");
                this.n = compoundTag.getLong("RandomSeed");
                this.x = compoundTag.getInteger("SpawnX");
                this.y = compoundTag.getInteger("SpawnY");
                this.z = compoundTag.getInteger("SpawnZ");
                this.getWorldTime = compoundTag.getLong("Time");
                this.setSizeOnDisk = compoundTag.getLong("SizeOnDisk");
                this.D = compoundTag.getCompoundTag("Player");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        boolean b = false;
        if (this.n == 0L) {
            this.n = long3;
            b = true;
        }
        this.chunkProvider = this.a(this.C);
        if (b) {
            // Client should not generate spawn point - it will be received from the server
            // The server handles spawn point generation in ServerWorld
            this.x = 0;
            this.y = 64;
            this.z = 0;
            // REMOVED: Infinite loop that was causing the game to freeze
            // while (!this.e(this.x, this.z)) {
            // this.x += this.rand.nextInt(64) - this.rand.nextInt(64);
            // this.z += this.rand.nextInt(64) - this.rand.nextInt(64);
            // }
        }
        this.calculateInitialSkylight();
    }

    protected IChunkProvider a(final File file) {
        // Client should ONLY receive chunks from the server via packets
        // Never generate or load chunks locally - that's the server's job
        IChunkLoader chunkLoader = new ChunkLoader();
        return new ChunkProviderClient(this, chunkLoader);
    }

    /**
     * Get the chunk provider for this world
     * Used by ClientNetworkManager to load chunks from packets
     */
    public IChunkProvider getChunkProvider() {
        return this.chunkProvider;
    }

    public void a() {
        while (this.f(this.x, this.z) == 0) {
            this.x += this.rand.nextInt(8) - this.rand.nextInt(8);
            this.z += this.rand.nextInt(8) - this.rand.nextInt(8);
        }
    }

    private boolean e(final int integer1, final int integer2) {
        return this.f(integer1, integer2) == Block.sand.blockID;
    }

    private int f(final int integer1, final int integer2) {
        int yCoord;
        for (yCoord = 63; this.getBlockId(integer1, yCoord + 1, integer2) != 0; ++yCoord) {
        }
        return this.getBlockId(integer1, yCoord, integer2);
    }

    public void spawnPlayerWithLoadedChunks() {
        try {
            if (this.D != null) {
                // In integrated server mode, player data may come from server
                // rather than from local save files
                if (net.opencraft.client.OpenCraft.oc != null
                        && net.opencraft.client.OpenCraft.oc.isMultiplayerWorld()) {
                    // For integrated server, we might want to get player data from server
                    // For now, initialize with basic player data
                    this.player.preparePlayerToSpawn();
                } else {
                    // In standalone mode, read from saved data
                    this.player.readFromNBT(this.D);
                }
                this.D = null;
            } else {
                // No saved data, prepare player with defaults
                this.player.preparePlayerToSpawn();
            }
            this.entityJoinedWorld(this.player);
        } catch (Exception ex) {
            System.err.println("Error spawning player: " + ex.getMessage());
            ex.printStackTrace();
            // Still add the player even if there was an error reading saved data
            this.player.preparePlayerToSpawn();
            this.entityJoinedWorld(this.player);
        }
    }

    @Override
    public int getBlockId(final int xCoord, final int yCoord, final int zCoord) {
        if (xCoord < -32000000 || zCoord < -32000000 || xCoord >= 32000000 || zCoord > 32000000) {
            return 0;
        }
        if (yCoord < 0) {
            return 0;
        }
        if (yCoord >= 128) {
            return 0;
        }
        return this.getChunkFromChunkCoords(xCoord >> 4, zCoord >> 4).getBlockID(xCoord & 0xF, yCoord, zCoord & 0xF);
    }

    public boolean blockExists(final int xCoord, final int yCoord, final int zCoord) {
        return yCoord >= 0 && yCoord < 128 && this.chunkExists(xCoord >> 4, zCoord >> 4);
    }

    public boolean checkChunksExist(int xCoordNegative, int yCoordNegative, int zCoordNegative, int xCoordPositive,
            int yCoordPositive, int zCoordPositive) {
        if (yCoordPositive < 0 || yCoordNegative >= 128) {
            return false;
        }
        xCoordNegative >>= 4;
        yCoordNegative >>= 4;
        zCoordNegative >>= 4;
        xCoordPositive >>= 4;
        yCoordPositive >>= 4;
        zCoordPositive >>= 4;
        for (int i = xCoordNegative; i <= xCoordPositive; ++i) {
            for (int j = zCoordNegative; j <= zCoordPositive; ++j) {
                if (!this.chunkExists(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean chunkExists(final int xCoord, final int zCoord) {
        return this.chunkProvider.chunkExists(xCoord, zCoord);
    }

    public Chunk getChunkFromChunkCoords(final int nya1, final int nya2) {
        return this.chunkProvider.provideChunk(nya1, nya2);
    }

    public boolean setBlock(final int xCoord, final int yCoord, final int zCoord, final int blockid) {
        if (xCoord < -32000000 || zCoord < -32000000 || xCoord >= 32000000 || zCoord > 32000000) {
            return false;
        }
        if (yCoord < 0) {
            return false;
        }
        if (yCoord >= 128) {
            return false;
        }
        final Chunk chunkFromChunkCoords = this.getChunkFromChunkCoords(xCoord >> 4, zCoord >> 4);
        return chunkFromChunkCoords.setBlockID(xCoord & 0xF, yCoord, zCoord & 0xF, blockid);
    }

    @Override
    public Material getBlockMaterial(final int nya1, final int nya2, final int nya3) {
        final int blockId = this.getBlockId(nya1, nya2, nya3);
        if (blockId == 0) {
            return Material.AIR;
        }
        return Block.blocksList[blockId].blockMaterial;
    }

    @Override
    public int getBlockMetadata(int xCoord, final int yCoord, int zCoord) {
        if (xCoord < -32000000 || zCoord < -32000000 || xCoord >= 32000000 || zCoord > 32000000) {
            return 0;
        }
        if (yCoord < 0) {
            return 0;
        }
        if (yCoord >= 128) {
            return 0;
        }
        final Chunk chunkFromChunkCoords = this.getChunkFromChunkCoords(xCoord >> 4, zCoord >> 4);
        xCoord &= 0xF;
        zCoord &= 0xF;
        return chunkFromChunkCoords.getBlockMetadata(xCoord, yCoord, zCoord);
    }

    public boolean setBlockMetadata(int xCoord, final int yCoord, int zCoord, final int metadataValue) {
        if (xCoord < -32000000 || zCoord < -32000000 || xCoord >= 32000000 || zCoord > 32000000) {
            return false;
        }
        if (yCoord < 0) {
            return false;
        }
        if (yCoord >= 128) {
            return false;
        }
        final Chunk chunkFromChunkCoords = this.getChunkFromChunkCoords(xCoord >> 4, zCoord >> 4);
        xCoord &= 0xF;
        zCoord &= 0xF;
        chunkFromChunkCoords.setBlockMetadata(xCoord, yCoord, zCoord, metadataValue);
        return true;
    }

    public boolean setBlockWithNotify(final int xCoord, final int yCoord, final int zCoord, final int blockid) {
        if (this.setBlock(xCoord, yCoord, zCoord, blockid)) {
            this.notifyBlockChange(xCoord, yCoord, zCoord, blockid);
            return true;
        }
        return false;
    }

    @Override
    public boolean setBlockAndMetadata(int x, int y, int z, int blockid, int metadata) {
        return false;
    }

    @Override
    public boolean setBlockAndMetadataWithNotify(int x, int y, int z, int blockid, int metadata) {
        return false;
    }

    private void notifyBlockChange(final int integer1, final int integer2, final int integer3, final int integer4) {
        for (int i = 0; i < this.worldAccesses.size(); ++i) {
            ((IWorldAccess) this.worldAccesses.get(i)).markBlockAndNeighborsNeedsUpdate(integer1, integer2, integer3);
        }
        this.notifyBlocksOfNeighborChange(integer1, integer2, integer3, integer4);
    }

    public void markBlocksDirtyVertical(final int nya1, final int nya2, int nya3, int nya4) {
        if (nya3 > nya4) {
            final int n = nya4;
            nya4 = nya3;
            nya3 = n;
        }
        this.markBlocksDirty(nya1, nya3, nya2, nya1, nya4, nya2);
    }

    public void markBlocksDirty(final int nya1, final int nya2, final int nya3, final int nya4, final int nya5,
            final int nya6) {
        for (int i = 0; i < this.worldAccesses.size(); ++i) {
            ((IWorldAccess) this.worldAccesses.get(i)).markBlockRangeNeedsUpdate(nya1, nya2, nya3, nya4, nya5, nya6);
        }
    }

    public void notifyBlocksOfNeighborChange(final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        this.notifyBlockOfNeighborChange(xCoord - 1, yCoord, zCoord, nya4);
        this.notifyBlockOfNeighborChange(xCoord + 1, yCoord, zCoord, nya4);
        this.notifyBlockOfNeighborChange(xCoord, yCoord - 1, zCoord, nya4);
        this.notifyBlockOfNeighborChange(xCoord, yCoord + 1, zCoord, nya4);
        this.notifyBlockOfNeighborChange(xCoord, yCoord, zCoord - 1, nya4);
        this.notifyBlockOfNeighborChange(xCoord, yCoord, zCoord + 1, nya4);
    }

    public void notifyBlockOfNeighborChange(final int nya1, final int nya2, final int nya3, final int nya4) {
        if (this.editingBlocks) {
            return;
        }
        final Block block = Block.blocksList[this.getBlockId(nya1, nya2, nya3)];
        if (block != null) {
            // In client-server architecture, neighbor block changes should be handled by
            // the server
            // The client shouldn't directly call onNeighborBlockChange with itself
            // Instead, the client should send an update to the server
            // The original instruction was to replace packet sending logic in setBlock,
            // but setBlock already has chunk update logic.
            // This section in notifyBlockOfNeighborChange contains packet sending logic.
            // Assuming the intent was to remove this client-side packet sending logic
            // and not replace it with anything specific here, as actual chunk updates
            // are handled elsewhere (e.g., setBlock).
            // If a server-side update is needed, it would be initiated by the client's
            // action, not by this local neighbor change notification.
        }
    }

    @Override
    public boolean canBlockSeeTheSky(int x, int y, int z) {
        return false;
    }

    public int getBlockLightValue(final int xCoord, final int yCoord, final int zCoord) {
        return this.getBlockLightValue_do(xCoord, yCoord, zCoord, true);
    }

    public int getBlockLightValue_do(int xCoord, final int yCoord, int zCoord, final boolean nya4) {
        if (xCoord < -32000000 || zCoord < -32000000 || xCoord >= 32000000 || zCoord > 32000000) {
            return 15;
        }
        if (nya4) {
            final int blockId = this.getBlockId(xCoord, yCoord, zCoord);
            if (blockId == Block.slabSingle.blockID || blockId == Block.tilledField.blockID) {
                int blockLightValue_do = this.getBlockLightValue_do(xCoord, yCoord + 1, zCoord, false);
                final int blockLightValue_do2 = this.getBlockLightValue_do(xCoord + 1, yCoord, zCoord, false);
                final int blockLightValue_do3 = this.getBlockLightValue_do(xCoord - 1, yCoord, zCoord, false);
                final int blockLightValue_do4 = this.getBlockLightValue_do(xCoord, yCoord, zCoord + 1, false);
                final int blockLightValue_do5 = this.getBlockLightValue_do(xCoord, yCoord, zCoord - 1, false);
                if (blockLightValue_do2 > blockLightValue_do) {
                    blockLightValue_do = blockLightValue_do2;
                }
                if (blockLightValue_do3 > blockLightValue_do) {
                    blockLightValue_do = blockLightValue_do3;
                }
                if (blockLightValue_do4 > blockLightValue_do) {
                    blockLightValue_do = blockLightValue_do4;
                }
                if (blockLightValue_do5 > blockLightValue_do) {
                    blockLightValue_do = blockLightValue_do5;
                }
                return blockLightValue_do;
            }
        }
        if (yCoord < 0) {
            return 0;
        }
        if (yCoord >= 128) {
            int blockId = 15 - this.skylightSubtracted;
            if (blockId < 0) {
                blockId = 0;
            }
            return blockId;
        }
        final Chunk chunkFromChunkCoords = this.getChunkFromChunkCoords(xCoord >> 4, zCoord >> 4);
        xCoord &= 0xF;
        zCoord &= 0xF;
        return chunkFromChunkCoords.getBlockLightValue(xCoord, yCoord, zCoord, this.skylightSubtracted);
    }

    public boolean canExistingBlockSeeTheSky(int xCoord, final int yCoord, int zCoord) {
        if (xCoord < -32000000 || zCoord < -32000000 || xCoord >= 32000000 || zCoord > 32000000) {
            return false;
        }
        if (yCoord < 0) {
            return false;
        }
        if (yCoord >= 128) {
            return true;
        }
        if (!this.chunkExists(xCoord >> 4, zCoord >> 4)) {
            return false;
        }
        final Chunk chunkFromChunkCoords = this.getChunkFromChunkCoords(xCoord >> 4, zCoord >> 4);
        xCoord &= 0xF;
        zCoord &= 0xF;
        return chunkFromChunkCoords.canBlockSeeTheSky(xCoord, yCoord, zCoord);
    }

    public int getHeightValue(final int xCoord, final int zCoord) {
        if (xCoord < -32000000 || zCoord < -32000000 || xCoord >= 32000000 || zCoord > 32000000) {
            return 0;
        }
        if (!this.chunkExists(xCoord >> 4, zCoord >> 4)) {
            return 0;
        }
        return this.getChunkFromChunkCoords(xCoord >> 4, zCoord >> 4).getHeightValue(xCoord & 0xF, zCoord & 0xF);
    }

    public void neighborLightPropagationChanged(final EnumSkyBlock enumSkyBlock, final int xCoord, final int yCoord,
            final int zCoord, int nya4) {
        if (!this.blockExists(xCoord, yCoord, zCoord)) {
            return;
        }
        if (enumSkyBlock == EnumSkyBlock.Sky) {
            if (this.canExistingBlockSeeTheSky(xCoord, yCoord, zCoord)) {
                nya4 = 15;
            }
        } else if (enumSkyBlock == EnumSkyBlock.Block) {
            final int blockId = this.getBlockId(xCoord, yCoord, zCoord);
            if (Block.lightValue[blockId] > nya4) {
                nya4 = Block.lightValue[blockId];
            }
        }
        if (this.getSavedLightValue(enumSkyBlock, xCoord, yCoord, zCoord) != nya4) {
            this.scheduleLightingUpdate(enumSkyBlock, xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        }
    }

    public int getSavedLightValue(final EnumSkyBlock enumSkyBlock, final int xCoord, final int yCoord,
            final int zCoord) {
        if (yCoord < 0 || yCoord >= 128 || xCoord < -32000000 || zCoord < -32000000 || xCoord >= 32000000
                || zCoord > 32000000) {
            return enumSkyBlock.defaultLightValue;
        }
        final int n = xCoord >> 4;
        final int n2 = zCoord >> 4;
        if (!this.chunkExists(n, n2)) {
            return 0;
        }
        return this.getChunkFromChunkCoords(n, n2).getSavedLightValue(enumSkyBlock, xCoord & 0xF, yCoord, zCoord & 0xF);
    }

    public void setLightValue(final EnumSkyBlock enumSkyBlock, final int xCoord, final int yCoord, final int zCoord,
            final int nya4) {
        if (xCoord < -32000000 || zCoord < -32000000 || xCoord >= 32000000 || zCoord > 32000000) {
            return;
        }
        if (yCoord < 0) {
            return;
        }
        if (yCoord >= 128) {
            return;
        }
        if (!this.chunkExists(xCoord >> 4, zCoord >> 4)) {
            return;
        }
        this.getChunkFromChunkCoords(xCoord >> 4, zCoord >> 4).setLightValue(enumSkyBlock, xCoord & 0xF, yCoord,
                zCoord & 0xF, nya4);
        for (int i = 0; i < this.worldAccesses.size(); ++i) {
            ((IWorldAccess) this.worldAccesses.get(i)).markBlockAndNeighborsNeedsUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public float getLightBrightness(final int nya1, final int nya2, final int nya3) {
        return ClientWorld.lightBrightnessTable[this.getBlockLightValue(nya1, nya2, nya3)];
    }

    public MovingObjectPosition rayTraceBlocks(final Vec3 var1, final Vec3 var2) {
        return this.rayTraceBlocks_do_do(var1, var2, false);
    }

    public MovingObjectPosition rayTraceBlocks_do_do(final Vec3 var1, final Vec3 var2, final boolean var3) {
        if (Double.isNaN(var1.x) || Double.isNaN(var1.y) || Double.isNaN(var1.z)) {
            return null;
        }
        if (Double.isNaN(var2.x) || Double.isNaN(var2.y) || Double.isNaN(var2.z)) {
            return null;
        }
        final int floor_double = Mth.floor_double(var2.x);
        final int floor_double2 = Mth.floor_double(var2.y);
        final int floor_double3 = Mth.floor_double(var2.z);
        int floor_double4 = Mth.floor_double(var1.x);
        int floor_double5 = Mth.floor_double(var1.y);
        int floor_double6 = Mth.floor_double(var1.z);
        int n = 20;
        while (n-- >= 0) {
            if (Double.isNaN(var1.x) || Double.isNaN(var1.y) || Double.isNaN(var1.z)) {
                return null;
            }
            if (floor_double4 == floor_double && floor_double5 == floor_double2 && floor_double6 == floor_double3) {
                return null;
            }
            double xCoord = 999.0;
            double yCoord = 999.0;
            double zCoord = 999.0;
            if (floor_double > floor_double4) {
                xCoord = floor_double4 + 1.0;
            }
            if (floor_double < floor_double4) {
                xCoord = floor_double4 + 0.0;
            }
            if (floor_double2 > floor_double5) {
                yCoord = floor_double5 + 1.0;
            }
            if (floor_double2 < floor_double5) {
                yCoord = floor_double5 + 0.0;
            }
            if (floor_double3 > floor_double6) {
                zCoord = floor_double6 + 1.0;
            }
            if (floor_double3 < floor_double6) {
                zCoord = floor_double6 + 0.0;
            }
            double n2 = 999.0;
            double n3 = 999.0;
            double n4 = 999.0;
            final double n5 = var2.x - var1.x;
            final double n6 = var2.y - var1.y;
            final double n7 = var2.z - var1.z;
            if (xCoord != 999.0) {
                n2 = (xCoord - var1.x) / n5;
            }
            if (yCoord != 999.0) {
                n3 = (yCoord - var1.y) / n6;
            }
            if (zCoord != 999.0) {
                n4 = (zCoord - var1.z) / n7;
            }
            int n8;
            if (n2 < n3 && n2 < n4) {
                if (floor_double > floor_double4) {
                    n8 = 4;
                } else {
                    n8 = 5;
                }
                var1.x = xCoord;
                var1.y += n6 * n2;
                var1.z += n7 * n2;
            } else if (n3 < n4) {
                if (floor_double2 > floor_double5) {
                    n8 = 0;
                } else {
                    n8 = 1;
                }
                var1.x += n5 * n3;
                var1.y = yCoord;
                var1.z += n7 * n3;
            } else {
                if (floor_double3 > floor_double6) {
                    n8 = 2;
                } else {
                    n8 = 3;
                }
                var1.x += n5 * n4;
                var1.y += n6 * n4;
                var1.z = zCoord;
            }
            final Vec3 vector;
            final Vec3 vec3D = vector = Vec3.newTemp(var1.x, var1.y, var1.z);
            final double xCoord2 = Mth.floor_double(var1.x);
            vector.x = xCoord2;
            floor_double4 = (int) xCoord2;
            if (n8 == 5) {
                --floor_double4;
                final Vec3 vec3D2 = vec3D;
                ++vec3D2.x;
            }
            final Vec3 vec3D3 = vec3D;
            final double yCoord2 = Mth.floor_double(var1.y);
            vec3D3.y = yCoord2;
            floor_double5 = (int) yCoord2;
            if (n8 == 1) {
                --floor_double5;
                final Vec3 vec3D4 = vec3D;
                ++vec3D4.y;
            }
            final Vec3 vec3D5 = vec3D;
            final double zCoord2 = Mth.floor_double(var1.z);
            vec3D5.z = zCoord2;
            floor_double6 = (int) zCoord2;
            if (n8 == 3) {
                --floor_double6;
                final Vec3 vec3D6 = vec3D;
                ++vec3D6.z;
            }
            int n9 = this.getBlockId(floor_double4, floor_double5, floor_double6);
            int n10 = this.getBlockMetadata(floor_double4, floor_double5, floor_double6);
            final Block block = Block.blocksList[n9];
            if (n9 > 0 && block.canCollideCheck(n10, var3)) {
                final MovingObjectPosition collisionRayTrace = block.collisionRayTrace(null, floor_double4,
                        floor_double5, floor_double6, var1, var2);
                if (collisionRayTrace != null) {
                    return collisionRayTrace;
                }
            }
            n9 = this.getBlockId(floor_double4, floor_double5 - 1, floor_double6);
            n10 = this.getBlockMetadata(floor_double4, floor_double5 - 1, floor_double6);
            final Block block2 = Block.blocksList[n9];
            if (n9 <= 0 || !block2.canCollideCheck(n10, var3)) {
                continue;
            }
            final MovingObjectPosition collisionRayTrace2 = block2.collisionRayTrace(null, floor_double4,
                    floor_double5 - 1, floor_double6, var1, var2);
            if (collisionRayTrace2 != null) {
                return collisionRayTrace2;
            }
        }
        return null;
    }

    @Override
    public void createExplosion(Entity entity, double x, double y, double z, float f) {

    }

    @Override
    public float getBlockDensity(Vec3 v, AABB aabb) {
        return 0;
    }

    @Override
    public List getCollidingBoundingBoxes(Entity entity, AABB aabb) {
        ArrayList list = new ArrayList();
        int floor_double = Mth.floor_double(aabb.minX);
        int floor_double2 = Mth.floor_double(aabb.maxX + 1.0);
        int floor_double3 = Mth.floor_double(aabb.minY);
        int floor_double4 = Mth.floor_double(aabb.maxY + 1.0);
        int floor_double5 = Mth.floor_double(aabb.minZ);
        int floor_double6 = Mth.floor_double(aabb.maxZ + 1.0);
        for (int i = floor_double; i < floor_double2; ++i) {
            for (int j = floor_double5; j < floor_double6; ++j) {
                if (!this.blockExists(i, 64, j)) {
                    continue;
                }
                for (int k = floor_double3 - 1; k < floor_double4; ++k) {
                    Block block = Block.blocksList[this.getBlockId(i, k, j)];
                    if (block != null) {
                        block.getCollidingBoundingBoxes(this, i, k, j, aabb, list);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public void playSound(Entity entity, String soundName, float volume, float pitch) {

    }

    public void playSoundEffect(final double xCoord, final double yCoord, final double zCoord, final String soundName,
            final float volume, final float pitch) {
        try {
            for (int i = 0; i < this.worldAccesses.size(); ++i) {
                float n = 16.0f;
                if (volume > 1.0f) {
                    n *= volume;
                }
                final double n2 = xCoord - this.player.posX;
                final double n3 = yCoord - this.player.posY;
                final double n4 = zCoord - this.player.posZ;
                if (n2 * n2 + n3 * n3 + n4 * n4 < n * n) {
                    ((IWorldAccess) this.worldAccesses.get(i)).playSound(soundName, xCoord, yCoord, zCoord, volume,
                            pitch);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void spawnParticle(String particle, double x, double y, double z, double mx, double my, double mz) {

    }

    public void entityJoinedWorld(final Entity entity) {
        final int floor_double = Mth.floor_double(entity.posX / 16.0);
        final int floor_double2 = Mth.floor_double(entity.posZ / 16.0);

        // Fix: Force chunk creation if it doesn't exist.
        // In client-server architecture, we might receive the player spawn packet
        // before the chunk data packet. We should create a placeholder chunk
        // so the entity can be added, rather than failing.
        // The chunk data will fill in later when the packet arrives.
        boolean chunkExists = this.chunkExists(floor_double, floor_double2);
        if (!chunkExists) {
            System.out.println(
                    "Forcing chunk creation for entity " + entity + " at " + floor_double + ", " + floor_double2);
        }

        // provideChunk will create a blank chunk if it doesn't exist (in
        // ChunkProviderClient)
        this.getChunkFromChunkCoords(floor_double, floor_double2).addEntity(entity);
        this.loadedEntityList.add(entity);
        for (int i = 0; i < this.worldAccesses.size(); ++i) {
            ((IWorldAccess) this.worldAccesses.get(i)).obtainEntitySkin(entity);
        }
    }

    public void setEntityDead(final Entity entity) {
        entity.setEntityDead();
    }

    // NOOP implementations for server-only methods
    @Override
    public void addWorldAccess(net.opencraft.core.world.IWorldAccess worldAccess) {

    }

    @Override
    public void removeWorldAccess(net.opencraft.core.world.IWorldAccess worldAccess) {

    }

    @Override
    public boolean isBoundingBoxBurning(AABB aabb) {
        return false;
    }

    @Override
    public boolean handleMaterialAcceleration(AABB aabb, Material material, Entity entity) {
        return false;
    }

    @Override
    public boolean isMaterialInBB(AABB aabb, Material material) {
        return false;
    }

    @Override
    public boolean getIsAnyLiquid(AABB aabb) {
        return false;
    }

    public void addWorldAccess(final IWorldAccess worldAccess) {
        this.worldAccesses.add(worldAccess);
    }

    public void removeWorldAccess(final IWorldAccess worldAccess) {
        this.worldAccesses.remove(worldAccess);
    }

    public int calculateSkylightSubtracted(final float float1) {
        float n = 1.0f - (cos(this.getCelestialAngle(float1) * PI_TIMES_2_f) * 2.0f + 0.5f);
        n = clamp(0, 1, n);

        return (int) (n * 11.0f);
    }

    public Vec3 getSkyColor(final float float1) {
        float n = cos(this.getCelestialAngle(float1) * PI_TIMES_2_f) * 2.0f + 0.5f;
        if (n < 0.0f) {
            n = 0.0f;
        }
        if (n > 1.0f) {
            n = 1.0f;
        }
        float n2 = (this.v >> 16 & 0xFFL) / 255.0f;
        float n3 = (this.v >> 8 & 0xFFL) / 255.0f;
        float n4 = (this.v & 0xFFL) / 255.0f;
        n2 *= n;
        n3 *= n;
        n4 *= n;
        return Vec3.newTemp(n2, n3, n4);
    }

    public float getCelestialAngle(final float float1) {
        float n = ((int) (this.getWorldTime % 24000L) + float1) / 24000.0f - 0.25f;
        if (n < 0.0f) {
            ++n;
        }
        if (n > 1.0f) {
            --n;
        }
        final float n2 = n;
        n = 1.0f - ((cos(n * PI_f) + 1.0f) * 0.5f);
        n = n2 + (n - n2) / 3.0f;
        return n;
    }

    public Vec3 drawClouds(final float float1) {
        float n = cos(this.getCelestialAngle(float1) * PI_TIMES_2_f) * 2.0f + 0.5f;
        if (n < 0.0f) {
            n = 0.0f;
        }
        if (n > 1.0f) {
            n = 1.0f;
        }
        float n2 = (this.field_1019_F >> 16 & 0xFFL) / 255.0f;
        float n3 = (this.field_1019_F >> 8 & 0xFFL) / 255.0f;
        float n4 = (this.field_1019_F & 0xFFL) / 255.0f;
        n2 *= n * 0.9f + 0.1f;
        n3 *= n * 0.9f + 0.1f;
        n4 *= n * 0.85f + 0.15f;
        return Vec3.newTemp(n2, n3, n4);
    }

    public Vec3 getFogColor(final float float1) {
        float n = cos(this.getCelestialAngle(float1) * PI_TIMES_2_f) * 2.0f + 0.5f;
        if (n < 0.0f) {
            n = 0.0f;
        }
        if (n > 1.0f) {
            n = 1.0f;
        }
        float n2 = (this.w >> 16 & 0xFFL) / 255.0f;
        float n3 = (this.w >> 8 & 0xFFL) / 255.0f;
        float n4 = (this.w & 0xFFL) / 255.0f;
        n2 *= n * 0.94f + 0.06f;
        n3 *= n * 0.94f + 0.06f;
        n4 *= n * 0.91f + 0.09f;
        return Vec3.newTemp(n2, n3, n4);
    }

    public int findTopSolidBlock(final int integer1, final int integer2) {
        return 64;
    }

    public float getStarBrightness(final float float1) {
        float n = 1.0f - (cos(this.getCelestialAngle(float1) * PI_TIMES_2_f) * 2.0f + 0.75f);
        if (n < 0.0f) {
            n = 0.0f;
        }
        if (n > 1.0f) {
            n = 1.0f;
        }
        return n * n * 0.5f;
    }

    public void scheduleBlockUpdate(final int integer1, final int integer2, final int integer3, final int integer4) {
        final NextTickListEntry nextTickListEntry = new NextTickListEntry(integer1, integer2, integer3, integer4);
        final int n = 8;
        if (this.checkChunksExist(integer1 - n, integer2 - n, integer3 - n, integer1 + n, integer2 + n, integer3 + n)) {
            if (integer4 > 0) {
                nextTickListEntry.setScheduledTime(Block.blocksList[integer4].tickRate() + this.getWorldTime);
            }
            if (!this.scheduledTickSet.contains(nextTickListEntry)) {
                this.scheduledTickSet.add(nextTickListEntry);
                this.scheduledTickTreeSet.add(nextTickListEntry);
            }
        }
    }

    public void updateEntities() {
        // In integrated server mode, the server handles entity updates
        // The client should only update entities if not connected to server
        if (net.opencraft.client.OpenCraft.oc != null && net.opencraft.client.OpenCraft.oc.isMultiplayerWorld()) {
            // When connected to integrated server, client should receive entity updates
            // from server
            // rather than updating them locally
            // For now, we'll still allow client-side update for rendering purposes
            // but in a full implementation, we'd sync entity positions from server
        }

        this.loadedEntityList.removeAll((Collection) this.unloadedEntityList);
        for (int i = 0; i < this.worldAccesses.size(); ++i) {
            final IWorldAccess worldAccess = (IWorldAccess) this.worldAccesses.get(i);
            for (int j = 0; j < this.unloadedEntityList.size(); ++j) {
                worldAccess.releaseEntitySkin((Entity) this.unloadedEntityList.get(j));
            }
        }
        this.unloadedEntityList.clear();
        for (int i = 0; i < this.loadedEntityList.size(); ++i) {
            final Entity entity = (Entity) this.loadedEntityList.get(i);
            if (entity.ridingEntity != null) {
                if (!entity.ridingEntity.isDead && entity.ridingEntity.riddenByEntity == entity) {
                    continue;
                }
                entity.ridingEntity.riddenByEntity = null;
                entity.ridingEntity = null;
            }
            if (!entity.isDead) {
                this.updateEntity(entity);
            }
            if (entity.isDead) {
                final int j = Mth.floor_double(entity.posX / 16.0);
                final int floor_double = Mth.floor_double(entity.posZ / 16.0);
                if (this.chunkExists(j, floor_double)) {
                    this.getChunkFromChunkCoords(j, floor_double).removeEntity(entity);
                }
                this.loadedEntityList.remove(i--);
                for (int k = 0; k < this.worldAccesses.size(); ++k) {
                    ((IWorldAccess) this.worldAccesses.get(k)).releaseEntitySkin(entity);
                }
            }
        }
        for (int i = 0; i < this.loadedTileEntityList.size(); ++i) {
            ((TileEntity) this.loadedTileEntityList.get(i)).updateEntity();
        }
    }

    private void updateEntity(final Entity entity) {
        final int floor_double = Mth.floor_double(entity.posX);
        Mth.floor_double(entity.posY);
        final int floor_double2 = Mth.floor_double(entity.posZ);
        final int n = 16;
        if (!this.checkChunksExist(floor_double - n, 0, floor_double2 - n, floor_double + n, 128, floor_double2 + n)) {
            // Debug: This is preventing entity updates!
            if (entity == this.player) {
                System.out.println("CLIENT: Skipping player update - chunks not loaded in 16-block radius around (" +
                        floor_double + ", " + floor_double2 + ")");
            }
            return;
        }
        entity.lastTickPosX = entity.posX;
        entity.lastTickPosY = entity.posY;
        entity.lastTickPosZ = entity.posZ;
        entity.prevRotationYaw = entity.rotationYaw;
        entity.prevRotationPitch = entity.rotationPitch;
        final int floor_double3 = Mth.floor_double(entity.posX / 16.0);
        final int floor_double4 = Mth.floor_double(entity.posY / 16.0);
        final int floor_double5 = Mth.floor_double(entity.posZ / 16.0);
        if (entity.ridingEntity != null) {
            entity.updateRidden();
        } else {
            entity.onUpdate();
        }
        final int floor_double6 = Mth.floor_double(entity.posX / 16.0);
        final int floor_double7 = Mth.floor_double(entity.posY / 16.0);
        final int floor_double8 = Mth.floor_double(entity.posZ / 16.0);
        if (floor_double3 != floor_double6 || floor_double4 != floor_double7 || floor_double5 != floor_double8) {
            if (this.chunkExists(floor_double3, floor_double5)) {
                this.getChunkFromChunkCoords(floor_double3, floor_double5).removeEntityAtIndex(entity, floor_double4);
            }
            if (this.chunkExists(floor_double6, floor_double8)) {
                this.getChunkFromChunkCoords(floor_double6, floor_double8).addEntity(entity);
            } else {
                entity.setEntityDead();
            }
        }
        if (entity.riddenByEntity != null) {
            if (entity.riddenByEntity.isDead || entity.riddenByEntity.ridingEntity != entity) {
                entity.riddenByEntity.ridingEntity = null;
                entity.riddenByEntity = null;
            } else {
                this.updateEntity(entity.riddenByEntity);
            }
        }
        if (Double.isNaN(entity.posX) || Double.isInfinite(entity.posX)) {
            entity.posX = entity.lastTickPosX;
        }
        if (Double.isNaN(entity.posY) || Double.isInfinite(entity.posY)) {
            entity.posY = entity.lastTickPosY;
        }
        if (Double.isNaN(entity.posZ) || Double.isInfinite(entity.posZ)) {
            entity.posZ = entity.lastTickPosZ;
        }
        if (Double.isNaN(entity.rotationPitch) || Double.isInfinite(entity.rotationPitch)) {
            entity.rotationPitch = entity.prevRotationPitch;
        }
        if (Double.isNaN(entity.rotationYaw) || Double.isInfinite(entity.rotationYaw)) {
            entity.rotationYaw = entity.prevRotationYaw;
        }
    }

    public void onBlockHit(int var1, int var2, int var3, final int var4) {
        if (var4 == 0) {
            --var2;
        }
        if (var4 == 1) {
            ++var2;
        }
        if (var4 == 2) {
            --var3;
        }
        if (var4 == 3) {
            ++var3;
        }
        if (var4 == 4) {
            --var1;
        }
        if (var4 == 5) {
            ++var1;
        }
        if (this.getBlockId(var1, var2, var3) == Block.fire.blockID) {
            this.playSoundEffect((var1 + 0.5f), (var2 + 0.5f), (var3 + 0.5f), "random.fizz", 0.5f,
                    2.6f + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.8f);
            this.setBlockWithNotify(var1, var2, var3, 0);
        }
    }

    public Entity func_4085_a(final Class cVar) {
        return null;
    }

    public String func_687_d() {
        return new StringBuilder().append("All: ").append(this.loadedEntityList.size()).toString();
    }

    // NOOP implementations for server-only methods
    @Override
    public void saveWorldIndirectly(net.opencraft.core.world.IProgressUpdate progressUpdate) {

    }

    @Override
    public boolean quickSaveWorld(int i) {
        return false;
    }

    @Override
    public void saveWorld(boolean flag, net.opencraft.core.world.IProgressUpdate progressUpdate) {

    }

    @Override
    public boolean isDaytime() {
        return false;
    }

    public Entity getPlayer() {
        return this.player;
    }

    @Override
    public void setBlockTileEntity(int x, int y, int z, TileEntity tileEntity) {

    }

    @Override
    public void removeBlockTileEntity(int x, int y, int z) {

    }

    @Override
    public TileEntity getBlockTileEntity(final int xCoord, final int yCoord, final int zCoord) {
        final Chunk chunkFromChunkCoords = this.getChunkFromChunkCoords(xCoord >> 4, zCoord >> 4);
        if (chunkFromChunkCoords != null) {
            return chunkFromChunkCoords.getChunkBlockTileEntity(xCoord & 0xF, yCoord, zCoord & 0xF);
        }
        return null;
    }

    @Override
    public boolean isBlockNormalCube(final int xCoord, final int yCoord, final int zCoord) {
        final Block block = Block.blocksList[this.getBlockId(xCoord, yCoord, zCoord)];
        return block != null && block.isOpaqueCube();
    }

    public void saveWorldIndirectly(final IProgressUpdate iPU) {
        this.saveWorld(true, iPU);
    }

    public void saveWorld(boolean flag, IProgressUpdate progressUpdate) {
        // ClientWorld should not save world state - that's the server's responsibility
        // The client only displays world state received from the server
        System.out.println("Warning: ClientWorld saveWorld called - client should not save world state");
        // In a proper implementation, the client would send changes to the server,
        // which would then save the world state
    }

    public boolean updatingLighting() {
        int n = 100000;
        while (this.lightingToUpdate.size() > 0) {
            if (--n <= 0) {
                return true;
            }
            ((MetadataChunkBlock) this.lightingToUpdate.remove(this.lightingToUpdate.size() - 1))
                    .updateLightingChunk(this);
        }
        return false;
    }

    public void scheduleLightingUpdate(final EnumSkyBlock enumSkyBlock, final int integer2, final int integer3,
            final int integer4, final int integer5, final int integer6, final int integer7) {
        final int size = this.lightingToUpdate.size();
        int n = 4;
        if (n > size) {
            n = size;
        }
        for (int i = 0; i < n; ++i) {
            final MetadataChunkBlock metadataChunkBlock = (MetadataChunkBlock) this.lightingToUpdate
                    .get(this.lightingToUpdate.size() - i - 1);
            if (metadataChunkBlock.field_1299_a == enumSkyBlock
                    && metadataChunkBlock.func_866_a(integer2, integer3, integer4, integer5, integer6, integer7)) {
                return;
            }
        }
        this.lightingToUpdate
                .add(new MetadataChunkBlock(enumSkyBlock, integer2, integer3, integer4, integer5, integer6, integer7));
        if (this.lightingToUpdate.size() > 100000) {
            while (this.lightingToUpdate.size() > 50000) {
                this.updatingLighting();
            }
        }
    }

    public void calculateInitialSkylight() {
        final int calculateSkylightSubtracted = this.calculateSkylightSubtracted(1.0f);
        if (calculateSkylightSubtracted != this.skylightSubtracted) {
            this.skylightSubtracted = calculateSkylightSubtracted;
        }
    }

    public void tick() {
        this.chunkProvider.unload100OldestChunks();
        if (!this.loadedEntityList.contains(this.player) && this.player != null) {
            this.entityJoinedWorld(this.player);
        }
        final int calculateSkylightSubtracted = this.calculateSkylightSubtracted(1.0f);
        if (calculateSkylightSubtracted != this.skylightSubtracted) {
            this.skylightSubtracted = calculateSkylightSubtracted;
            for (int i = 0; i < this.worldAccesses.size(); ++i) {
                ((IWorldAccess) this.worldAccesses.get(i)).updateAllRenderers();
            }
        }
        // Only update world time on client if not connected to integrated server
        if (net.opencraft.client.OpenCraft.oc == null || !net.opencraft.client.OpenCraft.oc.isMultiplayerWorld()) {
            ++this.getWorldTime;
            if (this.getWorldTime % 20L == 0L) { // this may be too often?
                // FIXME: attempt to save world on client-side, signal event to integrated
                // server instead
                // this.saveWorld(false, null);
            }
            this.TickUpdates(false);
            int i = Mth.floor_double(this.player.posX);
            final int floor_double = Mth.floor_double(this.player.posZ);
            final int n = 64;
            final ChunkCache chunkCache = new ChunkCache(this, i - n, 0, floor_double - n, i + n, 128,
                    floor_double + n);
            for (int j = 0; j < 8000; ++j) {
                this.randInt = this.randInt * 3 + this.zz;
                final int n2 = this.randInt >> 2;
                final int n3 = (n2 & 0x7F) - 64 + i;
                final int n4 = (n2 >> 8 & 0x7F) - 64 + floor_double;
                final int n5 = n2 >> 16 & 0x7F;
                final int blockId = chunkCache.getBlockId(n3, n5, n4);
                // FIXME: attempt to tick server blocks from client
                // if (Block.tickOnLoad[blockId]) {
                // Block.blocksList[blockId].updateTick(this, n3, n5, n4, this.rand);
                // }
            }
        } else {
            // When connected to integrated server, the server handles world ticking
            // The client should receive updates from the server instead
        }
    }

    public boolean TickUpdates(final boolean boolean1) {
        int size = this.scheduledTickTreeSet.size();
        if (size != this.scheduledTickSet.size()) {
            throw new IllegalStateException("TickNextTick list out of synch");
        }
        if (size > 500) {
            size = 500;
        }
        for (int i = 0; i < size; ++i) {
            final NextTickListEntry nextTickListEntry = (NextTickListEntry) this.scheduledTickTreeSet.first();
            if (!boolean1 && nextTickListEntry.scheduledTime > this.getWorldTime) {
                break;
            }
            this.scheduledTickTreeSet.remove(nextTickListEntry);
            this.scheduledTickSet.remove(nextTickListEntry);
            final int n = 8;
            // FIXME: attempt to tick server blocks from client
            // if (this.checkChunksExist(nextTickListEntry.xCoord - n,
            // nextTickListEntry.yCoord - n, nextTickListEntry.zCoord - n,
            // nextTickListEntry.xCoord + n, nextTickListEntry.yCoord + n,
            // nextTickListEntry.zCoord + n)) {
            // final int blockId = this.getBlockId(nextTickListEntry.xCoord,
            // nextTickListEntry.yCoord, nextTickListEntry.zCoord);
            // if (blockId == nextTickListEntry.blockID && blockId > 0) {
            // Block.blocksList[blockId].updateTick(this, nextTickListEntry.xCoord,
            // nextTickListEntry.yCoord, nextTickListEntry.zCoord, this.rand);
            // }
            // }
        }
        return !this.scheduledTickTreeSet.isEmpty();
    }

    public void randomDisplayUpdates(final int integer1, final int integer2, final int integer3) {
        final int n = 16;
        final Random random = new Random();
        for (int i = 0; i < 1000; ++i) {
            final int n2 = integer1 + this.rand.nextInt(n) - this.rand.nextInt(n);
            final int n3 = integer2 + this.rand.nextInt(n) - this.rand.nextInt(n);
            final int n4 = integer3 + this.rand.nextInt(n) - this.rand.nextInt(n);
            final int blockId = this.getBlockId(n2, n3, n4);
            if (blockId > 0) {
                // FIXME: attempt to tick server from client
                // Block.blocksList[blockId].randomDisplayTick(this, n2, n3, n4, random);
            }
        }
    }

    // NOOP implementations for server-only methods
    @Override
    public List getEntitiesWithinAABB(Class class1, AABB aabb) {
        return List.of();
    }

    @Override
    public void func_698_b(int x, int y, int z) {

    }

    @Override
    public int countEntities(Class class1) {
        return 0;
    }

    @Override
    public boolean canBlockBePlacedAt(int blockid, int x, int y, int z, boolean flag) {
        return false;
    }

    @Override
    public PathEntity getPathToEntity(Entity entity, Entity entity2, float f) {
        return null;
    }

    @Override
    public PathEntity getEntityPathToXYZ(Entity entity, int x, int y, int z, float f) {
        return null;
    }

    public List getEntitiesWithinAABBExcludingEntity(final Entity entity, final AABB aabb) {
        this.field_1012_M.clear();
        final int floor_double = Mth.floor_double((aabb.minX - 2.0) / 16.0);
        final int floor_double2 = Mth.floor_double((aabb.maxX + 2.0) / 16.0);
        final int floor_double3 = Mth.floor_double((aabb.minZ - 2.0) / 16.0);
        final int floor_double4 = Mth.floor_double((aabb.maxZ + 2.0) / 16.0);
        for (int i = floor_double; i <= floor_double2; ++i) {
            for (int j = floor_double3; j <= floor_double4; ++j) {
                if (this.chunkExists(i, j)) {
                    this.getChunkFromChunkCoords(i, j).getEntitiesWithinAABBForEntity(entity, aabb, this.field_1012_M);
                }
            }
        }
        return this.field_1012_M;
    }

    public List getLoadedEntityList() {
        return this.loadedEntityList;
    }

    public void addLoadedEntities(final List list) {
        this.loadedEntityList.addAll((Collection) list);
        for (int i = 0; i < this.worldAccesses.size(); ++i) {
            final IWorldAccess worldAccess = (IWorldAccess) this.worldAccesses.get(i);
            for (int j = 0; j < list.size(); ++j) {
                worldAccess.obtainEntitySkin((Entity) list.get(j));
            }
        }
    }

    public void unloadEntities(final List list) {
        this.unloadedEntityList.addAll((Collection) list);
    }

    public void func_656_j() {
        while (this.chunkProvider.unload100OldestChunks()) {
        }
    }

    public static NBTTagCompound potentiallySavesFolderLocation(final File file, final String string) {
        final File file2 = new File(new File(file, "saves"), string);
        if (!file2.exists()) {
            return null;
        }
        final File file3 = new File(file2, "level.dat");
        if (file3.exists()) {
            try {
                return CompressedStreamTools.loadGzippedCompoundFromOutputStream(new FileInputStream(file3))
                        .getCompoundTag("Data");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static String[] getSaveNames(File minecraftDir) {
        final File file = new File(minecraftDir, "saves");
        if (!file.exists()) {
            return new String[0];
        }
        final File[] arr = file.listFiles();
        final String[] arr2 = new String[arr.length];
        for (int i = 0; i < arr.length; ++i) {
            if (arr[i].isDirectory()) {
                arr2[i] = arr[i].getName();
            }
        }
        return arr2;
    }

    @Override
    public int getDifficultySetting() {
        return this.difficultySetting;
    }

    @Override
    public void setDifficultySetting(int difficulty) {
        this.difficultySetting = difficulty;
    }

    public static void deleteWorldDirectory(final File file, final String string) {
        final File file2 = new File(new File(file, "saves"), string);
        if (!file2.exists()) {
            return;
        }
        a(file2.listFiles());
        file2.delete();
    }

    private static void a(final File[] arr) {
        for (int i = 0; i < arr.length; ++i) {
            if (arr[i].isDirectory()) {
                a(arr[i].listFiles());
            }
            arr[i].delete();
        }
    }
}
