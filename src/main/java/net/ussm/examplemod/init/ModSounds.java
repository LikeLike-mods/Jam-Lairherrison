package net.ussm.examplemod.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.examplemod.ExampleMod;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ExampleMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTSABER_IGNITE = register("lightsaber_ignite");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTSABER_EXTINGUISH = register("lightsaber_extinguish");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTSABER_HUM = register("lightsaber_hum");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
