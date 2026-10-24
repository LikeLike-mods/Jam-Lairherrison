package net.ussm.examplemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.ussm.examplemod.entity.init.AbyssalPredator;
import software.bernie.geckolib.constant.DefaultAnimations;

public class AbyssalAngler extends AbyssalPredator {
    public AbyssalAngler(EntityType<? extends AbyssalAngler> type, Level level) {
        super(type, level, 7.0, DefaultAnimations.ATTACK_BITE, 0.5, 120);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.6)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }
}