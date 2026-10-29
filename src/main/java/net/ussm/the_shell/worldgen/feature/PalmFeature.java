package net.ussm.the_shell.worldgen.feature;

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
import net.ussm.the_shell.blocks.CoconutBlock;
import net.ussm.the_shell.init.ModBlocks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Palmiers en trois tailles, tous avec un pied d'un seul bloc :
// - moyen : le palmier construit à la main en jeu, tronc plus ou moins haut ;
// - petit : jeune palmier, tronc court, petite couronne en étoile ;
// - grand : tronc haut qui penche en plusieurs marches, grande couronne avec des palmes qui retombent.
// Chacun est tourné et mis en miroir au hasard.
public class PalmFeature extends Feature<NoneFeatureConfiguration> {
    private static final int LOG = 0, LEAVES = 1;
    private static final int AXIS_Y = 0, AXIS_X = 1, AXIS_Z = 2;
    // {dx, dy, dz, type, axe} depuis le pied du tronc
    private static final int[][] MEDIUM = {
            {0, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 2, 0, 0, 0},
            {0, 3, 0, 0, 0}, {0, 3, 1, 0, 0}, {0, 4, 1, 0, 0}, {0, 4, 2, 0, 0}, {0, 5, 2, 0, 0}, {-1, 6, 1, 1, 0},
            {0, 6, 1, 1, 0}, {1, 6, 1, 1, 0}, {-2, 6, 2, 1, 0}, {-1, 6, 2, 1, 0}, {0, 6, 2, 1, 0}, {1, 6, 2, 1, 0},
            {2, 6, 2, 1, 0}, {-2, 6, 3, 1, 0}, {-1, 6, 3, 1, 0}, {0, 6, 3, 0, 0}, {1, 6, 3, 1, 0}, {2, 6, 3, 1, 0},
            {-2, 6, 4, 1, 0}, {-1, 6, 4, 1, 0}, {0, 6, 4, 1, 0}, {1, 6, 4, 1, 0}, {2, 6, 4, 1, 0}, {-2, 6, 5, 1, 0},
            {-1, 6, 5, 1, 0}, {0, 6, 5, 1, 0}, {1, 6, 5, 1, 0}, {2, 6, 5, 1, 0}, {-1, 6, 6, 1, 0}, {0, 6, 6, 1, 0},
            {1, 6, 6, 1, 0}, {-1, 7, 2, 1, 0}, {0, 7, 2, 1, 0}, {1, 7, 2, 1, 0}, {-1, 7, 3, 1, 0}, {0, 7, 3, 1, 0},
            {1, 7, 3, 1, 0}, {-1, 7, 4, 1, 0}, {0, 7, 4, 1, 0}, {1, 7, 4, 1, 0}, {-1, 7, 5, 1, 0}, {0, 7, 5, 1, 0},
            {1, 7, 5, 1, 0},
    };
    private static final int STRETCH_FROM = 1;   // le tronc du palmier moyen s'allonge à partir de cette hauteur

    private final boolean vines;

    public PalmFeature(Codec<NoneFeatureConfiguration> codec, boolean vines) {
        super(codec);
        this.vines = vines;
    }

    // ---------------------------------------------------------------- formes

    private static List<int[]> medium(RandomSource random) {
        int extra = random.nextInt(3);
        List<int[]> shape = new ArrayList<>();
        for (int[] b : MEDIUM) {
            shape.add(new int[]{b[0], b[1] >= STRETCH_FROM ? b[1] + extra : b[1], b[2], b[3], b[4]});
        }
        for (int i = 0; i < extra; i++) shape.add(new int[]{0, STRETCH_FROM + i, 0, LOG, AXIS_Y});
        return shape;
    }

