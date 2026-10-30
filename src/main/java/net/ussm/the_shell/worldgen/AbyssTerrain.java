package net.ussm.the_shell.worldgen;

import net.minecraft.util.Mth;
import net.ussm.the_shell.api.FastNoiseLite;

import static net.ussm.the_shell.worldgen.TestChunkGenerator.SEED;

// Shape of the abyss terrain
public final class AbyssTerrain {
    public static final int MIN_Y = -64;
    public static final int HEIGHT = 256;
    public static final int TOP = MIN_Y + HEIGHT - 1;   // 191
    public static final int FLOOR = 0;                  // fond moyen des plaines
    public static final int CEILING = 140;              // bas du plafond en moyenne

    public enum Zone { PLAINS, VENTS, TRENCH, REEF }

    private static final FastNoiseLite ROLL = noise(SEED + 101, 0.008f, FastNoiseLite.FractalType.FBm, 3);
    private static final FastNoiseLite BUMPS = noise(SEED + 102, 0.04f, FastNoiseLite.FractalType.FBm, 2);
    private static final FastNoiseLite TRENCH = noise(SEED + 103, 0.0016f, FastNoiseLite.FractalType.Ridged, 1);
    private static final FastNoiseLite REGION = noise(SEED + 104, 0.002f, FastNoiseLite.FractalType.FBm, 2);
    private static final FastNoiseLite ROOF = noise(SEED + 105, 0.01f, FastNoiseLite.FractalType.FBm, 3);
    private static final FastNoiseLite DRIP = noise(SEED + 106, 0.08f, FastNoiseLite.FractalType.None, 1);
    private static final FastNoiseLite PLATEAU = noise(SEED + 107, 0.0035f, FastNoiseLite.FractalType.FBm, 3);
    // Peaks with worley noise
    private static final FastNoiseLite PEAK_DIST = cellular(SEED + 108, FastNoiseLite.CellularReturnType.Distance);
    private static final FastNoiseLite PEAK_SIZE = cellular(SEED + 108, FastNoiseLite.CellularReturnType.CellValue);

    private AbyssTerrain() {
    }

    private static FastNoiseLite noise(int seed, float freq, FastNoiseLite.FractalType fractal, int octaves) {
        FastNoiseLite n = new FastNoiseLite(seed);
        n.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        n.SetFrequency(freq);
        n.SetFractalType(fractal);
        n.SetFractalOctaves(octaves);
        return n;
    }

    private static FastNoiseLite cellular(int seed, FastNoiseLite.CellularReturnType type) {
        FastNoiseLite n = new FastNoiseLite(seed);
        n.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
        n.SetFrequency(0.018f);
        n.SetFractalType(FastNoiseLite.FractalType.None);
        n.SetCellularDistanceFunction(FastNoiseLite.CellularDistanceFunction.Euclidean);
        n.SetCellularReturnType(type);
        return n;
    }

    // 0 hors des fosses, 1 au milieu d'une fosse
    public static float trench(int x, int z) {
        float r = (TRENCH.GetNoise(x, z) + 1f) * 0.5f;
        return (float) Mth.smoothstep(Mth.clamp((r - 0.84f) / 0.07f, 0f, 1f));
    }

    public static Zone zone(int x, int z) {
        if (trench(x, z) > 0.3f) return Zone.TRENCH;
        float region = REGION.GetNoise(x, z);
        if (region < -0.3f) return Zone.VENTS;
        if (region > 0.35f) return Zone.REEF;
        return Zone.PLAINS;
    }

    // Height of the ceiling
    public static int floor(int x, int z) {
        float y = FLOOR + 3f * ROLL.GetNoise(x, z) + 1.5f * BUMPS.GetNoise(x, z);
        // Staircase plateaus
        float e = Mth.clamp((PLATEAU.GetNoise(x, z) + 0.15f) * 1.6f, 0f, 0.999f) * 3f;
        int marche = (int) e;
        y += (marche + Mth.clamp((e - marche - 0.9f) * 10f, 0f, 1f)) * 22f;
        // Peaks
        float taille = PEAK_SIZE.GetNoise(x, z);
        if (taille > 0.35f) {
            float cone = Mth.clamp(1f - (PEAK_DIST.GetNoise(x, z) + 1f) / 0.3f, 0f, 1f);
            y += (float) Math.pow(cone, 1.6) * (30f + 45f * taille);
        }
        float region = REGION.GetNoise(x, z);
        if (region < -0.3f) y += 5f * Mth.clamp((-region - 0.3f) * 4f, 0f, 1f) * (1f + BUMPS.GetNoise(x * 2, z * 2));   // buttes autour des cheminees
        // Trenches
        y = Mth.lerp(trench(x, z), y, -54f + 2f * BUMPS.GetNoise(x, z));
        return Mth.floor(Math.min(y, CEILING - 25));
    }

    // Ceiling
    public static int ceiling(int x, int z) {
        float y = CEILING + 10f * ROOF.GetNoise(x, z);
        float drip = DRIP.GetNoise(x, z);
        if (drip > 0.55f) y -= (drip - 0.55f) * 30f;
        return Math.max(floor(x, z) + 30, Mth.floor(y));
    }
}
