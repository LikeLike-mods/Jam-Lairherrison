package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.ussm.examplemod.init.ModItems;

// Noix de coco : pend sous les feuilles d'un palmier, tombe quand elle est mûre ou quand on casse les feuilles,
// et reste posée sur le sable une fois tombée.
public class CoconutBlock extends Block implements Fallable {
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    private static final VoxelShape HANGING_SHAPE = Block.box(5, 8, 5, 11, 14, 11);
    private static final VoxelShape GROUND_SHAPE = Block.box(5, 0, 5, 11, 6, 11);

    public CoconutBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HANGING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? HANGING_SHAPE : GROUND_SHAPE;
    }

    private static boolean hasBranch(LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return above.is(BlockTags.LEAVES) || above.is(BlockTags.LOGS);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasBranch(level, pos) || Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(HANGING)) {
            if (direction == Direction.UP && !hasBranch(level, pos)) {
                level.scheduleTick(pos, this, 2);   // plus de feuilles au-dessus : elle tombe
            }
            return state;
        }
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HANGING) && !hasBranch(level, pos)) {
            fall(level, pos, state);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HANGING);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // de temps en temps, une noix mûre se détache toute seule
        if (random.nextInt(45) == 0 && level.isEmptyBlock(pos.below())) {
            fall(level, pos, state);
        }
    }

    private static void fall(ServerLevel level, BlockPos pos, BlockState state) {
        FallingBlockEntity coconut = FallingBlockEntity.fall(level, pos, state);
        coconut.setHurtsEntities(1.0F, 3);   // bonk : jusqu'à un cœur et demi sur la tête
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {
        level.setBlock(pos, state.setValue(HANGING, false), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.WOOD_FALL, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock) {
        Block.popResource(level, pos, new ItemStack(ModItems.COCONUT.get()));
    }
}
