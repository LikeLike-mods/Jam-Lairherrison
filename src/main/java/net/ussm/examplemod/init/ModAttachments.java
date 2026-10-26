package net.ussm.examplemod.init;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.ussm.examplemod.ExampleMod;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ExampleMod.MOD_ID);

    // Usure des pinces de crabe, portée par le joueur (envoyée seulement à lui, gardée à sa mort)
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> CRAB_CLAW_WEAR =
        ATTACHMENT_TYPES.register("crab_claw_wear", () -> AttachmentType.builder(() -> 0)
            .serialize(Codec.INT)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, ByteBufCodecs.VAR_INT)
            .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
