package net.ussm.examplemod.entity;

import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;

public class Jellyfish extends SwimmingMobs {
    private int stingCooldown;
    private int pulseTimer;
    private float driftYaw;

    public Jellyfish(EntityType<? extends Jellyfish> type, Level level) {
        super(type, level);
        this.driftYaw = this.random.nextFloat() * 360.0F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (stingCooldown > 0) {
            stingCooldown--;
        }
        if (!this.level().isClientSide && this.isInWater()) {
            this.drift();
        }
    }

    private void drift() {
        boolean surface = !this.level().getFluidState(this.blockPosition().above()).is(FluidTags.WATER);
        if (--pulseTimer <= 0) {
            pulseTimer = 30 + this.random.nextInt(20);
            driftYaw += (this.random.nextFloat() - 0.5F) * 70.0F;
            double dx = -Mth.sin(driftYaw * Mth.DEG_TO_RAD) * 0.035;
            double dz = Mth.cos(driftYaw * Mth.DEG_TO_RAD) * 0.035;
            this.setDeltaMovement(this.getDeltaMovement().add(dx, surface ? 0.0 : 0.09, dz));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, surface ? -0.01 : -0.0022, 0.0));
        }
        this.setYRot(Mth.rotLerp(0.05F, this.getYRot(), driftYaw));
        this.yBodyRot = this.getYRot();
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
        controllers.add(new AnimationController<>(this, "move", 5, state ->
                state.setAndContinue(this.isInWater() ? DefaultAnimations.SWIM : DefaultAnimations.IDLE)));
    }
}
