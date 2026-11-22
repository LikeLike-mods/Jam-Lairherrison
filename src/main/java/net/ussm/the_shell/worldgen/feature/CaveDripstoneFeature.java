package net.ussm.the_shell.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import static net.ussm.the_shell.init.ModBlocks.BROWNSTONE;

public class CaveDripstoneFeature extends Feature<NoneFeatureConfiguration> {

    public CaveDripstoneFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private boolean isCaveAir(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getFluidState(pos).isEmpty();
    }

    private boolean canAnchor(WorldGenLevel level, BlockPos pos, Direction face) {
        BlockState state = level.getBlockState(pos);
        Block block = level.getBlockState(pos).getBlock();
        return state.isFaceSturdy(level, pos, face) && (block == BROWNSTONE.get());
    }

    private int randomLength(RandomSource random) {
        int roll = random.nextInt(100);

        if (roll < 40) return 1;
        if (roll < 70) return 2;
        if (roll < 90) return 3;
        return 4;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int placed = 0;
        int clusters = 3 + random.nextInt(5);
        java.util.List<BlockPos> existing = new java.util.ArrayList<>();

        for (int i = 0; i < clusters; i++) {
            int centerX = origin.getX() + random.nextInt(16);
            int centerZ = origin.getZ() + random.nextInt(16);
            int centerY = 103 + random.nextInt(138);

            int count = 2 + random.nextInt(5);
            for (int n = 0; n < count; n++) {

                int x = centerX + random.nextInt(7) - 3;
                int z = centerZ + random.nextInt(7) - 3;
                int y = centerY + random.nextInt(5) - 2;

                BlockPos pos = new BlockPos(x, y, z);

                boolean tooClose = existing.stream().anyMatch(p ->
                        Math.abs(p.getX() - pos.getX()) < 2
                                && Math.abs(p.getZ() - pos.getZ()) < 2
                                && Math.abs(p.getY() - pos.getY()) < 3
                );
                if (tooClose) continue;
                existing.add(pos);

                if (!level.getBlockState(pos).isAir()) continue;

                boolean ceiling = canAnchor(level, pos.above(), Direction.DOWN);
                boolean floor = canAnchor(level, pos.below(), Direction.UP);
                if (!isCaveAir(level, pos)) continue;
                if (!ceiling && !floor) continue;
                if (ceiling && floor) continue;

                int airCount = 0;
                for (int d = 1; d <= 3; d++) {
                    if (isCaveAir(level, pos.above(d))
                            && isCaveAir(level, pos.below(d))) {
                        airCount++;
                    }
                }
                if (airCount < 2) continue;

                boolean stalactite = ceiling;

                int length = randomLength(random);

                BlockPos anchor = stalactite ? pos.above() : pos.below();
                if (!level.getBlockState(anchor).isFaceSturdy(
                        level, anchor,
                        stalactite
                                ? net.minecraft.core.Direction.DOWN
                                : net.minecraft.core.Direction.UP)) {
                    continue;
                }

                for (int j = 0; j < length; j++) {

                    BlockPos target = stalactite
                            ? pos.below(j)
                            : pos.above(j);
                    if (!level.getBlockState(target).isAir()) break;

                    String thicknessName;
                    if (length == 1) {
                        thicknessName = "tip";
                    } else if (j == 0) {
                        thicknessName = "base";
                    } else if (j == length - 1) {
                        thicknessName = "tip";
                    } else if (j == length - 2) {
                        thicknessName = "frustum";
                    } else {
                        thicknessName = "middle";
                    }

                    var thickness = PointedDripstoneBlock.THICKNESS
                            .getPossibleValues()
                            .stream()
                            .filter(v -> v.getSerializedName().equals(thicknessName))
                            .findFirst()
                            .orElseThrow();

                    BlockState state = Blocks.POINTED_DRIPSTONE.defaultBlockState()
                            .setValue(
                                    PointedDripstoneBlock.TIP_DIRECTION,
                                    stalactite ? Direction.DOWN : Direction.UP
                            )
                            .setValue(PointedDripstoneBlock.THICKNESS, thickness);
                    level.setBlock(target, state, 2);
                    placed++;
                }
            }
        }

        return placed > 0;
    }
}
