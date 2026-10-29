package net.ussm.the_shell.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

// une petite boule irreguliere d'un bloc posee sur le fond (corail lumineux du recif, tas de soufre...)
public class AbyssBlobFeature extends Feature<SimpleBlockConfiguration> {
    public AbyssBlobFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (!level.getBlockState(origin).is(Blocks.WATER)) return false;
        float r = 1.2F + random.nextFloat() * 1.3F;
        int n = (int) Math.ceil(r);
        boolean placed = false;
        for (int dx = -n; dx <= n; dx++) {
            for (int dy = -1; dy <= n; dy++) {
                for (int dz = -n; dz <= n; dz++) {
                    float d = dx * dx + dy * dy * 1.6F + dz * dz;
                    if (d > r * r + random.nextFloat() * 0.8F) continue;
                    BlockPos p = origin.offset(dx, dy, dz);
                    if (level.getBlockState(p).is(Blocks.WATER) || (dy < 0 && !level.getBlockState(p).is(Blocks.BEDROCK))) {
                        level.setBlock(p, context.config().toPlace().getState(random, p), 2);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }
}
