package net.ussm.the_shell.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TemplePedestalBlock extends BaseEntityBlock {
    public static final MapCodec<TemplePedestalBlock> CODEC = simpleCodec(TemplePedestalBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = Shapes.or(Block.box(2, 0, 2, 14, 3, 14), Block.box(4, 3, 4, 12, 12, 12), Block.box(2, 12, 2, 14, 15, 14));

    public TemplePedestalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TemplePedestalBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TemplePedestalBlockEntity pedestal) || !pedestal.item().isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            pedestal.place(stack.copyWithCount(1));
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TemplePedestalBlockEntity pedestal) || pedestal.item().isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack taken = pedestal.item().copy();
            pedestal.place(ItemStack.EMPTY);
            if (!player.getInventory().add(taken)) player.drop(taken, false);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5F, 1.0F);
            level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
            level.updateNeighborsAt(pos.below(), this);
            level.scheduleTick(pos, this, 20);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) return;
        level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
        level.updateNeighborsAt(pos.below(), this);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) && direction == Direction.UP ? 15 : 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof TemplePedestalBlockEntity pedestal) || pedestal.item().isEmpty()) return;
        for (int i = 0; i < 2; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            level.addParticle(random.nextInt(3) == 0 ? ParticleTypes.END_ROD : ParticleTypes.WAX_ON,
                    pos.getX() + 0.5 + Math.cos(a) * 0.45, pos.getY() + 1.0 + random.nextDouble() * 0.5, pos.getZ() + 0.5 + Math.sin(a) * 0.45,
                    0.0, 0.02 + random.nextDouble() * 0.02, 0.0);
        }
    }
}
