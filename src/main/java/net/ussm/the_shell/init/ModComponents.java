package net.ussm.the_shell.init;

import net.ussm.the_shell.TheShell;
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
                TheShell.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ACTIVE =
        COMPONENTS.registerComponentType("active",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FELL_TREE =
        COMPONENTS.registerComponentType("fell_tree",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> WEIGHTED =
        COMPONENTS.registerComponentType("weighted",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> REINFORCED =
        COMPONENTS.registerComponentType("reinforced",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<net.minecraft.core.BlockPos>> GRAPPLE_TARGET =
        COMPONENTS.registerComponentType("grapple_target",
            builder -> builder.networkSynchronized(net.minecraft.core.BlockPos.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
