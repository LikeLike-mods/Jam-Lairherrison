package net.ussm.examplemod.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.ussm.examplemod.init.ModBlocks;

// Rocher arrondi à moitié enfoncé dans le sable (ou dans l'eau peu profonde du bord).
public class BeachRockFeature extends Feature<NoneFeatureConfiguration> {
    public BeachRockFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private static boolean replaceable(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(BlockTags.SAND) || state.is(BlockTags.REPLACEABLE);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos center = context.origin().below();
        float rx = 1.2F + random.nextFloat() * 1.3F, ry = 1.0F + random.nextFloat() * 0.8F, rz = 1.2F + random.nextFloat() * 1.3F;
        BlockState brown = ModBlocks.BROWNSTONE.get().defaultBlockState();
        BlockState grey = ModBlocks.GREY_GRANITE.get().defaultBlockState();
        int r = 3;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    float d = (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) + (dz * dz) / (rz * rz);
                    if (d > 1.0F + random.nextFloat() * 0.35F) continue;   // bords irréguliers
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (replaceable(level.getBlockState(pos))) {
                        this.setBlock(level, pos, random.nextFloat() < 0.12F ? grey : brown);
                    }
                }
            }
        }
        return true;
    }
}
