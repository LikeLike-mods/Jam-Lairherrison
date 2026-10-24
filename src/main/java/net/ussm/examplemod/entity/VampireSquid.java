package net.ussm.examplemod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.ussm.examplemod.entity.animations.Animations;
import net.ussm.examplemod.entity.init.AbyssalPredator;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;

public class VampireSquid extends AbyssalPredator {
    private int defendTicks;

    public VampireSquid(EntityType<? extends VampireSquid> type, Level level) {
        super(type, level, 10.0, DefaultAnimations.ATTACK_STRIKE, 0.7, 50);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.8)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    /*@Override
    public void aiStep() {
        super.aiStep();
        if (defendTicks > 0) {
            defendTicks--;
        }
    }*/

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (defendTicks > 0) {
            amount *= 0.5F;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && defendTicks == 0) {
            defendTicks = 30;
            this.triggerAnim("action", "defend");
            if (source.getEntity() instanceof LivingEntity attacker && attacker.distanceToSqr(this) < 25.0) {
                attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60), this);
            }
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.GLOW, this.getX(), this.getY() + 0.5, this.getZ(), 20, 0.6, 0.6, 0.6, 0.02);
        }
        return hurt;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        super.registerControllers(controllers);
        controllers.add(new AnimationController<>(this, "action", 2, state -> PlayState.STOP)
                .triggerableAnim("defend", Animations.DEFEND));
    }
}
