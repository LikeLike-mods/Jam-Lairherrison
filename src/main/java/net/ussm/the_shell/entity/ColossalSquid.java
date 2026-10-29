package net.ussm.the_shell.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.entity.init.AbyssalPredator;
import software.bernie.geckolib.constant.DefaultAnimations;

// calmar colossal : enorme, au fond des fosses. ses tentacules agrippent (on est ralenti) et il crache de l'encre quand on le blesse
public class ColossalSquid extends AbyssalPredator {
    private int inkCooldown;

    public ColossalSquid(EntityType<? extends ColossalSquid> type, Level level) {
        super(type, level, 18.0, DefaultAnimations.ATTACK_STRIKE, 0.6, 60);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.7)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2), this);
        }
        return hit;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (inkCooldown > 0) inkCooldown--;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && inkCooldown == 0) {
            inkCooldown = 100;
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 1.0, this.getZ(), 60, 1.2, 1.0, 1.2, 0.05);
            if (source.getEntity() instanceof LivingEntity attacker && attacker.distanceToSqr(this) < 64.0) {
                attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80), this);
            }
        }
        return hurt;
    }
}
