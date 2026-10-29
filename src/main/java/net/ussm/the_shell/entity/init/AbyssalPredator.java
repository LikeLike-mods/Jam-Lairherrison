package net.ussm.the_shell.entity.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.entity.ai.DeepSwimmingGoal;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;

public abstract class AbyssalPredator extends SwimmingMobs implements Enemy {
    private final double detectionRange;
    private final RawAnimation attackAnimation;
    private final double wanderSpeed;
    private final int wanderInterval;

    protected AbyssalPredator(EntityType<? extends WaterAnimal> type, Level level, double detectionRange, RawAnimation attackAnimation,
                              double wanderSpeed, int wanderInterval) {
        super(type, level);
        this.detectionRange = detectionRange;
        this.attackAnimation = attackAnimation;
        this.wanderSpeed = wanderSpeed;
        this.wanderInterval = wanderInterval;
        this.xpReward = 5;
        this.goalSelector.addGoal(4, new DeepSwimmingGoal(this, this.wanderSpeed, this.wanderInterval));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4, true));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isInDetectionRange));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return super.canAttack(target) && SpawnRules.inAbyss(this.level(), target.blockPosition());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.isInWater() && !SpawnRules.inAbyss(this.level(), this.blockPosition())) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.01, 0.0));
        }
    }

    private boolean isInDetectionRange(LivingEntity target) {
        return target.isInWater() && target.distanceToSqr(this) < detectionRange * detectionRange;
    }

    @Override
    protected int getBaseExperienceReward() {
        return this.xpReward;
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericSwimIdleController(this));
        controllers.add(DefaultAnimations.genericAttackAnimation(this, attackAnimation));
    }
}