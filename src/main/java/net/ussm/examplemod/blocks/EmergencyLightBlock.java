package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

// lampe d'urgence : quand elle est allumee elle clignote comme une alarme
public class EmergencyLightBlock extends IndustrialLampBlock {
    public static final BooleanProperty FLASH = BooleanProperty.create("flash");

    public EmergencyLightBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FLASH, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FLASH);
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
        if (state.getValue(LIT)) {
            level.setBlock(pos, state.cycle(FLASH), Block.UPDATE_CLIENTS);
            level.scheduleTick(pos, this, 10);
        } else if (state.getValue(FLASH)) {
            level.setBlock(pos, state.setValue(FLASH, false), Block.UPDATE_CLIENTS);
        }
    }
}
