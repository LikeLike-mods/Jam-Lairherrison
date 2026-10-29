package net.ussm.the_shell.worldgen.feature;

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
import net.ussm.the_shell.init.ModBlocks;

import java.util.HashSet;
import java.util.Set;

// Petit rocher de plage (2x2, parfois 3x2), à moitié enfoncé dans le sable ou dans l'eau peu profonde du bord.
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
        BlockPos origin = context.origin();
        BlockState brown = ModBlocks.BROWNSTONE.get().defaultBlockState();
        BlockState grey = ModBlocks.GREY_GRANITE.get().defaultBlockState();

        int sx = 2, sz = 2;
        if (random.nextFloat() < 0.25F) {   // de temps en temps un rocher un peu plus long
            if (random.nextBoolean()) sx = 3; else sz = 3;
        }

        // base : une couche enfouie + une couche visible, avec un ou deux coins manquants
        Set<BlockPos> base = new HashSet<>();
        for (int x = 0; x < sx; x++) {
            for (int z = 0; z < sz; z++) {
                if ((x > 0 || z > 0) && random.nextFloat() < 0.15F) continue;
                base.add(origin.offset(x, 0, z));
            }
        }
        for (BlockPos pos : base) {
            set(level, pos.below(), random, brown, grey);
            set(level, pos, random, brown, grey);
        }
        // un ou deux blocs posés dessus pour casser la forme carrée
        for (BlockPos pos : base) {
            if (random.nextFloat() < 0.3F) set(level, pos.above(), random, brown, grey);
        }
        return true;
    }

    private void set(WorldGenLevel level, BlockPos pos, RandomSource random, BlockState brown, BlockState grey) {
        if (replaceable(level.getBlockState(pos))) {
            this.setBlock(level, pos, random.nextFloat() < 0.12F ? grey : brown);
        }
    }
}
