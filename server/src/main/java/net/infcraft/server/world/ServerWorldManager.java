package net.infcraft.server.world;

import net.infcraft.core.entity.Entity;
import net.infcraft.core.entity.EntityItem;
import net.infcraft.core.world.IWorldAccess;
import net.infcraft.server.Server;
import net.infcraft.shared.network.packets.PacketSpawnItem;

public class ServerWorldManager implements IWorldAccess {

    private Server server;
    private ServerWorld world;

    public ServerWorldManager(Server server, ServerWorld world) {
        this.server = server;
        this.world = world;
    }

    @Override
    public void markBlockAndNeighborsNeedsUpdate(int integer1, int integer2, int integer3) {
    }

    @Override
    public void markBlockRangeNeedsUpdate(int integer1, int integer2, int integer3, int integer4, int integer5,
            int integer6) {
    }

    @Override
    public void playSound(String soundName, double xCoord, double yCoord, double zCoord, float volume, float pitch) {
        // TODO: Broadcast sound packet
    }

    @Override
    public void spawnParticle(String particle, double xCoordBlock, double yCoordBlock, double zCoordBlock,
            double xPosition, double yPosition, double zPosition) {
        // TODO: Broadcast particle packet
    }

    @Override
    public void obtainEntitySkin(Entity entity) {
        System.out.println("SERVER: obtainEntitySkin called for entity " + entity.getClass().getSimpleName() + " ID="
                + entity.entityId);
        if (entity instanceof EntityItem) {
            EntityItem item = (EntityItem) entity;
            System.out.println("SERVER: Creating PacketSpawnItem for item " + item.entityId);
            PacketSpawnItem packet = new PacketSpawnItem(
                    item.entityId,
                    item.posX,
                    item.posY,
                    item.posZ,
                    item.motionX,
                    item.motionY,
                    item.motionZ,
                    item.item);
            System.out.println("SERVER: Sending PacketSpawnItem to all players");
            server.getNetworkSystem().sendPacketToAllPlayers(packet);
        }
    }

    @Override
    public void releaseEntitySkin(Entity entity) {
    }

    @Override
    public void updateAllRenderers() {
    }

    @Override
    public void onEntityPickup(Entity collector, Entity collected) {
        if (collected instanceof EntityItem) {
            net.infcraft.shared.network.packets.PacketCollectItem packet = new net.infcraft.shared.network.packets.PacketCollectItem(
                    collected.entityId,
                    collector.entityId);
            server.getNetworkSystem().sendPacketToAllPlayers(packet);

            // Synchronize inventory to client after pickup
            // The server has already updated the inventory in
            // EntityItem.onCollideWithPlayer
            // Now we need to tell the client about the change
            if (collector instanceof net.infcraft.core.entity.EntityPlayer) {
                net.infcraft.core.entity.EntityPlayer player = (net.infcraft.core.entity.EntityPlayer) collector;

                // Send full inventory to client
                // We use PacketWindowItems to sync the entire inventory because item pickup
                // can affect multiple slots (stacking, etc.)
                net.infcraft.shared.network.packets.PacketWindowItems inventoryPacket = new net.infcraft.shared.network.packets.PacketWindowItems(
                        0,
                        java.util.Arrays.asList(player.inventory.mainInventory));
                server.getNetworkSystem().sendPacketToPlayer(player.getUsername(), inventoryPacket);

                System.out.println("SERVER: Sent inventory sync to " + player.getUsername() + " after item pickup");
            }
        }
    }
}
