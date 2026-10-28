package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;

// machine = pompe a air : allumee, les joueurs sous l'eau a 6 blocs autour retrouvent tout leur air
public class MachineBlock extends DecorBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public MachineBlock(Properties properties) {
        super(properties, Shapes.block());
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockState toggled = state.cycle(POWERED);
            level.setBlock(pos, toggled, Block.UPDATE_ALL);
            boolean on = toggled.getValue(POWERED);
            level.playSound(null, pos, on ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.6F, 1.4F);
            if (on) level.scheduleTick(pos, this, 20);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (state.getValue(POWERED) && !oldState.is(this)) level.scheduleTick(pos, this, 20);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) return;
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(6.0))) {
            if (player.isEyeInFluid(FluidTags.WATER)) player.setAirSupply(player.getMaxAirSupply());
        }
        level.scheduleTick(pos, this, 20);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) return;
        if (random.nextInt(30) == 0) {
            level.playLocalSound(pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.3F, 0.7F, false);
        }
        if (state.getValue(WATERLOGGED) || level.getFluidState(pos.above()).is(FluidTags.WATER)) {
            for (int i = 0; i < 2; i++) {
                level.addParticle(ParticleTypes.BUBBLE, pos.getX() + random.nextDouble(), pos.getY() + 1.0, pos.getZ() + random.nextDouble(), 0.0, 0.08, 0.0);
            }
        }
    }
}
