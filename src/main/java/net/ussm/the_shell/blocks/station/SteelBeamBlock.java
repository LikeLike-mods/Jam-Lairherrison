package net.ussm.the_shell.blocks.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// an I-shaped beam: two flanges and a web, not a full block
public class SteelBeamBlock extends RotatedPillarBlock {
    private static final VoxelShape UP = Shapes.or(Block.box(2, 0, 2, 14, 16, 5), Block.box(2, 0, 11, 14, 16, 14), Block.box(6, 0, 5, 10, 16, 11));
    private static final VoxelShape ALONG_Z = Shapes.or(Block.box(2, 2, 0, 14, 5, 16), Block.box(2, 11, 0, 14, 14, 16), Block.box(6, 5, 0, 10, 11, 16));
    private static final VoxelShape ALONG_X = Shapes.or(Block.box(0, 2, 2, 16, 5, 14), Block.box(0, 11, 2, 16, 14, 14), Block.box(0, 5, 6, 16, 11, 10));

    public SteelBeamBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(AXIS)) {
            case X -> ALONG_X;
            case Z -> ALONG_Z;
            default -> UP;
        };
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
}
