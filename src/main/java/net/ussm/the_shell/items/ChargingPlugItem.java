package net.ussm.the_shell.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.blocks.station.SubmarineChargerBlock;
import net.ussm.the_shell.entites.Submarine;
import net.ussm.the_shell.init.ModComponents;
import net.ussm.the_shell.init.ModItems;

// the charger's plug, the cable follows it (see ChargerCable)
public class ChargingPlugItem extends Item {
    public static final double REACH = 12.0;

    public ChargingPlugItem(Properties properties) {
        super(properties);
    }

    public static ItemStack forCharger(BlockPos pos) {
        ItemStack stack = new ItemStack(ModItems.CHARGING_PLUG.get());
        stack.set(ModComponents.CHARGER_POS.get(), pos);
        return stack;
    }

    // back to the charger if it leaves your hand, if you walk too far, or if the charger is gone
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) return;
        BlockPos pos = stack.get(ModComponents.CHARGER_POS.get());
        boolean loaded = pos != null && level.isLoaded(pos);
        BlockState state = loaded ? level.getBlockState(pos) : null;
        boolean charger = state != null && state.getBlock() instanceof SubmarineChargerBlock && state.getValue(SubmarineChargerBlock.IN_USE);
        boolean inHand = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
        boolean close = pos != null && player.distanceToSqr(Vec3.atCenterOf(pos)) <= REACH * REACH;
        if (charger && inHand && close) return;
        stack.setCount(0);
        if (charger) SubmarineChargerBlock.putBack(level, pos);
        if (charger && inHand) player.displayClientMessage(Component.translatable("message.the_shell.charger.too_far"), true);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.level().isClientSide) {
            BlockPos pos = stack.get(ModComponents.CHARGER_POS.get());
            if (pos != null) SubmarineChargerBlock.putBack(entity.level(), pos);
            entity.discard();
        }
        return true;
    }

    public static InteractionResult plugInto(Submarine sub, Player player, ItemStack stack) {
        if (sub.level().isClientSide) return InteractionResult.SUCCESS;
        BlockPos pos = stack.get(ModComponents.CHARGER_POS.get());
        if (pos == null) return InteractionResult.PASS;
        if (sub.getCharger() != null) {
            player.displayClientMessage(Component.translatable("message.the_shell.charger.taken"), true);
        } else if (sub.distanceToSqr(Vec3.atCenterOf(pos)) > REACH * REACH) {
            player.displayClientMessage(Component.translatable("message.the_shell.charger.too_far"), true);
        } else {
            sub.setCharger(pos);
            stack.setCount(0);
            sub.level().playSound(null, sub.blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.NEUTRAL, 0.7F, 1.6F);
            player.displayClientMessage(Component.translatable("message.the_shell.charger.plugged"), true);
        }
        return InteractionResult.CONSUME;
    }
}
