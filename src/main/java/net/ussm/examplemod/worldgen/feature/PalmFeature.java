package net.ussm.examplemod.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.ussm.examplemod.blocks.CoconutBlock;
import net.ussm.examplemod.init.ModBlocks;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

// Palmier construit à la main en jeu, reproduit tel quel avec rotation, miroir et tronc plus ou moins haut.
public class PalmFeature extends Feature<NoneFeatureConfiguration> {
    private static final int LOG = 0, LEAVES = 1;
    private static final int AXIS_Y = 0, AXIS_X = 1, AXIS_Z = 2;
    // {dx, dy, dz, type, axe} depuis le pied du tronc ; dy 0-1 = la base élargie, qui ne monte pas avec le tronc
    private static final int[][] SHAPE = {
            {0, 0, 0, 0, 2}, {0, 0, 1, 0, 2}, {1, 0, 1, 0, 0}, {0, 1, 0, 0, 0}, {1, 1, 1, 0, 1}, {0, 2, 0, 0, 0},
            {0, 3, 0, 0, 0}, {0, 3, 1, 0, 0}, {0, 4, 1, 0, 0}, {0, 4, 2, 0, 0}, {0, 5, 2, 0, 0}, {-1, 6, 1, 1, 0},
            {0, 6, 1, 1, 0}, {1, 6, 1, 1, 0}, {-2, 6, 2, 1, 0}, {-1, 6, 2, 1, 0}, {0, 6, 2, 1, 0}, {1, 6, 2, 1, 0},
            {2, 6, 2, 1, 0}, {-2, 6, 3, 1, 0}, {-1, 6, 3, 1, 0}, {0, 6, 3, 0, 0}, {1, 6, 3, 1, 0}, {2, 6, 3, 1, 0},
            {-2, 6, 4, 1, 0}, {-1, 6, 4, 1, 0}, {0, 6, 4, 1, 0}, {1, 6, 4, 1, 0}, {2, 6, 4, 1, 0}, {-2, 6, 5, 1, 0},
            {-1, 6, 5, 1, 0}, {0, 6, 5, 1, 0}, {1, 6, 5, 1, 0}, {2, 6, 5, 1, 0}, {-1, 6, 6, 1, 0}, {0, 6, 6, 1, 0},
            {1, 6, 6, 1, 0}, {-1, 7, 2, 1, 0}, {0, 7, 2, 1, 0}, {1, 7, 2, 1, 0}, {-1, 7, 3, 1, 0}, {0, 7, 3, 1, 0},
            {1, 7, 3, 1, 0}, {-1, 7, 4, 1, 0}, {0, 7, 4, 1, 0}, {1, 7, 4, 1, 0}, {-1, 7, 5, 1, 0}, {0, 7, 5, 1, 0},
            {1, 7, 5, 1, 0},
    };
    private static final int STRETCH_FROM = 2;   // le tronc s'allonge à partir de cette hauteur
    // emplacements possibles des noix de coco, juste sous la couronne (avant allongement du tronc)
    private static final int[][] COCONUTS = {{1, 5, 3}, {-1, 5, 3}, {0, 5, 4}, {0, 5, 3}};

    private final boolean vines;

    public PalmFeature(Codec<NoneFeatureConfiguration> codec, boolean vines) {
        super(codec);
        this.vines = vines;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        Rotation rotation = Rotation.getRandom(random);
        boolean mirror = random.nextBoolean();
        int extra = random.nextInt(3);

        Map<BlockPos, BlockState> blocks = new HashMap<>();
        for (int[] b : SHAPE) {
            int dy = b[1] >= STRETCH_FROM ? b[1] + extra : b[1];
            put(blocks, origin, rotation, mirror, b[0], dy, b[2], b[3], b[4]);
        }
        for (int i = 0; i < extra; i++) {
            put(blocks, origin, rotation, mirror, 0, STRETCH_FROM + i, 0, LOG, AXIS_Y);
        }

        // On ne pousse pas à travers un autre arbre ou un rocher
        for (BlockPos pos : blocks.keySet()) {
            if (!level.isStateAtPosition(pos, s -> s.isAir() || s.is(BlockTags.REPLACEABLE_BY_TREES))) {
                return false;
            }
        }

        // Distance de chaque feuille à la bûche la plus proche, pour qu'elles ne pourrissent pas toutes seules
        Map<BlockPos, Integer> distance = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        blocks.forEach((pos, state) -> {
            if (state.is(ModBlocks.PALM_LOG.get())) {
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
            if (state.hasProperty(LeavesBlock.DISTANCE)) {
                state = state.setValue(LeavesBlock.DISTANCE, Math.min(7, distance.getOrDefault(pos, 7)));
            }
            this.setBlock(level, pos, state);
        });

        // Lianes (version jungle) : elles pendent des feuilles du bord de la couronne
        if (vines) {
            blocks.forEach((leaf, state) -> {
                if (!state.is(ModBlocks.PALM_LEAVES.get()) || blocks.containsKey(leaf.below())) return;
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    BlockPos pos = leaf.relative(side);
                    if (blocks.containsKey(pos) || random.nextFloat() > 0.22F) continue;
                    BlockState vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(side.getOpposite()), true);
                    for (int i = 1 + random.nextInt(4); i > 0 && level.isEmptyBlock(pos); i--, pos = pos.below()) {
                        this.setBlock(level, pos, vine);
                    }
                }
            });
        }

        // Noix de coco accrochées sous la couronne, autour de la dernière bûche
        BlockState hanging = ModBlocks.COCONUT.get().defaultBlockState();
        for (int[] c : COCONUTS) {
            if (random.nextFloat() < 0.35F) {
                BlockPos pos = at(origin, rotation, mirror, c[0], c[1] + extra, c[2]);
                if (level.isEmptyBlock(pos) && blocks.containsKey(pos.above())) {
                    this.setBlock(level, pos, hanging);
                }
            }
        }
        // Parfois une noix déjà tombée sur le sable, sous la couronne
        if (random.nextFloat() < 0.4F) {
            // on cherche le sol près de la hauteur du pied (le heightmap compterait la couronne)
            BlockPos column = at(origin, rotation, mirror, random.nextInt(5) - 2, 0, 1 + random.nextInt(5));
            for (int dy = 2; dy >= -4; dy--) {
                BlockPos pos = column.above(dy);
                if (level.isEmptyBlock(pos) && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                    if (!blocks.containsKey(pos.below())) {
                        this.setBlock(level, pos, hanging.setValue(CoconutBlock.HANGING, false));
                    }
                    break;
                }
            }
        }
        return true;
    }

    private static BlockPos at(BlockPos origin, Rotation rotation, boolean mirror, int dx, int dy, int dz) {
        return origin.offset(new BlockPos(mirror ? -dx : dx, dy, dz).rotate(rotation));
    }

    private static void put(Map<BlockPos, BlockState> blocks, BlockPos origin, Rotation rotation, boolean mirror,
                            int dx, int dy, int dz, int type, int axis) {
        BlockPos pos = at(origin, rotation, mirror, dx, dy, dz);
        BlockState state;
        if (type == LOG) {
            state = ModBlocks.PALM_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                    axis == AXIS_X ? Direction.Axis.X : axis == AXIS_Z ? Direction.Axis.Z : Direction.Axis.Y);
            state = RotatedPillarBlock.rotatePillar(state, rotation);
        } else {
            state = ModBlocks.PALM_LEAVES.get().defaultBlockState();
        }
        // une bûche gagne toujours sur une feuille au même endroit
        if (type == LOG || !blocks.containsKey(pos)) {
            blocks.put(pos, state);
        }
    }
}
