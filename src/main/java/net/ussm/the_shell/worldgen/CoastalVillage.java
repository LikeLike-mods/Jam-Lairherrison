package net.ussm.the_shell.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.util.RandomSource;
import net.ussm.the_shell.init.ModStructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// fishing village laid out from the shore: one half on the beach, the other on piers over the water
public class CoastalVillage extends Structure {
    public static final MapCodec<CoastalVillage> CODEC = simpleCodec(CoastalVillage::new);
    private static final int SEA = TestChunkGenerator.SEA;
    private static final int DECK = SEA + 1;
    private static final StructureTemplatePool.Projection RIGID = StructureTemplatePool.Projection.RIGID;
    private static final StructureTemplatePool.Projection GROUND = StructureTemplatePool.Projection.TERRAIN_MATCHING;
    // anchors in template coordinates: the block in front of the door / the middle of an edge
    private static final BlockPos PLAZA = new BlockPos(5, 1, 5), SQUARE = new BlockPos(3, 1, 3), DECK_PLAZA = new BlockPos(5, 12, 5);
    private static final BlockPos STREET = new BlockPos(1, 0, 0), PIER = new BlockPos(1, 12, 0);

    public CoastalVillage(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.COASTAL_VILLAGE.get();
    }

    private static int ground(GenerationContext c, int x, int z) {
        return c.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, c.heightAccessor(), c.randomState()) - 1;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext c) {
        int x0 = c.chunkPos().getMiddleBlockX(), z0 = c.chunkPos().getMiddleBlockZ();
        int g0 = ground(c, x0, z0);
        if (g0 < SEA || g0 > SEA + 5) return Optional.empty();
        // the sea is the side where the water comes first, and the other side has to stay dry for a while
        Direction sea = null;
        int shore = 99;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int k = 6; k <= 36; k += 2) {
                if (ground(c, x0 + d.getStepX() * k, z0 + d.getStepZ() * k) < SEA - 1) {
                    if (k < shore) { shore = k; sea = d; }
                    break;
                }
            }
        }
        if (sea == null) return Optional.empty();
        for (int k = 8; k <= 24; k += 4) {
            if (ground(c, x0 - sea.getStepX() * k, z0 - sea.getStepZ() * k) < SEA) return Optional.empty();
        }
        Direction f = sea;
        int s = shore;
        BlockPos o = new BlockPos(x0, g0, z0);
        return Optional.of(new GenerationStub(o, b -> new Layout(c, b, o, f).build(s)));
    }

    private static final class Layout {
        final GenerationContext c;
        final StructurePiecesBuilder b;
        final BlockPos o;
        final Direction f, r;
        final RandomSource rand;
        final List<BoundingBox> boxes = new ArrayList<>();
        final java.util.Map<String, Integer> used = new java.util.HashMap<>();

        Layout(GenerationContext c, StructurePiecesBuilder b, BlockPos o, Direction f) {
            this.c = c; this.b = b; this.o = o; this.f = f; this.r = f.getClockWise(); this.rand = c.random();
        }

        BlockPos at(int along, int side) {
            return o.relative(f, along).relative(r, side);
        }

        boolean wet(BlockPos p) {
            return ground(c, p.getX(), p.getZ()) < SEA;
        }

        int land(BlockPos p) {
            return Math.max(ground(c, p.getX(), p.getZ()), SEA);
        }

        static Rotation facing(Direction d) {
            return switch (d) {
                case EAST -> Rotation.CLOCKWISE_90;
                case SOUTH -> Rotation.CLOCKWISE_180;
                case WEST -> Rotation.COUNTERCLOCKWISE_90;
                default -> Rotation.NONE;
            };
        }

        // puts a template so that its anchor lands on the given block, front turned towards `front`
        boolean place(String name, StructureTemplatePool.Projection proj, BlockPos anchor, BlockPos local, Direction front, boolean force) {
            Rotation rot = facing(front);
            StructurePoolElement el = StructurePoolElement.single("the_shell:village/" + name).apply(proj);
            StructureTemplateManager tm = c.structureTemplateManager();
            BlockPos pos = anchor.subtract(StructureTemplate.transform(local, Mirror.NONE, rot, BlockPos.ZERO));
            BoundingBox box = el.getBoundingBox(tm, pos, rot);
            if (!force) for (BoundingBox other : boxes) if (other.intersects(box)) return false;
            boxes.add(box);
            b.addPiece(new PoolElementStructurePiece(tm, el, pos, el.getGroundLevelDelta(), rot, box, LiquidSettings.IGNORE_WATERLOGGING));
            return true;
        }

        // a stretch of street: planks on the sand, a pier on posts over the water. Runs from `from` towards `dir`
        void road(BlockPos from, Direction dir, int length) {
            for (int k = 0; k < length; k += 9) {
                BlockPos p = from.relative(dir, k);
                if (wet(p.relative(dir, 4))) place("pier", RIGID, p.atY(DECK), PIER, dir.getOpposite(), true);
                else place("street", GROUND, p.atY(land(p)), STREET, dir.getOpposite(), true);
            }
        }

        // what can stand on the beach and what can stand over the water (stilts), repeats = more likely
        static final String[] LAND = {"house", "house", "house", "fisher_hut", "fisher_hut", "market", "tavern", "smokehouse", "garden", "tower"};
        static final String[] WATER = {"house", "house", "fisher_hut", "fisher_hut", "fisher_hut", "tavern", "tower", "boathouse", "boathouse"};

        // block in front of the door (under the first step for stilt houses), in template coordinates
        static BlockPos anchor(String name) {
            return switch (name) {
                case "house" -> new BlockPos(4, 9, 0);
                case "fisher_hut" -> new BlockPos(3, 9, 0);
                case "tavern" -> new BlockPos(5, 9, 0);
                case "tower" -> new BlockPos(3, 9, 0);
                case "boathouse" -> new BlockPos(4, 12, 0);
                case "garden" -> new BlockPos(4, 0, 0);
                default -> new BlockPos(3, 0, 0);
            };
        }

        // a building on one side of a road, door towards it; falls back to a small hut if the big one doesn't fit
        void building(BlockPos roadside, Direction towardRoad) {
            boolean water = wet(roadside);
            BlockPos door = roadside.atY(water ? DECK : land(roadside));
            String[] pool = water ? WATER : LAND;
            String name = pool[rand.nextInt(pool.length)];
            if (List.of("tower", "tavern", "market").contains(name) && used.merge(name, 1, Integer::sum) > 2) name = "house";   // two of each at most
            if (!place(name, RIGID, door, anchor(name), towardRoad, false) && !name.equals("fisher_hut"))
                place("fisher_hut", RIGID, door, anchor("fisher_hut"), towardRoad, false);
        }

        void build(int shore) {
            place("plaza", RIGID, o, PLAZA, f, true);
            // the main street goes from the plaza out to sea
            int length = shore + 22;
            BlockPos start = at(6, 0);
            road(start, f, length);
            for (int k = 6; k < length - 4; k += 11) {
                building(at(6 + k, 2), r.getOpposite());
                building(at(6 + k, -2), r);
            }
            // out at sea: a deck plaza, and a pier on each side of it lined with stilt houses
            int endA = 6 + (length + 8) / 9 * 9 + 5;
            BlockPos end = at(endA, 0);
            if (wet(end)) {
                place("water_plaza", RIGID, end.atY(DECK), DECK_PLAZA, f, false);
                for (int side : new int[]{1, -1}) {
                    Direction way = side > 0 ? r : r.getOpposite();
                    BlockPos p0 = at(endA, 6 * side);
                    road(p0, way, 27);
                    for (int k = 4; k < 27; k += 11) {
                        building(p0.relative(way, k).relative(f.getOpposite(), 2), f);
                        building(p0.relative(way, k).relative(f, 2), f.getOpposite());
                    }
                }
            } else {
                place("plaza", RIGID, end.atY(land(end)), PLAZA, f, false);
            }
            // the beach street, along the shore, with a little square at each end
            for (int side : new int[]{1, -1}) {
                Direction way = side > 0 ? r : r.getOpposite();
                BlockPos s0 = at(0, 6 * side);
                road(s0, way, 36);
                for (int k = 5; k < 36; k += 11) {
                    building(s0.relative(way, k).relative(f.getOpposite(), 2), f);
                    building(s0.relative(way, k).relative(f, 2), f.getOpposite());
                }
                BlockPos sq = s0.relative(way, 40);
                if (!wet(sq)) place("square", RIGID, sq.atY(land(sq)), SQUARE, f, false);
            }
            // a short street inland
            road(at(-6, 0), f.getOpposite(), 18);
            for (int k = 5; k < 18; k += 11) {
                building(at(-6 - k, 2), r.getOpposite());
                building(at(-6 - k, -2), r);
            }
        }
    }
}
