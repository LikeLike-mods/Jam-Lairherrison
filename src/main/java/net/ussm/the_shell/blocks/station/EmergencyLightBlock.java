package net.ussm.the_shell.blocks.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;

public class EmergencyLightBlock extends IndustrialLampBlock {
    public static final BooleanProperty FLASH = BooleanProperty.create("flash");
    public static final BooleanProperty ALARM = BooleanProperty.create("alarm");

    public EmergencyLightBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FLASH, false).setValue(ALARM, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FLASH, ALARM);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) level.scheduleTick(pos, this, 10);
    }

    @Override
    protected void onToggled(BlockState state, Level level, BlockPos pos) {
        level.scheduleTick(pos, this, 10);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean danger = state.getValue(LIT) && !level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(12.0), m -> m instanceof Enemy && m.isAlive()).isEmpty();
        BlockState next = state.setValue(ALARM, danger).setValue(FLASH, danger && !state.getValue(FLASH));
        if (next != state) {
            level.setBlock(pos, next, Block.UPDATE_ALL);
            if (next.getValue(ALARM) != state.getValue(ALARM)) level.updateNeighborsAt(pos, this);
            if (next.getValue(FLASH)) level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.BLOCKS, 0.4F, 0.5F);
        }
        if (state.getValue(LIT)) level.scheduleTick(pos, this, 10);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ALARM) ? 15 : 0;
    }
}
