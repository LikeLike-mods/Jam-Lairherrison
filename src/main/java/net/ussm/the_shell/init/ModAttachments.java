package net.ussm.the_shell.init;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.ussm.the_shell.TheShell;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TheShell.MOD_ID);

    // usure des pinces
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> CRAB_CLAW_WEAR =
        ATTACHMENT_TYPES.register("crab_claw_wear", () -> AttachmentType.builder(() -> 0)
            .serialize(Codec.INT)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, ByteBufCodecs.VAR_INT)
            .build());

    // pour la conque
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> ATOLL_TRAVEL =
        ATTACHMENT_TYPES.register("atoll_travel", () -> AttachmentType.builder(() -> new CompoundTag())
            .serialize(CompoundTag.CODEC)
            .copyOnDeath()
            .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
