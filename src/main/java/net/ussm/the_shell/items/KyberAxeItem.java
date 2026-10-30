package net.ussm.the_shell.items;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.ussm.the_shell.init.ModItems;
import net.ussm.the_shell.init.ModSounds;

public class KyberAxeItem extends LightsaberItem {
    public KyberAxeItem(Item.Properties properties) {
        super("kyber_axe", "kyber_axe", 9, -3.0F, ModItems.KYBER_SICKLE, properties);
    }

    public static boolean fellsWholeTree(ItemStack stack) {
        return true;
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
