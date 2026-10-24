package net.ussm.examplemod.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

public class VisitFlowerGoal extends Goal {
    private final PathfinderMob mob;
    private BlockPos flower;
    private int stayTicks;
    private int cooldown;

    public VisitFlowerGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        this.cooldown = 60 + this.mob.getRandom().nextInt(80);
        Optional<BlockPos> found = BlockPos.findClosestMatch(this.mob.blockPosition(), 10, 4,
                pos -> this.mob.level().getBlockState(pos).is(BlockTags.FLOWERS));
        found.ifPresent(pos -> this.flower = pos.immutable());
        return found.isPresent();
    }

    @Override
    public boolean canContinueToUse() {
        return this.flower != null && this.stayTicks > 0 && this.mob.level().getBlockState(this.flower).is(BlockTags.FLOWERS);
    }

    @Override
    public void start() {
        this.stayTicks = 100 + this.mob.getRandom().nextInt(100);
        this.goToFlower();
    }

    @Override
    public void tick() {
        Vec3 above = Vec3.atBottomCenterOf(this.flower).add(0.0, 0.7, 0.0);
        if (this.mob.distanceToSqr(above) < 0.6) {
            this.mob.getNavigation().stop();
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.6));
            this.stayTicks--;
        } else if (this.mob.getNavigation().isDone()) {
            this.goToFlower();
            this.stayTicks -= 5;
        }
    }

    @Override
    public void stop() {
        this.flower = null;
    }

    private void goToFlower() {
        this.mob.getNavigation().moveTo(this.flower.getX() + 0.5, this.flower.getY() + 0.7, this.flower.getZ() + 0.5, 1.0);
    }
}
