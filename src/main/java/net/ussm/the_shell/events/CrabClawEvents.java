package net.ussm.the_shell.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.init.ModAttachments;
import net.ussm.the_shell.init.ModItems;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class CrabClawEvents {
    private static final ResourceLocation REACH_ID = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "crab_claw_reach");
    private static final double REACH_BONUS = 10.0;
    private static final int MAX_WEAR = 12;

    private static boolean isClaw(ItemStack stack) {
        return stack.is(ModItems.CRAB_CLAW.get());
    }

    @SubscribeEvent
    public static void updateReach(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        AttributeInstance reach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reach == null) return;
        boolean hasClaw = player.getInventory().contains(CrabClawEvents::isClaw);
        boolean hasBonus = reach.hasModifier(REACH_ID);
        if (hasClaw && !hasBonus) {
            reach.addTransientModifier(new AttributeModifier(REACH_ID, REACH_BONUS, AttributeModifier.Operation.ADD_VALUE));
        } else if (!hasClaw && hasBonus) {
            reach.removeModifier(REACH_ID);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        wearIfExtraReach(event.getPlayer(), event.getPos());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            wearIfExtraReach(player, event.getPos());
        }
    }

    private static void wearIfExtraReach(Player player, BlockPos pos) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (player.isCreative() || player.isSpectator()) return;
        AttributeInstance reach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reach == null || !reach.hasModifier(REACH_ID)) return;
        double normal = player.blockInteractionRange() - REACH_BONUS;
        if (new AABB(pos).distanceToSqr(player.getEyePosition()) < normal * normal) return;

        int wear = player.getData(ModAttachments.CRAB_CLAW_WEAR) + 1;
        if (wear >= MAX_WEAR) {
            wear = 0;
            if (consumeOneClaw(player)) {
                serverPlayer.playNotifySound(SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F,
                        0.8F + player.getRandom().nextFloat() * 0.4F);
            }
        }
        player.setData(ModAttachments.CRAB_CLAW_WEAR, wear);
    }

    private static boolean consumeOneClaw(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (isClaw(stack)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void addTooltip(ItemTooltipEvent event) {
        if (!isClaw(event.getItemStack())) return;
        event.getToolTip().add(Component.translatable("tooltip.the_shell.crab_claw.reach").withStyle(ChatFormatting.GRAY));
        if (event.getEntity() != null) {
            int wear = event.getEntity().getData(ModAttachments.CRAB_CLAW_WEAR);
            event.getToolTip().add(Component.translatable("tooltip.the_shell.crab_claw.wear", wear, MAX_WEAR).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
