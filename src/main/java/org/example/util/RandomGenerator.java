package org.example.util;

import java.util.Random;

/**
 * Utility class for generating random values with specified ranges.
 */
public class RandomGenerator {
    private final Random random;

    public RandomGenerator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Generates a random integer between min (inclusive) and max (exclusive).
     *
     * @param min Minimum value (inclusive)
     * @param max Maximum value (exclusive)
     * @return Random integer within the specified range
     */
    public int nextInt(int min, int max) {
        return min + random.nextInt(max - min);
    }

    /**
     * Generates a random integer between min (inclusive) and max (exclusive) with specified step.
     *
     * @param min Minimum value (inclusive)
     * @param max Maximum value (exclusive)
     * @param step Step size between possible values
     * @return Random integer within the specified range, aligned to the step size
     */
    public int nextInt(int min, int max, int step) {
        int range = (max - min) / step;
        return min + (random.nextInt(range) * step);
    }

    /**
     * Generates a random long between min (inclusive) and max (exclusive).
     *
     * @param min Minimum value (inclusive)
     * @param max Maximum value (exclusive)
     * @return Random long within the specified range
     */
    public long nextLong(long min, long max) {
        return min + (long)(random.nextDouble() * (max - min));
    }

    /**
     * Generates a random long between min (inclusive) and max (exclusive) with specified step.
     *
     * @param min Minimum value (inclusive)
     * @param max Maximum value (exclusive)
     * @param step Step size between possible values
     * @return Random long within the specified range, aligned to the step size
     */
    public long nextLong(long min, long max, long step) {
        long range = (max - min) / step;
        return min + (random.nextInt((int)range) * step);
    }

    /**
     * Generates a random double between 0.0 (inclusive) and 1.0 (exclusive).
     *
     * @return Random double between 0.0 and 1.0
     */
    public double nextDouble() {
        return random.nextDouble();
    }

    /**
     * Generates a random double between min (inclusive) and max (exclusive).
     *
     * @param min Minimum value (inclusive)
     * @param max Maximum value (exclusive)
     * @return Random double within the specified range
     */
    public double nextDouble(double min, double max) {
        return min + (random.nextDouble() * (max - min));
    }
}