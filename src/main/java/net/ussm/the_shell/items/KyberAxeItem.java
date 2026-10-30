package net.ussm.the_shell.items;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.ussm.the_shell.init.ModComponents;
import net.ussm.the_shell.init.ModSounds;

public class KyberAxeItem extends LightsaberItem {
    public KyberAxeItem(Item.Properties properties) {
        super("kyber_axe", "kyber_axe", 9, -3.0F, () -> null, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.consume(stack);
        if (player.isShiftKeyDown()) {
            boolean tree = !fellsWholeTree(stack);
            stack.set(ModComponents.FELL_TREE, tree);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.LIGHTSABER_IGNITE.get(), SoundSource.PLAYERS, 0.6F, tree ? 0.7F : 1.2F);
            player.displayClientMessage(Component.translatable(tree ? "message.the_shell.kyber_axe.tree" : "message.the_shell.kyber_axe.single"), true);
            return InteractionResultHolder.consume(stack);
        }
        boolean active = !isActive(stack);
        stack.set(ModComponents.ACTIVE, active);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                active ? ModSounds.LIGHTSABER_IGNITE.get() : ModSounds.LIGHTSABER_EXTINGUISH.get(), SoundSource.PLAYERS, 0.6F, 0.8F);
        return InteractionResultHolder.consume(stack);
    }

    public static boolean fellsWholeTree(ItemStack stack) {
        return stack.getOrDefault(ModComponents.FELL_TREE, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(fellsWholeTree(stack) ? "message.the_shell.kyber_axe.tree" : "message.the_shell.kyber_axe.single").withStyle(net.minecraft.ChatFormatting.AQUA));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isActive(stack) && state.is(BlockTags.MINEABLE_WITH_AXE) ? 14.0F : 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_AXE);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability);
    }

    public static void fellTree(ServerLevel level, BlockPos start, Player player) {
        Set<BlockPos> logs = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        logs.add(start);
        while (!queue.isEmpty() && logs.size() < 256) {
            BlockPos p = queue.poll();
            for (BlockPos v : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
                if (Math.abs(v.getX() - start.getX()) > 12 || Math.abs(v.getZ() - start.getZ()) > 12 || v.getY() < start.getY()) continue;
                if (!logs.contains(v) && level.getBlockState(v).is(BlockTags.LOGS)) {
                    BlockPos copied = v.immutable();
                    logs.add(copied);
                    queue.add(copied);
                }
            }
        }
        Set<BlockPos> leaves = new HashSet<>();
        ArrayDeque<BlockPos> edge = new ArrayDeque<>(logs);
        for (int step = 0; step < 7 && leaves.size() < 600; step++) {
            ArrayDeque<BlockPos> next = new ArrayDeque<>();
            for (BlockPos p : edge) {
                for (BlockPos v : new BlockPos[]{p.above(), p.below(), p.north(), p.south(), p.east(), p.west()}) {
                    BlockState s = level.getBlockState(v);
                    if (!leaves.contains(v) && s.is(BlockTags.LEAVES) && !(s.hasProperty(LeavesBlock.PERSISTENT) && s.getValue(LeavesBlock.PERSISTENT))) {
                        leaves.add(v);
                        next.add(v);
                    }
                }
            }
            edge = next;
        }
        logs.remove(start);
        for (BlockPos p : logs) level.destroyBlock(p, true, player);
        for (BlockPos p : leaves) level.destroyBlock(p, true, player);
        level.playSound(null, start, ModSounds.LIGHTSABER_EXTINGUISH.get(), SoundSource.PLAYERS, 1.0F, 0.6F);
    }
}
