package net.ussm.the_shell.events;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.init.ModComponents;
import net.ussm.the_shell.items.DivingBootsItem;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public record DropWeightPayload() implements CustomPacketPayload {
    public static final Type<DropWeightPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "drop_weight"));
    public static final StreamCodec<ByteBuf, DropWeightPayload> STREAM_CODEC = StreamCodec.unit(new DropWeightPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, DropWeightPayload::handle);
    }

    private static void handle(DropWeightPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (!DivingBootsItem.isWeighted(boots)) return;
            boots.set(ModComponents.WEIGHTED, false);
            ItemEntity ingot = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), new ItemStack(Items.IRON_INGOT));
            ingot.setDeltaMovement(0.0, -0.2, 0.0);
            player.level().addFreshEntity(ingot);
            Vec3 v = player.getDeltaMovement();
            player.setDeltaMovement(v.x, Math.max(v.y, 0.8), v.z);
            player.hurtMarked = true;
            player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.PLAYERS, 0.8F, 0.6F);
        });
    }
}
