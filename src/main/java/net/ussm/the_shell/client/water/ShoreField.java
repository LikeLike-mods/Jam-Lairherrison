package net.ussm.the_shell.client.water;

import java.util.Arrays;

public final class ShoreField {
    public static final byte UNKNOWN = 0;
    public static final byte LAND = 1;
    public static final byte WATER = 2;
    public static final int MAX_DISTANCE = 32;
    public static final int MAX_DEPTH = 32;
    private static final float DIAGONAL = (float) Math.sqrt(2.0);

    private ShoreField() {}

    public static float[] distances(byte[] cells, int size) {
        if (size <= 0 || cells.length != size * size) {
            throw new IllegalArgumentException("A square shoreline grid is required");
        }
        float[] d = new float[cells.length];
        Arrays.fill(d, MAX_DISTANCE);
        for (int i = 0; i < d.length; i++) {
            if (cells[i] == LAND) d[i] = 0;
        }
        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                int i = z * size + x;
                if (cells[i] != WATER) continue;
                if (x > 0) d[i] = Math.min(d[i], d[i - 1] + 1);
                if (z > 0) {
                    d[i] = Math.min(d[i], d[i - size] + 1);
                    if (x > 0) d[i] = Math.min(d[i], d[i - size - 1] + DIAGONAL);
                    if (x + 1 < size) d[i] = Math.min(d[i], d[i - size + 1] + DIAGONAL);
                }
            }
        }
        for (int z = size - 1; z >= 0; z--) {
            for (int x = size - 1; x >= 0; x--) {
                int i = z * size + x;
                if (cells[i] != WATER) continue;
                if (x + 1 < size) d[i] = Math.min(d[i], d[i + 1] + 1);
                if (z + 1 < size) {
                    d[i] = Math.min(d[i], d[i + size] + 1);
                    if (x > 0) d[i] = Math.min(d[i], d[i + size - 1] + DIAGONAL);
                    if (x + 1 < size) d[i] = Math.min(d[i], d[i + size + 1] + DIAGONAL);
                }
            }
        }
        return d;
    }

    public static int pixel(byte kind, int depth, float distance) {
        if (kind != WATER) return 0xFF000000;
        int r = Math.round(Math.clamp(depth / (float) MAX_DEPTH, 0, 1) * 255);
        int g = Math.round(Math.clamp(distance / MAX_DISTANCE, 0, 1) * 255);
        return 0xFFFF0000 | (g << 8) | r;
    }
}
