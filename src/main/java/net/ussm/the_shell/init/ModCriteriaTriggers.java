package net.ussm.the_shell.init;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.the_shell.TheShell;

public final class ModCriteriaTriggers {
    private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, TheShell.MOD_ID);
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> WITNESS_WHALE_BREACH =
            TRIGGERS.register("witness_whale_breach", PlayerTrigger::new);

    public static void register(IEventBus bus) { TRIGGERS.register(bus); }
    private ModCriteriaTriggers() {}
}
