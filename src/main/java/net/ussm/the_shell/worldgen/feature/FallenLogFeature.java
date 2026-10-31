package net.ussm.the_shell.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.ussm.the_shell.blocks.archipelago.CoconutBlock;
import net.ussm.the_shell.init.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class FallenLogFeature extends Feature<NoneFeatureConfiguration> {
    private final int minLength, maxLength;
    private final boolean withCrown;

    public FallenLogFeature(Codec<NoneFeatureConfiguration> codec, int minLength, int maxLength, boolean withCrown) {
        super(codec);
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.withCrown = withCrown;
    }

    private static boolean free(WorldGenLevel level, BlockPos pos) {
        return level.isStateAtPosition(pos, s -> s.isAir() || s.is(BlockTags.REPLACEABLE_BY_TREES));
    }

    private static boolean ground(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int length = minLength + random.nextInt(maxLength - minLength + 1);

        List<BlockPos> logs = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            BlockPos pos = origin.relative(direction, i);
            if (!free(level, pos) || !ground(level, pos)) break;
            logs.add(pos);
        }
        if (logs.size() < minLength) return false;

        BlockState log = ModBlocks.PALM_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, direction.getAxis());
        for (BlockPos pos : logs) this.setBlock(level, pos, log);

        if (withCrown) {
            BlockState leaves = ModBlocks.PALM_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
            BlockPos head = logs.get(logs.size() - 1).relative(direction);
            for (int a = -1; a <= 2; a++) {
                for (int b = -2; b <= 2; b++) {
                    if (Math.abs(b) == 2 && (a == -1 || a == 2) || random.nextFloat() < 0.15F) continue;
                    BlockPos pos = head.relative(direction, a).relative(direction.getClockWise(), b);
                    for (int dy = 1; dy >= -1; dy--) { // hugs the terrain
                        BlockPos p = pos.above(dy);
                        if (free(level, p) && ground(level, p)) {
                            this.setBlock(level, p, leaves);
                            break;
                        }
                    }
                }
            }
            if (random.nextFloat() < 0.5F) {
                BlockPos nut = head.relative(direction.getCounterClockWise(), 2);
                if (free(level, nut) && ground(level, nut)) {
                    this.setBlock(level, nut, ModBlocks.COCONUT.get().defaultBlockState().setValue(CoconutBlock.HANGING, false));
                }
            }
        }
        return true;
    }
}
