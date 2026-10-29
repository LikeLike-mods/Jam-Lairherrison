package net.ussm.the_shell.entites.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.entites.init.SpawnRules;
import org.jetbrains.annotations.Nullable;

public class DeepSwimmingGoal extends RandomSwimmingGoal {
    public DeepSwimmingGoal(PathfinderMob mob, double speed, int interval) {
        super(mob, speed, interval);
    }

    @Override
    public boolean canUse() {
        if (!SpawnRules.inAbyss(this.mob.level(), this.mob.blockPosition())) {
            this.trigger();
        }
        return super.canUse();
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        boolean outside = !SpawnRules.inAbyss(this.mob.level(), this.mob.blockPosition());
        for (int i = 0; i < 10; i++) {
            Vec3 pos = super.getPosition();
            if (pos != null && (SpawnRules.inAbyss(this.mob.level(), BlockPos.containing(pos)) || outside && pos.y < this.mob.getY() - 1.0)) {
                return pos;
            }
        }
        return null;
    }
}
