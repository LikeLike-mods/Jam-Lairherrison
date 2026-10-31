package net.ussm.the_shell.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.ussm.the_shell.blocks.archipelago.TitanoboaShedBlock;
import net.ussm.the_shell.init.ModBlocks;

public class TitanoboaShedFeature extends Feature<NoneFeatureConfiguration> {
    public TitanoboaShedFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos.MutableBlockPos pos = context.origin().mutable();
        int length = 3 + random.nextInt(5);
        int placed = 0;
        for (int i = 0; i < length; i++) {
            BlockState state = ModBlocks.TITANOBOA_SHED.get().defaultBlockState().setValue(TitanoboaShedBlock.FACING, direction);
            BlockPos ground = null;
            for (int dy = 2; dy >= -3 && ground == null; dy--) {
                BlockPos at = pos.offset(0, dy, 0);
                if (level.isEmptyBlock(at) && state.canSurvive(level, at)) ground = at;
            }
            if (ground != null) {
                level.setBlock(ground, state, 2);
                pos.setY(ground.getY());
                placed++;
            }
            pos.move(direction);
            if (random.nextFloat() < 0.3F) direction = random.nextBoolean() ? direction.getClockWise() : direction.getCounterClockWise();
        }
        return placed > 0;
    }
}
