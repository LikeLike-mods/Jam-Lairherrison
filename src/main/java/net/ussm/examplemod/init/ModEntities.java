package net.ussm.examplemod.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.entity.LeviathanEntity;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ExampleMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<LeviathanEntity>> LEVIATHAN =
            ENTITIES.register("leviathan", () -> EntityType.Builder.of(LeviathanEntity::new, MobCategory.MONSTER)
                    .sized(2.6F * LeviathanEntity.SIZE_MULTIPLIER, 1.7F * LeviathanEntity.SIZE_MULTIPLIER)
                    .eyeHeight(1.2F * LeviathanEntity.SIZE_MULTIPLIER)
                    .clientTrackingRange(12)
                    .updateInterval(3)
                    .build(ExampleMod.MOD_ID + ":leviathan"));

    private ModEntities() {
    }

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
        eventBus.addListener(ModEntities::createAttributes);
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(LEVIATHAN.get(), LeviathanEntity.createAttributes().build());
    }
}
