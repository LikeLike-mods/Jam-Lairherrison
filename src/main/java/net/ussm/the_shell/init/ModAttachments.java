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

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> CRAB_CLAW_WEAR =
        ATTACHMENT_TYPES.register("crab_claw_wear", () -> AttachmentType.builder(() -> 0)
            .serialize(Codec.INT)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, ByteBufCodecs.VAR_INT)
            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> ATOLL_TRAVEL =
        ATTACHMENT_TYPES.register("atoll_travel", () -> AttachmentType.builder(() -> new CompoundTag())
            .serialize(CompoundTag.CODEC)
            .copyOnDeath()
            .build());

    // reserve d'oxygene du scaphandre, en ticks
    public static final int MAX_OXYGEN = 4800;
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> OXYGEN =
        ATTACHMENT_TYPES.register("oxygen", () -> AttachmentType.builder(() -> MAX_OXYGEN)
            .serialize(Codec.INT)
            .sync((holder, to) -> holder == to, ByteBufCodecs.VAR_INT)
            .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
