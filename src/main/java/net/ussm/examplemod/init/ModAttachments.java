package net.ussm.examplemod.init;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.ussm.examplemod.ExampleMod;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ExampleMod.MOD_ID);

    public static final Supplier<AttachmentType<Boolean>> WARM_VARIANT = ATTACHMENT_TYPES.register("warm_variant",
        () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).sync(ByteBufCodecs.BOOL).build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
