package net.ussm.the_shell.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.worldgen.CoastalVillage;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, TheShell.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<CoastalVillage>> COASTAL_VILLAGE =
            STRUCTURE_TYPES.register("coastal_village", () -> () -> CoastalVillage.CODEC);

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
    }
}
