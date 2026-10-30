package net.ussm.the_shell.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.ussm.the_shell.events.TempleQuiz;

public class NotchAltarBlock extends Block {
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 1, 3);
    public static final BooleanProperty SOLVED = BooleanProperty.create("solved");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 2, 16), Block.box(2, 2, 2, 14, 12, 14), Block.box(1, 12, 1, 15, 15, 15));

    public NotchAltarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TIER, 1).setValue(SOLVED, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER, SOLVED, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player.isCreative() && player.isShiftKeyDown()) {
            int altarTier = state.getValue(TIER) % 3 + 1;
            level.setBlock(pos, state.setValue(TIER, altarTier).setValue(SOLVED, false), Block.UPDATE_ALL);
            player.displayClientMessage(Component.translatable("message.the_shell.notch.tier", altarTier), true);
            return InteractionResult.SUCCESS;
        }
        if (!state.getValue(SOLVED) && earlierTierUnsolved((ServerLevel) level, pos, state.getValue(TIER))) {
            player.sendSystemMessage(Component.literal("<Notch> ").append(Component.translatable("message.the_shell.notch.order").withStyle(ChatFormatting.GRAY)));
            return InteractionResult.SUCCESS;
        }
        if (state.getValue(SOLVED)) {
            player.sendSystemMessage(Component.literal("<Notch> ").append(Component.translatable("message.the_shell.notch.done").withStyle(ChatFormatting.GRAY)));
        } else {
            TempleQuiz.askQuestion((ServerPlayer) player, pos, state.getValue(TIER));
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean earlierTierUnsolved(ServerLevel level, BlockPos pos, int altarTier) {
        if (altarTier == 1) return false;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-24, -32, -24), pos.offset(24, 32, 24))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof NotchAltarBlock && s.getValue(TIER) == altarTier - 1 && !s.getValue(SOLVED)) return true;
        }
        return false;
    }

    public static void markSolved(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof NotchAltarBlock) level.setBlock(pos, state.setValue(SOLVED, true), Block.UPDATE_ALL);
    }
}
