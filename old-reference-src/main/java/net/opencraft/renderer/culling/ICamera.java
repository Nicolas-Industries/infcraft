
package net.opencraft.client.renderer.culling;

import net.opencraft.core.physics.AABB;

public interface ICamera {

    boolean isBoundingBoxInFrustum(final AABB aabb);

    void setPosition(final double xCoord, final double yCoord, final double zCoord);
}
