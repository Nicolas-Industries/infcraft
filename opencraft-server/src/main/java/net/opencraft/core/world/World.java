package net.opencraft.core.world;

import net.opencraft.core.blocks.material.Material;
import net.opencraft.core.entity.Entity;
import net.opencraft.core.enums.EnumSkyBlock;
import net.opencraft.core.input.MovingObjectPosition;
import net.opencraft.core.nbt.NBTTagCompound;
import net.opencraft.core.pathfinder.PathEntity;
import net.opencraft.core.physics.AABB;
import net.opencraft.core.tileentity.TileEntity;
import net.opencraft.core.util.Vec3;

import java.io.File;
import java.util.List;

public interface World extends IBlockAccess {

    List getCollidingBoundingBoxes(Entity entity, AABB aabb);

    void playSound(Entity entity, String soundName, float volume, float pitch);

    void playSoundEffect(double x, double y, double z, String soundName, float volume, float pitch);

    void spawnParticle(String particle, double x, double y, double z, double mx, double my, double mz);

    void entityJoinedWorld(Entity entity);

    void setEntityDead(Entity entity);

    void addWorldAccess(IWorldAccess worldAccess);

    void removeWorldAccess(IWorldAccess worldAccess);

    boolean isBoundingBoxBurning(AABB aabb);

    boolean handleMaterialAcceleration(AABB aabb, Material material, Entity entity);

    boolean isMaterialInBB(AABB aabb, Material material);

    boolean getIsAnyLiquid(AABB aabb);

    int getHeightValue(int x, int z);

    boolean canExistingBlockSeeTheSky(int x, int y, int z);

    int getBlockLightValue_do(int x, int y, int z, boolean flag);

    int getSavedLightValue(EnumSkyBlock enumSkyBlock, int x, int y, int z);

    void setLightValue(EnumSkyBlock enumSkyBlock, int x, int y, int z, int value);

    float getCelestialAngle(float f);

    Vec3 getSkyColor(float f);

    Vec3 drawClouds(float f);

    Vec3 getFogColor(float f);

    float getStarBrightness(float f);

    void scheduleBlockUpdate(int x, int y, int z, int blockId);

    void updateEntities();

    List getEntitiesWithinAABBExcludingEntity(Entity entity, AABB aabb);

    List getLoadedEntityList();

    void addLoadedEntities(List list);

    void unloadEntities(List list);

    void func_656_j();

    void setBlockTileEntity(int x, int y, int z, TileEntity tileEntity);

    void removeBlockTileEntity(int x, int y, int z);

    boolean updatingLighting();

    void scheduleLightingUpdate(EnumSkyBlock enumSkyBlock, int i1, int i2, int i3, int i4, int i5, int i6);

    void calculateInitialSkylight();

    void tick();

    boolean TickUpdates(boolean flag);

    void randomDisplayUpdates(int x, int y, int z);

    List getEntitiesWithinAABB(Class class1, AABB aabb);

    void func_698_b(int x, int y, int z);

    int countEntities(Class class1);

    boolean canBlockBePlacedAt(int blockid, int x, int y, int z, boolean flag);

    PathEntity getPathToEntity(Entity entity, Entity entity2, float f);

    PathEntity getEntityPathToXYZ(Entity entity, int x, int y, int z, float f);

    static NBTTagCompound potentiallySavesFolderLocation(File file, String string) {
        return null;
    }

    static int getAmountOfSaves(File file) {
        return 0;
    }

    static String[] getSaveNames(File file) {
        return new String[0];
    }

    static void deleteWorldDirectory(File file, String string) {
    }

    // Additional methods from Block usage

    boolean blockExists(int x, int y, int z);

    boolean checkChunksExist(int x1, int y1, int z1, int x2, int y2, int z2);

    boolean setBlock(int x, int y, int z, int blockid);

    boolean setBlockMetadata(int x, int y, int z, int metadata);

    boolean setBlockWithNotify(int x, int y, int z, int blockid);

    boolean setBlockAndMetadata(int x, int y, int z, int blockid, int metadata);

    boolean setBlockAndMetadataWithNotify(int x, int y, int z, int blockid, int metadata);


    void markBlocksDirtyVertical(int x, int y, int z1, int z2);

    void markBlocksDirty(int x1, int y1, int z1, int x2, int y2, int z2);

    void notifyBlocksOfNeighborChange(int x, int y, int z, int blockid);

    void notifyBlockOfNeighborChange(int x, int y, int z, int blockid);

    boolean canBlockSeeTheSky(int x, int y, int z);

    int getBlockLightValue(int x, int y, int z);

    MovingObjectPosition rayTraceBlocks(Vec3 v1, Vec3 v2);

    MovingObjectPosition rayTraceBlocks_do_do(Vec3 v1, Vec3 v2, boolean flag);

    void createExplosion(Entity entity, double x, double y, double z, float f);

    float getBlockDensity(Vec3 v, AABB aabb);

    void onBlockHit(int x, int y, int z, int side);

    Entity func_4085_a(Class class1);

    String func_687_d();

    void saveWorldIndirectly(IProgressUpdate progressUpdate);

    boolean quickSaveWorld(int i);

    void saveWorld(boolean flag, IProgressUpdate progressUpdate);

    boolean isDaytime();

    int calculateSkylightSubtracted(float f);

    int findTopSolidBlock(int x, int z);

    void neighborLightPropagationChanged(EnumSkyBlock enumSkyBlock, int x, int y, int z, int value);

    private void notifyBlockChange(int x, int y, int z, int blockid) {

    }
}
