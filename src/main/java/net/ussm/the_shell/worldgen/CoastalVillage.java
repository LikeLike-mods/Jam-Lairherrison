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

public class CoastalVillage extends Structure {
    public static final MapCodec<CoastalVillage> CODEC = simpleCodec(CoastalVillage::new);
    private static final int SEA = TestChunkGenerator.SEA;
    private static final int DECK = SEA + 1;
    private static final StructureTemplatePool.Projection RIGID = StructureTemplatePool.Projection.RIGID;
    private static final StructureTemplatePool.Projection GROUND = StructureTemplatePool.Projection.TERRAIN_MATCHING;
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
        int chunkX = c.chunkPos().getMinBlockX();
        int chunkZ = c.chunkPos().getMinBlockZ();

        for (int dx = 2; dx <= 14; dx += 4) {
            for (int dz = 2; dz <= 14; dz += 4) {
                int x = chunkX + dx;
                int z = chunkZ + dz;
                int g = ground(c, x, z);
                if (g < SEA || g > SEA + 12) {
                    continue;
                }

                Direction sea = null;
                int shore = Integer.MAX_VALUE;
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    for (int k = 4; k <= 64; k += 2) {
                        int level = ground(
                                c,
                                x + d.getStepX() * k,
                                z + d.getStepZ() * k
                        );
                        if (level < SEA - 1) {
                            if (k < shore) {
                                shore = k;
                                sea = d;
                            }
                            break;
                        }
                    }
                }
                if (sea == null) {
                    continue;
                }

                boolean landBehind = false;
                for (int k = 3; k <= 12; k += 3) {
                    int level = ground(
                            c,
                            x - sea.getStepX() * k,
                            z - sea.getStepZ() * k
                    );
                    if (level >= SEA) {
                        landBehind = true;
                        break;
                    }
                }
                if (!landBehind) {
                    continue;
                }

                final Direction facing = sea;
                final int distance = shore;
                BlockPos origin = new BlockPos(x, g, z);
                return Optional.of(
                        new GenerationStub(
                                origin,
                                builder -> new Layout(
                                        c,
                                        builder,
                                        origin,
                                        facing
                                ).build(distance)
                        )
                );
            }
        }
        return Optional.empty();
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

        void road(BlockPos from, Direction dir, int length) {
            for (int k = 0; k < length; k += 9) {
                BlockPos p = from.relative(dir, k);
                if (wet(p.relative(dir, 4))) place("pier", RIGID, p.atY(DECK), PIER, dir.getOpposite(), true);
                else place("street", GROUND, p.atY(land(p)), STREET, dir.getOpposite(), true);
            }
        }

        static final String[] LAND = {"house", "house", "house", "fisher_hut", "fisher_hut", "market", "tavern", "smokehouse", "garden", "tower"};
        static final String[] WATER = {"house", "house", "fisher_hut", "fisher_hut", "fisher_hut", "tavern", "tower", "boathouse", "boathouse"};

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

        void building(BlockPos roadside, Direction towardRoad) {
            boolean water = wet(roadside);
            BlockPos door = roadside.atY(water ? DECK : land(roadside));
            String[] pool = water ? WATER : LAND;
            String name = pool[rand.nextInt(pool.length)];
            if (List.of("tower", "tavern", "market").contains(name) && used.merge(name, 1, Integer::sum) > 2) name = "house";   // max 2 each
            if (!place(name, RIGID, door, anchor(name), towardRoad, false) && !name.equals("fisher_hut"))
                place("fisher_hut", RIGID, door, anchor("fisher_hut"), towardRoad, false);
        }

        void build(int shore) {
            place("plaza", RIGID, o, PLAZA, f, true);
            int length = Math.max(30, Math.min(72, shore * 2 + 64));
            int start = -length / 2;

            road(at(0, start), r, length);
            for (int offset = 12; offset < length - 8; offset += 18) {
                int position = start + offset;
                BlockPos junction = at(0, position);
                int seaLength = 12 + rand.nextInt(3) * 9;
                road(junction, f, seaLength);
                int landLength = 3 + rand.nextInt(2) * 6;
                road(junction, f.getOpposite(), landLength);
                for (int k = 5; k < seaLength - 3; k += 11) {
                    BlockPos p = junction.relative(f, k);
                    building(p.relative(r, 2), r.getOpposite());
                    building(p.relative(r, -2), r);
                }
                building(at(4, position + 5), f.getOpposite());
                building(at(-4, position + 5), f);
            }

            for (int side : new int[]{1, -1}) {
                Direction branch = side > 0 ? r : r.getOpposite();
                int branchLength = 18 + rand.nextInt(3) * 9;
                BlockPos startBranch = at(0, side * (length / 2 - 9));
                road(startBranch, branch, branchLength);
                for (int k = 5; k < branchLength - 3; k += 11) {
                    BlockPos p = startBranch.relative(branch, k);
                    building(p.relative(f, 2), f.getOpposite());
                    building(p.relative(f.getOpposite(), 2), f);
                }
            }

            BlockPos seaEnd = at(0, length / 2 + 9);
            if (wet(seaEnd)) {
                place(
                        "water_plaza",
                        RIGID,
                        seaEnd.atY(DECK),
                        DECK_PLAZA,
                        f,
                        true
                );
                for (int side : new int[]{1, -1}) {
                    Direction branch = side > 0 ? r : r.getOpposite();
                    road(seaEnd, branch, 18);
                }
            }

            BlockPos landEnd = at(0, -length / 2 - 9);
            if (!wet(landEnd)) {
                place(
                        "square",
                        RIGID,
                        landEnd.atY(land(landEnd)),
                        SQUARE,
                        f,
                        false
                );
            }
        }
        void organicBranch(BlockPos start, Direction direction, int length) {
            road(start, direction, length);
            for (int k = 5; k < length - 3; k += 11) {
                BlockPos p = start.relative(direction, k);
                building(p.relative(direction.getClockWise(), 2),
                        direction.getClockWise().getOpposite());
                building(p.relative(direction.getCounterClockWise(), 2),
                        direction.getCounterClockWise().getOpposite());
            }
        }
    }
}
