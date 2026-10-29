package net.ussm.the_shell.world.water;

public record ImpactWave(double x, double y, double z, long startTick, float strength, float initialRadius) {
    public static final int MAX_ACTIVE = 8;
    public static final int LIFETIME_TICKS = 200;
    public static final float DEFAULT_STRENGTH = 1.0F;
    public static final float DEFAULT_RADIUS = 6.0F;

    public ImpactWave {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || Math.abs(x) > 30_000_000 || Math.abs(y) > 30_000_000 || Math.abs(z) > 30_000_000) {
            throw new IllegalArgumentException("Impact position must be finite and inside world bounds");
        }
        if (startTick < 0 || startTick > Long.MAX_VALUE - LIFETIME_TICKS) {
            throw new IllegalArgumentException("Invalid impact start tick");
        }
        if (!Float.isFinite(strength) || strength <= 0 || strength > 2) {
            throw new IllegalArgumentException("Impact strength must be in (0, 2]");
        }
        if (!Float.isFinite(initialRadius) || initialRadius < 0.5F || initialRadius > 12) {
            throw new IllegalArgumentException("Impact radius must be between 0.5 and 12 blocks");
        }
    }

    public float ageSeconds(long gameTick, float partialTick) {
        return (float) Math.max(0, (gameTick - startTick + (double) partialTick) / 20.0);
    }

    public boolean expired(long gameTick) { return gameTick - startTick >= LIFETIME_TICKS; }
}
