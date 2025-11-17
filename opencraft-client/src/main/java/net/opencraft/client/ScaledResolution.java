
package net.opencraft.client;

import net.opencraft.client.config.GameSettings;

public class ScaledResolution {

    private int scaledWidth;
    private int scaledHeight;
    private int scaleFactor;

    public ScaledResolution(final int width, final int height, GameSettings settings) {
        this.scaledWidth = width;
        this.scaledHeight = height;

        // Determine the scale factor based on settings
        if (settings.guiScale == 0) { // Auto scale
            int scale = 1;
            for (scale = 1; width / (scale + 1) >= 320 && height / (scale + 1) >= 240; ++scale) {
                // Find the largest scale that still allows at least 320x240 scaled resolution
            }
            this.scaleFactor = scale;
        } else {
            // Use the specified scale from settings, but make sure it's reasonable
            this.scaleFactor = Math.max(1, settings.guiScale);
            // Ensure scale doesn't make the resolution too small
            while (width / scaleFactor < 320 || height / scaleFactor < 240) {
                scaleFactor--;
            }
            // Ensure scale doesn't exceed reasonable limits - allow up to 8x for high resolutions
            scaleFactor = Math.min(scaleFactor, 8);
        }

        this.scaledWidth = width / scaleFactor;
        this.scaledHeight = height / scaleFactor;
    }

    public ScaledResolution(final int width, final int height) {
        this(width, height, new GameSettings(null, null)); // Use default settings for backward compatibility
    }

    public int getScaledWidth() {
        return this.scaledWidth;
    }

    public int getScaledHeight() {
        return this.scaledHeight;
    }

    public int getScaleFactor() {
        return this.scaleFactor;
    }
}
