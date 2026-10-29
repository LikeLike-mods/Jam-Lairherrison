package net.ussm.the_shell.entites.ai;

public final class LeviathanBiteCycle {
    public static final int DURATION = 25;
    public static final int HIT = 10;
    public static final int COOLDOWN = 45;

    private int elapsedTicks = -1;
    private int cooldownTicks;

    public boolean start() {
        if (!isReady()) {
            return false;
        }

        elapsedTicks = 0;
        cooldownTicks = COOLDOWN;
        return true;
    }

    public boolean tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (!isActive()) {
            return false;
        }

        elapsedTicks++;
        boolean hit = elapsedTicks == HIT;
        if (elapsedTicks >= DURATION) {
            elapsedTicks = -1;
        }
        return hit;
    }

    public boolean isActive() {
        return elapsedTicks >= 0;
    }

    public boolean isReady() {
        return !isActive() && cooldownTicks == 0;
    }

    public void cancel() {
        elapsedTicks = -1;
    }
}
