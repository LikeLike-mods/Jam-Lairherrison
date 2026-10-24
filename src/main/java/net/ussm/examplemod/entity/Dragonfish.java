package net.ussm.examplemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.constant.DefaultAnimations;

public class Dragonfish extends AbyssalPredator {
    public Dragonfish(EntityType<? extends Dragonfish> type, Level level) {
        super(type, level, 14.0, DefaultAnimations.ATTACK_BITE, 1.0, 10);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 1.2)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }
}
