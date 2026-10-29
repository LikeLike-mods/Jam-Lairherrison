package net.ussm.the_shell.entity.animations;

import net.minecraft.util.Mth;

public final class LeviathanTurnState {
    private static final float SMOOTHING = 0.2F;

    private final float maxTurnPerTick;
    private float bend;
    private float previousBend;

    public LeviathanTurnState(float maxTurnPerTick) {
        this.maxTurnPerTick = maxTurnPerTick;
    }

    public void update(float bodyYaw, float previousBodyYaw) {
        previousBend = bend;
        float yawDelta = Mth.wrapDegrees(bodyYaw - previousBodyYaw);
        float target = Mth.clamp(yawDelta / maxTurnPerTick, -1.0F, 1.0F);
        bend += (target - bend) * SMOOTHING;
    }

    public float getBend(float partialTick) {
        return Mth.lerp(partialTick, previousBend, bend);
    }
}
