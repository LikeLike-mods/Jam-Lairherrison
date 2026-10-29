package net.ussm.the_shell.entites.animations;

import net.minecraft.util.Mth;

public final class TurnBendState {
    private final float smoothing;

    private final float maxTurnPerTick;
    private float bend;
    private float previousBend;

    public TurnBendState(float maxTurnPerTick) {
        this(maxTurnPerTick, 0.2F);
    }

    public TurnBendState(float maxTurnPerTick, float smoothing) {
        if (!(smoothing > 0 && smoothing <= 1)) throw new IllegalArgumentException("smoothing must be in (0, 1]");
        this.smoothing = smoothing;
        if (!(maxTurnPerTick > 0)) throw new IllegalArgumentException("maxTurnPerTick must be positive");
        this.maxTurnPerTick = maxTurnPerTick;
    }

    public void update(float bodyYaw, float previousBodyYaw) {
        previousBend = bend;
        float yawDelta = Mth.wrapDegrees(bodyYaw - previousBodyYaw);
        float target = Mth.clamp(yawDelta / maxTurnPerTick, -1.0F, 1.0F);
        bend += (target - bend) * smoothing;
    }

    public float getBend(float partialTick) {
        return Mth.lerp(partialTick, previousBend, bend);
    }
}
