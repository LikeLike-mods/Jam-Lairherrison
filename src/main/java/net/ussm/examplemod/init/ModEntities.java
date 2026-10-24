package net.ussm.examplemod.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.entity.AbyssalAngler;
import net.ussm.examplemod.entity.Butterfly;
import net.ussm.examplemod.entity.Crab;
import net.ussm.examplemod.entity.Dragonfish;
import net.ussm.examplemod.entity.Gecko;
import net.ussm.examplemod.entity.GiantIsopod;
import net.ussm.examplemod.entity.GulperEel;
import net.ussm.examplemod.entity.HermitCrab;
import net.ussm.examplemod.entity.Jellyfish;
import net.ussm.examplemod.entity.PoisonFrog;
import net.ussm.examplemod.entity.Seagull;
import net.ussm.examplemod.entity.Toucan;
import net.ussm.examplemod.entity.VampireSquid;
import net.ussm.examplemod.entity.LeviathanEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, ExampleMod.MOD_ID);

    // Plage et jungle (paisibles)
    public static final DeferredHolder<EntityType<?>, EntityType<Butterfly>> BUTTERFLY = register("butterfly", Butterfly::new, MobCategory.AMBIENT, 0.5F, 0.3F);
    public static final DeferredHolder<EntityType<?>, EntityType<Crab>> CRAB = register("crab", Crab::new, MobCategory.CREATURE, 0.6F, 0.4F);
    public static final DeferredHolder<EntityType<?>, EntityType<Gecko>> GECKO = register("gecko", Gecko::new, MobCategory.CREATURE, 0.4F, 0.2F);
    public static final DeferredHolder<EntityType<?>, EntityType<HermitCrab>> HERMIT_CRAB = register("hermit_crab", HermitCrab::new, MobCategory.CREATURE, 0.4F, 0.4F);
    public static final DeferredHolder<EntityType<?>, EntityType<Jellyfish>> JELLYFISH = register("jellyfish", Jellyfish::new, MobCategory.WATER_AMBIENT, 0.5F, 0.8F);
    public static final DeferredHolder<EntityType<?>, EntityType<PoisonFrog>> POISON_FROG = register("poison_frog", PoisonFrog::new, MobCategory.CREATURE, 0.35F, 0.3F);
    public static final DeferredHolder<EntityType<?>, EntityType<Seagull>> SEAGULL = register("seagull", Seagull::new, MobCategory.CREATURE, 0.5F, 0.6F);
    public static final DeferredHolder<EntityType<?>, EntityType<Toucan>> TOUCAN = register("toucan", Toucan::new, MobCategory.CREATURE, 0.45F, 0.6F);

    // Gloom : les grottes, faune des abysses (hostiles)
    public static final DeferredHolder<EntityType<?>, EntityType<AbyssalAngler>> ABYSSAL_ANGLER = register("abyssal_angler", AbyssalAngler::new, MobCategory.MONSTER, 1.1F, 1.0F);
    public static final DeferredHolder<EntityType<?>, EntityType<GulperEel>> GULPER_EEL = register("gulper_eel", GulperEel::new, MobCategory.MONSTER, 0.8F, 0.5F);
    public static final DeferredHolder<EntityType<?>, EntityType<Dragonfish>> DRAGONFISH = register("dragonfish", Dragonfish::new, MobCategory.MONSTER, 0.5F, 0.4F);
    public static final DeferredHolder<EntityType<?>, EntityType<GiantIsopod>> GIANT_ISOPOD = register("giant_isopod", GiantIsopod::new, MobCategory.MONSTER, 0.9F, 0.45F);
    public static final DeferredHolder<EntityType<?>, EntityType<VampireSquid>> VAMPIRE_SQUID = register("vampire_squid", VampireSquid::new, MobCategory.MONSTER, 0.6F, 1.1F);

    // Léviathan d'Oesphy (taille et suivi propres)
    public static final DeferredHolder<EntityType<?>, EntityType<LeviathanEntity>> LEVIATHAN =
            ENTITY_TYPES.register("leviathan", () -> EntityType.Builder.of(LeviathanEntity::new, MobCategory.MONSTER)
                    .sized(2.6F * LeviathanEntity.SIZE_MULTIPLIER, 1.7F * LeviathanEntity.SIZE_MULTIPLIER)
                    .eyeHeight(1.2F * LeviathanEntity.SIZE_MULTIPLIER)
                    .clientTrackingRange(12)
                    .updateInterval(3)
                    .build(ExampleMod.MOD_ID + ":leviathan"));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.EntityFactory<T> factory,
                                                                                           MobCategory category, float width, float height) {
        return ENTITY_TYPES.register(name, () -> EntityType.Builder.of(factory, category).sized(width, height).build(name));
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BUTTERFLY.get(), Butterfly.createAttributes().build());
        event.put(CRAB.get(), Crab.createAttributes().build());
        event.put(GECKO.get(), Gecko.createAttributes().build());
        event.put(HERMIT_CRAB.get(), HermitCrab.createAttributes().build());
        event.put(JELLYFISH.get(), Jellyfish.createAttributes().build());
        event.put(POISON_FROG.get(), PoisonFrog.createAttributes().build());
        event.put(SEAGULL.get(), Seagull.createAttributes().build());
        event.put(TOUCAN.get(), Toucan.createAttributes().build());
        event.put(ABYSSAL_ANGLER.get(), AbyssalAngler.createAttributes().build());
        event.put(GULPER_EEL.get(), GulperEel.createAttributes().build());
        event.put(DRAGONFISH.get(), Dragonfish.createAttributes().build());
        event.put(GIANT_ISOPOD.get(), GiantIsopod.createAttributes().build());
        event.put(VAMPIRE_SQUID.get(), VampireSquid.createAttributes().build());
        event.put(LEVIATHAN.get(), LeviathanEntity.createAttributes().build());
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
        eventBus.addListener(ModEntities::registerAttributes);
    }
}
