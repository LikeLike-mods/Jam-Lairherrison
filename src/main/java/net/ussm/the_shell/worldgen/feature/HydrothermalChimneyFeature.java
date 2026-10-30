package net.ussm.the_shell.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;
import net.ussm.the_shell.blocks.abyss.ChimneyBlock;
import net.ussm.the_shell.init.ModBlocks;

public class HydrothermalChimneyFeature extends Feature<NoneFeatureConfiguration> {
    public HydrothermalChimneyFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (!level.getBlockState(origin).is(Blocks.WATER) || !level.getBlockState(origin.below()).isFaceSturdy(level, origin.below(), Direction.UP)) {
            return false;
        }

        // la croute autour du pied
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx * dx + dz * dz > 9 + random.nextInt(3)) continue;
                BlockPos sol = origin.offset(dx, -1, dz);
                if (level.getBlockState(sol).isFaceSturdy(level, sol, Direction.UP) && !level.getBlockState(sol).is(Blocks.BEDROCK)) {
                    level.setBlock(sol, random.nextInt(4) == 0 ? ModBlocks.SULFUR_BLOCK.get().defaultBlockState() : ModBlocks.ABYSSAL_BASALT.get().defaultBlockState(), 2);
                }
            }
        }

        column(level, origin, 4 + random.nextInt(7));
        int petites = random.nextInt(4);
        for (int i = 0; i < petites; i++) {
            BlockPos p = origin.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
            if (!p.equals(origin)) column(level, p, 2 + random.nextInt(3));
        }
        return true;
    }

    private void column(WorldGenLevel level, BlockPos base, int height) {
        BlockPos.MutableBlockPos p = base.mutable();
        for (int i = 0; i < 4 && level.getBlockState(p.below()).is(Blocks.WATER); i++) p.move(Direction.DOWN);
        for (int i = 0; i < 4 && !level.getBlockState(p).is(Blocks.WATER); i++) p.move(Direction.UP);
        if (!level.getBlockState(p).is(Blocks.WATER) || !level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)) return;
        int built = 0;
        while (built < height && level.getBlockState(p.above(built)).is(Blocks.WATER)) built++;
        for (int i = 0; i < built; i++, p.move(Direction.UP)) {
            BlockState state = ModBlocks.HYDROTHERMAL_CHIMNEY.get().defaultBlockState()
                    .setValue(ChimneyBlock.TOP, i == built - 1)
                    .setValue(ChimneyBlock.WATERLOGGED, level.getFluidState(p).is(Fluids.WATER));
            level.setBlock(p, state, 2);
        }
    }
}
