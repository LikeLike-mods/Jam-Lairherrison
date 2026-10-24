package net.ussm.examplemod.init;

import net.ussm.examplemod.ExampleMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public class ModComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
        DeferredRegister.createDataComponents(
                Registries.DATA_COMPONENT_TYPE,
                ExampleMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ACTIVE =
        COMPONENTS.registerComponentType("active",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
