package net.ussm.examplemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.ussm.examplemod.entity.init.AbyssalPredator;
import software.bernie.geckolib.constant.DefaultAnimations;

public class GulperEel extends AbyssalPredator {
    public GulperEel(EntityType<? extends GulperEel> type, Level level) {
        super(type, level, 12.0, DefaultAnimations.ATTACK_BITE, 0.8, 40);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }
}
