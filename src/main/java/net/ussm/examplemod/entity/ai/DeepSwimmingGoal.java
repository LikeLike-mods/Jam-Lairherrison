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

    @Nullable
    @Override
    protected Vec3 getPosition() {
        Vec3 pos = super.getPosition();
        return pos != null && pos.y > this.mob.level().getSeaLevel() - this.depth ? null : pos;
    }
}
