package net.ussm.the_shell.blocks;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class TempleSealBlock extends Block {
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 0, 3);

    public TempleSealBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TIER, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isCreative() || !player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            int altarTier = (state.getValue(TIER) + 1) % 4;
            level.setBlock(pos, state.setValue(TIER, altarTier), Block.UPDATE_CLIENTS);
            player.displayClientMessage(Component.translatable("message.the_shell.seal.tier." + altarTier), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moving) {
        if (level instanceof ServerLevel server && level.hasNeighborSignal(pos)) openSeal(server, pos);
    }

    public static void openSeal(ServerLevel level, BlockPos start) {
        int altarTier = level.getBlockState(start).getValue(TIER);
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty() && seen.size() < 512) {
            BlockPos p = queue.poll();
            for (Direction d : Direction.values()) {
                BlockPos v = p.relative(d);
                BlockState s = level.getBlockState(v);
                if (!seen.contains(v) && s.getBlock() instanceof TempleSealBlock && s.getValue(TIER) == altarTier) {
                    seen.add(v);
                    queue.add(v);
                }
            }
        }
        for (BlockPos p : seen) {
            level.levelEvent(2001, p, Block.getId(level.getBlockState(p)));
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.playSound(null, start, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 0.6F);
    }

    public static void openTier(ServerLevel level, BlockPos altar, int altarTier) {
        for (BlockPos p : BlockPos.betweenClosed(altar.offset(-24, -16, -24), altar.offset(24, 24, 24))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof TempleSealBlock && s.getValue(TIER) == altarTier) openSeal(level, p.immutable());
        }
    }
}
