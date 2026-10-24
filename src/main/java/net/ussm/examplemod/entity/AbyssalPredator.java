package net.ussm.examplemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;

// Base des prédateurs des grottes inondées : nagent au hasard, attaquent les joueurs qui passent à portée.
public abstract class AbyssalPredator extends AtollSwimmer implements Enemy {
    private final double detectionRange;
    private final RawAnimation attackAnimation;

    protected AbyssalPredator(EntityType<? extends WaterAnimal> type, Level level, double detectionRange, RawAnimation attackAnimation) {
        super(type, level);
        this.detectionRange = detectionRange;
        this.attackAnimation = attackAnimation;
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4, true));
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0, 10));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isInDetectionRange));
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
