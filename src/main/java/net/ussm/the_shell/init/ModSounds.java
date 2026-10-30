package net.ussm.the_shell.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.the_shell.TheShell;

import java.util.List;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, TheShell.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTSABER_IGNITE = register("lightsaber_ignite");
    public static final DeferredHolder<SoundEvent, SoundEvent> BANANA_SLIP = register("banana_slip");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTSABER_EXTINGUISH = register("lightsaber_extinguish");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTSABER_HUM = register("lightsaber_hum");

    // sons d'ambiance (dans les biomes)
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_ATOLL_WAVES = register("ambient.atoll.waves");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_ATOLL_JUNGLE = register("ambient.atoll.jungle");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_ABYSS_LOOP = register("ambient.abyss.loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_ABYSS_ADDITIONS = register("ambient.abyss.additions");

    public static final DeferredHolder<SoundEvent, SoundEvent> FLYTRAP_SNAP = register("block.flytrap.snap");

    // sons des mobs, on les retrouve avec mobSound()
    static {
        for (String name : List.of(
                "toucan.ambient", "toucan.hurt", "toucan.death", "seagull.ambient", "seagull.hurt", "seagull.death",
                "crab.ambient", "crab.hurt", "crab.death", "crab.step", "hermit_crab.ambient", "hermit_crab.hurt",
                "hermit_crab.death", "hermit_crab.step", "gecko.ambient", "gecko.hurt", "gecko.death",
                "poison_frog.ambient", "poison_frog.hurt", "poison_frog.death", "butterfly.hurt", "butterfly.death",
                "jellyfish.ambient", "jellyfish.hurt", "jellyfish.death", "giant_isopod.ambient",
                "giant_isopod.hurt", "giant_isopod.death", "giant_isopod.step", "vampire_squid.ambient",
                "vampire_squid.hurt", "vampire_squid.death", "abyssal_angler.ambient", "abyssal_angler.hurt",
                "abyssal_angler.death", "dragonfish.ambient", "dragonfish.hurt", "dragonfish.death",
                "gulper_eel.ambient", "gulper_eel.hurt", "gulper_eel.death", "yeti_crab.ambient", "yeti_crab.hurt",
                "yeti_crab.death", "hatchetfish.ambient", "hatchetfish.hurt", "hatchetfish.death",
                "colossal_squid.ambient", "colossal_squid.hurt", "colossal_squid.death")) {
            register("entity." + name);
        }
    }

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, name)));
    }

    // null si le mob a pas ce son
    public static SoundEvent mobSound(Entity mob, String kind) {
        ResourceLocation id = EntityType.getKey(mob.getType());
        return BuiltInRegistries.SOUND_EVENT.get(id.withPath("entity." + id.getPath() + "." + kind));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
