package net.opencraft;

import java.util.Random;

public class PerlinNoise {

    private int[] permutationTable;
    public double xOffset;
    public double yOffset;
    public double zOffset;

    public PerlinNoise() {
        this(new Random());
    }

    public PerlinNoise(final Random random) {
        this.permutationTable = new int[512];
        this.xOffset = random.nextDouble() * 256.0;
        this.yOffset = random.nextDouble() * 256.0;
        this.zOffset = random.nextDouble() * 256.0;

        for (int i = 0; i < 256; ++i) {
            this.permutationTable[i] = i;
        }
        for (int i = 0; i < 256; ++i) {
            final int swapIndex = random.nextInt(256 - i) + i;
            final int tempValue = this.permutationTable[i];
            this.permutationTable[i] = this.permutationTable[swapIndex];
            this.permutationTable[swapIndex] = tempValue;
            this.permutationTable[i + 256] = this.permutationTable[i];
        }
    }

    /**
     * Compute 3D Perlin noise at (x, y, z).
     */
    public double getNoise(double x, double y, double z) {
        double xCoord = x + this.xOffset;
        double yCoord = y + this.yOffset;
        double zCoord = z + this.zOffset;

        int xFloor = (int) xCoord;
        int yFloor = (int) yCoord;
        int zFloor = (int) zCoord;

        if (xCoord < xFloor) --xFloor;
        if (yCoord < yFloor) --yFloor;
        if (zCoord < zFloor) --zFloor;

        int xHash = xFloor & 0xFF;
        int yHash = yFloor & 0xFF;
        int zHash = zFloor & 0xFF;

        xCoord -= xFloor;
        yCoord -= yFloor;
        zCoord -= zFloor;

        double u = fade(xCoord);
        double v = fade(yCoord);
        double w = fade(zCoord);

        int aa = permutationTable[xHash] + yHash;
        int aaa = permutationTable[aa] + zHash;
        int aba = permutationTable[aa + 1] + zHash;
        int baa = permutationTable[xHash + 1] + yHash;
        int bba = permutationTable[baa] + zHash;
        int bab = permutationTable[baa + 1] + zHash;

        return lerp(w,
                lerp(v,
                        lerp(u, grad(permutationTable[aaa], xCoord, yCoord, zCoord),
                                grad(permutationTable[bba], xCoord - 1.0, yCoord, zCoord)),
                        lerp(u, grad(permutationTable[aba], xCoord, yCoord - 1.0, zCoord),
                                grad(permutationTable[bab], xCoord - 1.0, yCoord - 1.0, zCoord))),
                lerp(v,
                        lerp(u, grad(permutationTable[aaa + 1], xCoord, yCoord, zCoord - 1.0),
                                grad(permutationTable[bba + 1], xCoord - 1.0, yCoord, zCoord - 1.0)),
                        lerp(u, grad(permutationTable[aba + 1], xCoord, yCoord - 1.0, zCoord - 1.0),
                                grad(permutationTable[bab + 1], xCoord - 1.0, yCoord - 1.0, zCoord - 1.0)))
        );
    }

    /**
     * Linear interpolation.
     */
    public double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    /**
     * Gradient function.
     */
    public double grad(int hash, double x, double y, double z) {
        int h = hash & 0xF;
        double u = (h < 8) ? x : y;
        double v = (h < 4) ? y : ((h == 12 || h == 14) ? x : z);
        return (((h & 1) == 0) ? u : -u) + (((h & 2) == 0) ? v : -v);
    }

    /**
     * Fade function for smoothing.
     */
    private double fade(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    /**
     * 2D noise convenience method.
     */
    public double getNoise(double x, double y) {
        return getNoise(x, y, 0.0);
    }

    /**
     * Alias for 3D noise.
     */
    public double getNoise3D(double x, double y, double z) {
        return getNoise(x, y, z);
    }

    /**
     * Fill an array with noise values over a 3D grid.
     */
    public void fillNoiseArray(double[] noiseArray,
                               int startX, int startY, int startZ,
                               int sizeX, int sizeY, int sizeZ,
                               double scaleX, double scaleY, double scaleZ,
                               double amplitude) {
        int index = 0;
        double amplitudeScale = 1.0 / amplitude;
        int prevYHash = -1;
        double lerpXY1 = 0.0, lerpXY2 = 0.0, lerpXZ1 = 0.0, lerpXZ2 = 0.0;

        for (int i = 0; i < sizeX; ++i) {
            double xCoord = (startX + i) * scaleX + this.xOffset;
            int xFloor = (int) xCoord;
            if (xCoord < xFloor) --xFloor;
            int xHash = xFloor & 0xFF;
            xCoord -= xFloor;
            double u = fade(xCoord);

            for (int j = 0; j < sizeZ; ++j) {
                double zCoord = (startZ + j) * scaleZ + this.zOffset;
                int zFloor = (int) zCoord;
                if (zCoord < zFloor) --zFloor;
                int zHash = zFloor & 0xFF;
                zCoord -= zFloor;
                double w = fade(zCoord);

                for (int k = 0; k < sizeY; ++k) {
                    double yCoord = (startY + k) * scaleY + this.yOffset;
                    int yFloor = (int) yCoord;
                    if (yCoord < yFloor) --yFloor;
                    int yHash = yFloor & 0xFF;
                    yCoord -= yFloor;
                    double v = fade(yCoord);

                    if (k == 0 || yHash != prevYHash) {
                        prevYHash = yHash;
                        int aa = permutationTable[xHash] + yHash;
                        int aaa = permutationTable[aa] + zHash;
                        int aba = permutationTable[aa + 1] + zHash;
                        int baa = permutationTable[xHash + 1] + yHash;
                        int bba = permutationTable[baa] + zHash;
                        int bab = permutationTable[baa + 1] + zHash;

                        lerpXY1 = lerp(u, grad(permutationTable[aaa], xCoord, yCoord, zCoord),
                                grad(permutationTable[bba], xCoord - 1.0, yCoord, zCoord));
                        lerpXY2 = lerp(u, grad(permutationTable[aba], xCoord, yCoord - 1.0, zCoord),
                                grad(permutationTable[bab], xCoord - 1.0, yCoord - 1.0, zCoord));
                        lerpXZ1 = lerp(u, grad(permutationTable[aaa + 1], xCoord, yCoord, zCoord - 1.0),
                                grad(permutationTable[bba + 1], xCoord - 1.0, yCoord, zCoord - 1.0));
                        lerpXZ2 = lerp(u, grad(permutationTable[aba + 1], xCoord, yCoord - 1.0, zCoord - 1.0),
                                grad(permutationTable[bab + 1], xCoord - 1.0, yCoord - 1.0, zCoord - 1.0));
                    }

                    double noiseValue = lerp(w, lerp(v, lerpXY1, lerpXY2), lerp(v, lerpXZ1, lerpXZ2));
                    noiseArray[index++] += noiseValue * amplitudeScale;
                }
            }
        }
    }
}
