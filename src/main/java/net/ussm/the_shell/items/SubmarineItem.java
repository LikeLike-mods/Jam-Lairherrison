package net.ussm.the_shell.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.entites.Submarine;
import net.ussm.the_shell.events.SubmarineInterior;

public class SubmarineItem extends Item {

    public SubmarineItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.dimension() == SubmarineInterior.INTERIOR) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.the_shell.submarine.no_room"), true);
            if (player instanceof ServerPlayer joueur) {
                AdvancementHolder succes = joueur.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("the_shell", "inception"));
                if (succes != null) joueur.getAdvancements().award(succes, "inception");
            }
            return InteractionResultHolder.fail(stack);
        }
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);
        Vec3 pos = hit.getLocation();
        Submarine submarine = new Submarine(level, pos.x, pos.y, pos.z, player.getYRot());
        if (!level.noCollision(submarine, submarine.getBoundingBox())) return InteractionResultHolder.fail(stack);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (tag.contains("Tier")) submarine.setTier(tag.getInt("Tier"));
            if (tag.contains("Energy")) submarine.setEnergy(tag.getInt("Energy"));
            if (tag.contains("Interior") && SubmarineInterior.claim((ServerLevel) level, tag.getInt("Interior"))) submarine.setInteriorId(tag.getInt("Interior"));
            level.addFreshEntity(submarine);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
            stack.consume(1, player);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("Tier")) return;
        tooltip.add(Component.translatable("item.the_shell.submarine.tier", tag.getInt("Tier")).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.the_shell.submarine.energy", tag.getInt("Energy") * 100 / Submarine.MAX_ENERGY).withStyle(ChatFormatting.GRAY));
    }
}
