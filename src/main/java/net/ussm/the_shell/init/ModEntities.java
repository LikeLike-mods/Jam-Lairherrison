package net.ussm.the_shell.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.entites.AbyssalAngler;
import net.ussm.the_shell.entites.Butterfly;
import net.ussm.the_shell.entites.Crab;
import net.ussm.the_shell.entites.Dragonfish;
import net.ussm.the_shell.entites.Gecko;
import net.ussm.the_shell.entites.GiantIsopod;
import net.ussm.the_shell.entites.GulperEel;
import net.ussm.the_shell.entites.HermitCrab;
import net.ussm.the_shell.entites.Jellyfish;
import net.ussm.the_shell.entites.PoisonFrog;
import net.ussm.the_shell.entites.Seagull;
import net.ussm.the_shell.entites.Toucan;
import net.ussm.the_shell.entites.VampireSquid;
import net.ussm.the_shell.entites.LeviathanEntity;
import net.ussm.the_shell.entites.Submarine;
import net.ussm.the_shell.entites.BasilosaurusEntity;
import net.ussm.the_shell.entites.BlueWhaleEntity;
import net.ussm.the_shell.entites.PurussaurusEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, TheShell.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Butterfly>> BUTTERFLY = register("butterfly", Butterfly::new, MobCategory.AMBIENT, 0.5F, 0.3F);
    public static final DeferredHolder<EntityType<?>, EntityType<Crab>> CRAB = register("crab", Crab::new, MobCategory.CREATURE, 0.9F, 0.55F);
    public static final DeferredHolder<EntityType<?>, EntityType<Gecko>> GECKO = register("gecko", Gecko::new, MobCategory.CREATURE, 0.4F, 0.2F);
    public static final DeferredHolder<EntityType<?>, EntityType<HermitCrab>> HERMIT_CRAB = register("hermit_crab", HermitCrab::new, MobCategory.CREATURE, 0.4F, 0.4F);
    public static final DeferredHolder<EntityType<?>, EntityType<Jellyfish>> JELLYFISH = register("jellyfish", Jellyfish::new, MobCategory.WATER_AMBIENT, 0.5F, 0.8F);
    public static final DeferredHolder<EntityType<?>, EntityType<PoisonFrog>> POISON_FROG = register("poison_frog", PoisonFrog::new, MobCategory.CREATURE, 0.35F, 0.3F);
    public static final DeferredHolder<EntityType<?>, EntityType<Seagull>> SEAGULL = register("seagull", Seagull::new, MobCategory.CREATURE, 0.5F, 0.6F);
    public static final DeferredHolder<EntityType<?>, EntityType<Toucan>> TOUCAN = register("toucan", Toucan::new, MobCategory.CREATURE, 0.45F, 0.6F);

    public static final DeferredHolder<EntityType<?>, EntityType<AbyssalAngler>> ABYSSAL_ANGLER = register("abyssal_angler", AbyssalAngler::new, MobCategory.UNDERGROUND_WATER_CREATURE, 1.1F, 1.0F);
    public static final DeferredHolder<EntityType<?>, EntityType<GulperEel>> GULPER_EEL = register("gulper_eel", GulperEel::new, MobCategory.UNDERGROUND_WATER_CREATURE, 0.8F, 0.5F);
    public static final DeferredHolder<EntityType<?>, EntityType<Dragonfish>> DRAGONFISH = register("dragonfish", Dragonfish::new, MobCategory.UNDERGROUND_WATER_CREATURE, 0.5F, 0.4F);
    public static final DeferredHolder<EntityType<?>, EntityType<GiantIsopod>> GIANT_ISOPOD = register("giant_isopod", GiantIsopod::new, MobCategory.AXOLOTLS, 0.9F, 0.45F);
    public static final DeferredHolder<EntityType<?>, EntityType<VampireSquid>> VAMPIRE_SQUID = register("vampire_squid", VampireSquid::new, MobCategory.UNDERGROUND_WATER_CREATURE, 0.6F, 1.1F);
    public static final DeferredHolder<EntityType<?>, EntityType<net.ussm.the_shell.entites.YetiCrab>> YETI_CRAB = register("yeti_crab", net.ussm.the_shell.entites.YetiCrab::new, MobCategory.AXOLOTLS, 0.8F, 0.45F);
    public static final DeferredHolder<EntityType<?>, EntityType<net.ussm.the_shell.entites.Hatchetfish>> HATCHETFISH = register("hatchetfish", net.ussm.the_shell.entites.Hatchetfish::new, MobCategory.WATER_AMBIENT, 0.35F, 0.45F);
    public static final DeferredHolder<EntityType<?>, EntityType<net.ussm.the_shell.entites.ColossalSquid>> COLOSSAL_SQUID = register("colossal_squid", net.ussm.the_shell.entites.ColossalSquid::new, MobCategory.UNDERGROUND_WATER_CREATURE, 1.6F, 2.8F);


    public static final DeferredHolder<EntityType<?>, EntityType<Submarine>> SUBMARINE = register("submarine", Submarine::new, MobCategory.MISC, 2.0F, 1.5F);
    // Oesphy's leviathan
    public static final DeferredHolder<EntityType<?>, EntityType<LeviathanEntity>> LEVIATHAN =
            ENTITY_TYPES.register("leviathan", () -> EntityType.Builder.of(LeviathanEntity::new, MobCategory.MONSTER)
                    .sized(3.4F, 3.7F)
                    .eyeHeight(2.6F)
                    .clientTrackingRange(12)
                    .updateInterval(3)
                    .build(TheShell.MOD_ID + ":leviathan"));

    public static final DeferredHolder<EntityType<?>, EntityType<BasilosaurusEntity>> BASILOSAURUS =
            ENTITY_TYPES.register("basilosaurus", () -> EntityType.Builder.of(BasilosaurusEntity::new, MobCategory.MONSTER)
                    .sized(0.5F * BasilosaurusEntity.SIZE_MULTIPLIER, 0.5F * BasilosaurusEntity.SIZE_MULTIPLIER)
                    .eyeHeight(0.34F * BasilosaurusEntity.SIZE_MULTIPLIER)
                    .clientTrackingRange(12).updateInterval(3).build(TheShell.MOD_ID + ":basilosaurus"));
    public static final DeferredHolder<EntityType<?>, EntityType<BlueWhaleEntity>> BLUE_WHALE =
            ENTITY_TYPES.register("blue_whale", () -> EntityType.Builder.of(BlueWhaleEntity::new, MobCategory.WATER_CREATURE)
                    .sized(4.75F, 4.75F).eyeHeight(3.5F)
                    .clientTrackingRange(16).updateInterval(3).build(TheShell.MOD_ID + ":blue_whale"));
    public static final DeferredHolder<EntityType<?>, EntityType<PurussaurusEntity>> PURUSSAURUS =
            ENTITY_TYPES.register("purussaurus", () -> EntityType.Builder.of(PurussaurusEntity::new, MobCategory.MONSTER)
                    .sized(2.2F, 1.3F).eyeHeight(0.9F)
                    .clientTrackingRange(12).updateInterval(2).build(TheShell.MOD_ID + ":purussaurus"));

    public static final DeferredHolder<EntityType<?>, EntityType<net.ussm.the_shell.entites.ThrownAquaPearl>> AQUA_PEARL = ENTITY_TYPES.register("aqua_pearl",
            () -> EntityType.Builder.<net.ussm.the_shell.entites.ThrownAquaPearl>of(net.ussm.the_shell.entites.ThrownAquaPearl::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build("aqua_pearl"));

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
        event.put(YETI_CRAB.get(), net.ussm.the_shell.entites.YetiCrab.createAttributes().build());
        event.put(HATCHETFISH.get(), net.minecraft.world.entity.animal.AbstractFish.createAttributes().build());
        event.put(COLOSSAL_SQUID.get(), net.ussm.the_shell.entites.ColossalSquid.createAttributes().build());
        event.put(BASILOSAURUS.get(), BasilosaurusEntity.createAttributes().build());
        event.put(BLUE_WHALE.get(), BlueWhaleEntity.createAttributes().build());
        event.put(PURUSSAURUS.get(), PurussaurusEntity.createAttributes().build());
        event.put(LEVIATHAN.get(), LeviathanEntity.createAttributes().build());
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
        eventBus.addListener(ModEntities::registerAttributes);
    }
}
