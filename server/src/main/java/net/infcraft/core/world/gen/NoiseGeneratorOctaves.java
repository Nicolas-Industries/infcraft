package net.infcraft.core.world.gen;

import java.util.Random;

/**
 * Generates fractal noise by combining multiple octaves of Perlin noise.
 */
public class NoiseGeneratorOctaves {

    private final PerlinNoise[] octaves;
    private final int octaveCount;

    public NoiseGeneratorOctaves(final Random random, final int octaveCount) {
        this.octaveCount = octaveCount;
        this.octaves = new PerlinNoise[octaveCount];
        for (int i = 0; i < octaveCount; ++i) {
            this.octaves[i] = new PerlinNoise(random);
        }
    }

    /**
     * Generate 2D octave noise at (x, y).
     */
    public double getNoise2D(final double x, final double y) {
        double result = 0.0;
        double frequency = 1.0;
        for (int i = 0; i < this.octaveCount; ++i) {
            result += this.octaves[i].getNoise(x * frequency, y * frequency) / frequency;
            frequency /= 2.0;
        }
        return result;
    }

    /**
     * Generate 3D octave noise at (x, y, z).
     */
    public double getNoise3D(final double x, final double y, final double z) {
        double result = 0.0;
        double frequency = 1.0;
        for (int i = 0; i < this.octaveCount; ++i) {
            result += this.octaves[i].getNoise3D(x * frequency, y * frequency, z * frequency) / frequency;
            frequency /= 2.0;
        }
        return result;
    }

    /**
     * Fill an array with octave noise values over a 3D grid.
     */
    public double[] generateNoiseArray(double[] noiseArray,
                                       final int startX, final int startY, final int startZ,
                                       final int sizeX, final int sizeY, final int sizeZ,
                                       final double scaleX, final double scaleY, final double scaleZ) {
        if (noiseArray == null) {
            noiseArray = new double[sizeX * sizeY * sizeZ];
        } else {
            for (int i = 0; i < noiseArray.length; ++i) {
                noiseArray[i] = 0.0;
            }
        }

        double frequency = 1.0;
        for (int j = 0; j < this.octaveCount; ++j) {
            this.octaves[j].fillNoiseArray(
                    noiseArray,
                    startX, startY, startZ,
                    sizeX, sizeY, sizeZ,
                    scaleX * frequency,
                    scaleY * frequency,
                    scaleZ * frequency,
                    frequency
            );
            frequency /= 2.0;
        }
        return noiseArray;
    }
}
