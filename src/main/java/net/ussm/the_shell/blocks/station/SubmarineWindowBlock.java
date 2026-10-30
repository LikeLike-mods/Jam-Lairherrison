package net.ussm.the_shell.blocks.station;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class SubmarineWindowBlock extends Block {
    public static final IntegerProperty ZONE = IntegerProperty.create("zone", 0, 2);

    public SubmarineWindowBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ZONE, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ZONE);
    }
}
