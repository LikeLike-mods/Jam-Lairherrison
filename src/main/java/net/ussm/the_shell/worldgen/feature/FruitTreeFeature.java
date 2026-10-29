package net.ussm.the_shell.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.ussm.the_shell.init.ModBlocks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// bananier (petit tronc, grandes feuilles qui retombent, un regime dessous)
// et palmier dattier (grand tronc droit, couronne de palmes, des grappes de dattes)
public class FruitTreeFeature extends Feature<NoneFeatureConfiguration> {
    private final boolean datePalm;

    public FruitTreeFeature(Codec<NoneFeatureConfiguration> codec, boolean datePalm) {
        super(codec);
        this.datePalm = datePalm;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        BlockState log = (datePalm ? ModBlocks.DATE_PALM_LOG : ModBlocks.BANANA_STEM).get().defaultBlockState();
        BlockState leaves = (datePalm ? ModBlocks.DATE_PALM_LEAVES : ModBlocks.BANANA_LEAVES).get().defaultBlockState();
        BlockState fruit = (datePalm ? ModBlocks.DATE_CLUSTER : ModBlocks.BANANA_BUNCH).get().defaultBlockState();

        int height = datePalm ? 8 + random.nextInt(3) : 5 + random.nextInt(2);
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        for (int y = 0; y < height; y++) blocks.put(origin.above(y), log);
        BlockPos top = origin.above(height - 1);

        // chaque palme : {pas devant, pas sur le cote, hauteur}. elles montent un peu puis retombent
        // (jamais plus de 6 blocs du tronc en passant par les feuilles, sinon elles pourrissent)
        int[][] frond = datePalm
                ? new int[][]{{1, 0, 0}, {2, 0, 0}, {3, 0, 0}, {3, 0, -1}, {4, 0, -1}, {4, 0, -2}, {1, 0, 1}, {1, 0, 2}}
                : new int[][]{{1, 0, 0}, {1, 0, 1}, {2, 0, 1}, {2, 0, 0}, {3, 0, 0}, {3, 0, -1}};
        int[][] between = datePalm
                ? new int[][]{{1, 1, 0}, {2, 1, 0}, {2, 2, 0}, {2, 2, -1}, {3, 2, -1}}
                : new int[][]{{1, 1, 0}, {1, 1, -1}};
        blocks.putIfAbsent(top.above(), leaves);
        blocks.putIfAbsent(top.above(2), leaves);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            Direction c = d.getClockWise();
            for (int[] f : frond) blocks.putIfAbsent(top.relative(d, f[0]).relative(c, f[1]).above(f[2]), leaves);
            for (int[] f : between) blocks.putIfAbsent(top.relative(d, f[0]).relative(c, f[1]).above(f[2]), leaves);
        }

        for (BlockPos pos : blocks.keySet()) {
            if (!level.isStateAtPosition(pos, s -> s.isAir() || s.is(BlockTags.REPLACEABLE_BY_TREES))) return false;
        }

        // distance des feuilles = distance au tronc en passant par l'arbre (sinon elles pourrissent)
        Map<BlockPos, Integer> distance = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        blocks.forEach((pos, state) -> {
            if (state == log) {
                distance.put(pos, 0);
                queue.add(pos);
            }
        });
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (blocks.containsKey(next) && !distance.containsKey(next)) {
                    distance.put(next, distance.get(pos) + 1);
                    queue.add(next);
                }
            }
        }
        blocks.forEach((pos, state) -> {
            if (state.hasProperty(LeavesBlock.DISTANCE)) state = state.setValue(LeavesBlock.DISTANCE, Math.min(7, distance.getOrDefault(pos, 7)));
            this.setBlock(level, pos, state);
        });

        // les fruits pendent juste sous la couronne
        List<Direction> sides = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
        Collections.shuffle(sides, new java.util.Random(random.nextLong()));
        int fruits = datePalm ? 2 + random.nextInt(2) : 1 + random.nextInt(2);
        for (int k = 0; k < fruits; k++) {
            BlockPos pos = top.relative(sides.get(k)).below();
            if (level.isEmptyBlock(pos)) this.setBlock(level, pos, fruit);
        }
        return true;
    }
}
