package net.ussm.examplemod.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.constant.DefaultAnimations;

public class Jellyfish extends SwimmingMobs {
    private int stingCooldown;

    public Jellyfish(EntityType<? extends Jellyfish> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RandomSwimmingGoal(this, 0.6, 40));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (stingCooldown > 0) {
            stingCooldown--;
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (!this.level().isClientSide && stingCooldown == 0 && !player.getAbilities().invulnerable) {
            if (player.hurt(this.damageSources().mobAttack(this), 1.0F)) {
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 60), this);
            }
            stingCooldown = 40;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericSwimIdleController(this));
    }
}
