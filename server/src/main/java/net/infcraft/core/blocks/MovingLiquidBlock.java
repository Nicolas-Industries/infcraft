
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;
import net.infcraft.server.world.ServerWorld;

import java.util.Random;

public class MovingLiquidBlock extends LiquidBlock {

    int numAdjacentSources;
    boolean[] isOptimalFlowDirection;
    int[] flowCost;

    protected MovingLiquidBlock(final int blockid, final Material material) {
        super(blockid, material);
        this.numAdjacentSources = 0;
        this.isOptimalFlowDirection = new boolean[4];
        this.flowCost = new int[4];
    }

    private void updateFlow(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        serverWorld.setBlockAndMetadata(xCoord, yCoord, zCoord, this.blockID + 1, serverWorld.getBlockMetadata(xCoord, yCoord, zCoord));
        serverWorld.markBlocksDirty(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
    }

    @Override
    public void updateTick(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final Random random) {
        int flowDecay = this.getFlowDecay(serverWorld, xCoord, yCoord, zCoord);
        boolean b = true;
        if (flowDecay > 0) {
            int n = -100;
            this.numAdjacentSources = 0;
            n = this.getSmallestFlowDecay(serverWorld, xCoord - 1, yCoord, zCoord, n);
            n = this.getSmallestFlowDecay(serverWorld, xCoord + 1, yCoord, zCoord, n);
            n = this.getSmallestFlowDecay(serverWorld, xCoord, yCoord, zCoord - 1, n);
            n = this.getSmallestFlowDecay(serverWorld, xCoord, yCoord, zCoord + 1, n);
            int n2 = n + this.unsure;
            if (n2 >= 8 || n < 0) {
                n2 = -1;
            }
            if (this.getFlowDecay(serverWorld, xCoord, yCoord + 1, zCoord) >= 0) {
                final int flowDecay2 = this.getFlowDecay(serverWorld, xCoord, yCoord + 1, zCoord);
                if (flowDecay2 >= 8) {
                    n2 = flowDecay2;
                } else {
                    n2 = flowDecay2 + 8;
                }
            }
            if (this.numAdjacentSources >= 2 && this.blockMaterial == Material.WATER) {
                n2 = 0;
            }
            if (this.blockMaterial == Material.LAVA && flowDecay < 8 && n2 < 8 && n2 > flowDecay && random.nextInt(4) != 0) {
                n2 = flowDecay;
                b = false;
            }
            if (n2 != flowDecay) {
                flowDecay = n2;
                if (flowDecay < 0) {
                    serverWorld.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
                } else {
                    serverWorld.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, flowDecay);
                    serverWorld.scheduleBlockUpdate(xCoord, yCoord, zCoord, this.blockID);
                    serverWorld.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, this.blockID);
                }
            } else if (b) {
                this.updateFlow(serverWorld, xCoord, yCoord, zCoord);
            }
        } else {
            this.updateFlow(serverWorld, xCoord, yCoord, zCoord);
        }
        if (this.liquidCanDisplaceBlock(serverWorld, xCoord, yCoord - 1, zCoord)) {
            if (flowDecay >= 8) {
                serverWorld.setBlockAndMetadataWithNotify(xCoord, yCoord - 1, zCoord, this.blockID, flowDecay);
            } else {
                serverWorld.setBlockAndMetadataWithNotify(xCoord, yCoord - 1, zCoord, this.blockID, flowDecay + 8);
            }
        } else if (flowDecay >= 0 && (flowDecay == 0 || this.blockBlocksFlow(serverWorld, xCoord, yCoord - 1, zCoord))) {
            final boolean[] optimalFlowDirections = this.getOptimalFlowDirections(serverWorld, xCoord, yCoord, zCoord);
            int n2 = flowDecay + this.unsure;
            if (flowDecay >= 8) {
                n2 = 1;
            }
            if (n2 >= 8) {
                return;
            }
            if (optimalFlowDirections[0]) {
                this.flowIntoBlock(serverWorld, xCoord - 1, yCoord, zCoord, n2);
            }
            if (optimalFlowDirections[1]) {
                this.flowIntoBlock(serverWorld, xCoord + 1, yCoord, zCoord, n2);
            }
            if (optimalFlowDirections[2]) {
                this.flowIntoBlock(serverWorld, xCoord, yCoord, zCoord - 1, n2);
            }
            if (optimalFlowDirections[3]) {
                this.flowIntoBlock(serverWorld, xCoord, yCoord, zCoord + 1, n2);
            }
        }
    }

    private void flowIntoBlock(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int metadataValue) {
        if (this.liquidCanDisplaceBlock(serverWorld, xCoord, yCoord, zCoord)) {
            final int blockId = serverWorld.getBlockId(xCoord, yCoord, zCoord);
            if (blockId > 0) {
                if (this.blockMaterial == Material.LAVA) {
                    this.triggerLavaMixEffects(serverWorld, xCoord, yCoord, zCoord);
                } else {
                    Block.blocksList[blockId].dropBlockAsItem(serverWorld, xCoord, yCoord, zCoord, serverWorld.getBlockMetadata(xCoord, yCoord, zCoord));
                }
            }
            serverWorld.setBlockAndMetadataWithNotify(xCoord, yCoord, zCoord, this.blockID, metadataValue);
        }
    }

    private int calculateFlowCost(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4, final int nya5) {
        int n = 1000;
        for (int i = 0; i < 4; ++i) {
            if (i != 0 || nya5 != 1) {
                if (i != 1 || nya5 != 0) {
                    if (i != 2 || nya5 != 3) {
                        if (i != 3 || nya5 != 2) {
                            int xCoord2 = xCoord;
                            int zCoord2 = zCoord;
                            if (i == 0) {
                                --xCoord2;
                            }
                            if (i == 1) {
                                ++xCoord2;
                            }
                            if (i == 2) {
                                --zCoord2;
                            }
                            if (i == 3) {
                                ++zCoord2;
                            }
                            if (!this.blockBlocksFlow(serverWorld, xCoord2, yCoord, zCoord2)) {
                                if (serverWorld.getBlockMaterial(xCoord2, yCoord, zCoord2) != this.blockMaterial || serverWorld.getBlockMetadata(xCoord2, yCoord, zCoord2) != 0) {
                                    if (!this.blockBlocksFlow(serverWorld, xCoord2, yCoord - 1, zCoord2)) {
                                        return nya4;
                                    }
                                    if (nya4 < 4) {
                                        final int calculateFlowCost = this.calculateFlowCost(serverWorld, xCoord2, yCoord, zCoord2, nya4 + 1, i);
                                        if (calculateFlowCost < n) {
                                            n = calculateFlowCost;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return n;
    }

    private boolean[] getOptimalFlowDirections(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        for (int i = 0; i < 4; ++i) {
            this.flowCost[i] = 1000;
            int j = xCoord;
            int zCoord2 = zCoord;
            if (i == 0) {
                --j;
            }
            if (i == 1) {
                ++j;
            }
            if (i == 2) {
                --zCoord2;
            }
            if (i == 3) {
                ++zCoord2;
            }
            if (!this.blockBlocksFlow(serverWorld, j, yCoord, zCoord2)) {
                if (serverWorld.getBlockMaterial(j, yCoord, zCoord2) != this.blockMaterial || serverWorld.getBlockMetadata(j, yCoord, zCoord2) != 0) {
                    if (!this.blockBlocksFlow(serverWorld, j, yCoord - 1, zCoord2)) {
                        this.flowCost[i] = 0;
                    } else {
                        this.flowCost[i] = this.calculateFlowCost(serverWorld, j, yCoord, zCoord2, 1, i);
                    }
                }
            }
        }
        int i = this.flowCost[0];
        for (int j = 1; j < 4; ++j) {
            if (this.flowCost[j] < i) {
                i = this.flowCost[j];
            }
        }
        for (int j = 0; j < 4; ++j) {
            this.isOptimalFlowDirection[j] = (this.flowCost[j] == i);
        }
        return this.isOptimalFlowDirection;
    }

    private boolean blockBlocksFlow(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        final int blockId = serverWorld.getBlockId(xCoord, yCoord, zCoord);
        return blockId == Block.door.blockID || blockId == Block.signPost.blockID || blockId == Block.ladder.blockID || (blockId != 0 && Block.blocksList[blockId].blockMaterial.isSolid());
    }

    protected int getSmallestFlowDecay(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        int flowDecay = this.getFlowDecay(serverWorld, xCoord, yCoord, zCoord);
        if (flowDecay < 0) {
            return nya4;
        }
        if (flowDecay == 0) {
            ++this.numAdjacentSources;
        }
        if (flowDecay >= 8) {
            flowDecay = 0;
        }
        return (nya4 < 0 || flowDecay < nya4) ? flowDecay : nya4;
    }

    private boolean liquidCanDisplaceBlock(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        final Material blockMaterial = serverWorld.getBlockMaterial(xCoord, yCoord, zCoord);
        return blockMaterial != this.blockMaterial && blockMaterial != Material.LAVA && !this.blockBlocksFlow(serverWorld, xCoord, yCoord, zCoord);
    }

    @Override
    public void onBlockAdded(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        super.onBlockAdded(serverWorld, xCoord, yCoord, zCoord);
        if (serverWorld.getBlockId(xCoord, yCoord, zCoord) == this.blockID) {
            serverWorld.scheduleBlockUpdate(xCoord, yCoord, zCoord, this.blockID);
        }
    }
}
