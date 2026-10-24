package net.ussm.examplemod.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DeepSwimmingGoal extends RandomSwimmingGoal {
    private final int depth;

    public DeepSwimmingGoal(PathfinderMob mob, double speed, int interval, int depth) {
        super(mob, speed, interval);
        this.depth = depth;
    }

    @Override
    public boolean canUse() {
        if (this.mob.getY() > this.limit()) {
            this.trigger();
        }
        return super.canUse();
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        for (int i = 0; i < 10; i++) {
            Vec3 pos = super.getPosition();
            if (pos != null && (pos.y <= this.limit() || pos.y < this.mob.getY() - 1.0)) {
                return pos;
            }
        }
        return null;
    }

    private double limit() {
        return this.mob.level().getSeaLevel() - this.depth;
    }
}
