package net.ussm.the_shell.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

// hublot de la cabine du sous-marin : il montre une mer simulee, claire (0), sombre (1) ou les abysses (2) selon ou est le sous-marin
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