    // Tronc vertical d'un bloc de large ; aux hauteurs "steps", il fait une marche d'un bloc vers +z (comme le modèle).
    private static int[] trunk(List<int[]> shape, int height, int... steps) {
        int z = 0;
        for (int y = 0; y < height; y++) {
            shape.add(new int[]{0, y, z, LOG, AXIS_Y});
            for (int s : steps) {
                if (s == y) {
                    z++;
                    shape.add(new int[]{0, y, z, LOG, AXIS_Y});
                }
            }
        }
        return new int[]{0, height - 1, z};
    }

    private static List<int[]> small(RandomSource random) {
        List<int[]> shape = new ArrayList<>();
        int height = 3 + random.nextInt(2);
        int[] top = random.nextBoolean() ? trunk(shape, height, height - 2) : trunk(shape, height);
        int tx = top[0], ty = top[1], tz = top[2];
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) shape.add(new int[]{tx + dx, ty, tz + dz, LEAVES, 0});
            }
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {   // les branches de l'étoile
            shape.add(new int[]{tx + 2 * d.getStepX(), ty, tz + 2 * d.getStepZ(), LEAVES, 0});
            shape.add(new int[]{tx + d.getStepX(), ty + 1, tz + d.getStepZ(), LEAVES, 0});
        }
        shape.add(new int[]{tx, ty + 1, tz, LEAVES, 0});
        return shape;
    }

    private static List<int[]> tall(RandomSource random) {
        List<int[]> shape = new ArrayList<>();
        int height = 9 + random.nextInt(3);
        int[] top = trunk(shape, height, height - 6, height - 4, height - 3);
        int tx = top[0], ty = top[1], tz = top[2];
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 4 && (dx != 0 || dz != 0)) shape.add(new int[]{tx + dx, ty, tz + dz, LEAVES, 0});
                if (Math.max(Math.abs(dx), Math.abs(dz)) <= 2 && Math.abs(dx) + Math.abs(dz) <= 3) shape.add(new int[]{tx + dx, ty + 1, tz + dz, LEAVES, 0});
            }
        }
        // les palmes qui retombent, un bloc plus bas, toujours collées à la couronne (sinon elles pourriraient)
        for (Direction d : Direction.Plane.HORIZONTAL) {
            Direction c = d.getClockWise();
            shape.add(new int[]{tx + 4 * d.getStepX(), ty - 1, tz + 4 * d.getStepZ(), LEAVES, 0});
            shape.add(new int[]{tx + 3 * d.getStepX() + c.getStepX(), ty - 1, tz + 3 * d.getStepZ() + c.getStepZ(), LEAVES, 0});
        }
        shape.add(new int[]{tx, ty + 2, tz, LEAVES, 0});
        return shape;
    }

    // ---------------------------------------------------------------- placement

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        Rotation rotation = Rotation.getRandom(random);
        boolean mirror = random.nextBoolean();

        int roll = random.nextInt(100);
        List<int[]> shape = roll < 30 ? small(random) : roll < 75 ? medium(random) : tall(random);

        Map<BlockPos, BlockState> blocks = new HashMap<>();
        int[] top = null;
        for (int[] b : shape) {
            put(blocks, origin, rotation, mirror, b[0], b[1], b[2], b[3], b[4]);
            if (b[3] == LOG && (top == null || b[1] >= top[1])) top = b;
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
        int[][] spots = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {0, 0}};
        for (int[] c : spots) {
            if (random.nextFloat() < 0.3F) {
                BlockPos pos = at(origin, rotation, mirror, top[0] + c[0], top[1] - 1, top[2] + c[1]);
                if (level.isEmptyBlock(pos) && blocks.containsKey(pos.above())) {
                    this.setBlock(level, pos, hanging);
                }
            }
        }
        // Parfois une noix déjà tombée sur le sable, sous la couronne
        if (random.nextFloat() < 0.4F) {
            // on cherche le sol près de la hauteur du pied (le heightmap compterait la couronne)
            BlockPos column = at(origin, rotation, mirror, top[0] + random.nextInt(5) - 2, 0, top[2] + random.nextInt(5) - 2);
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
