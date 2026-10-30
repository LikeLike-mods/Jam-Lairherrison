package net.ussm.the_shell.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.blocks.station.SubmarineWindowBlock;
import net.ussm.the_shell.entites.Submarine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class SubmarineInterior {
    public static final ResourceKey<Level> INTERIOR = ResourceKey.create(Registries.DIMENSION, id("submarine_interior"));
    public static final TagKey<Block> HULL = TagKey.create(Registries.BLOCK, id("submarine_hull"));
    private static final ResourceLocation CABIN = id("submarine/interior");
    private static final int SPACING = 128;
    private static final int BOTTOM = 64;
    private static final Vec3 ARRIVAL = new Vec3(7.5, 6.0, 12.5);
    private static final double HATCH = 4.5;
    private static final double ROOF = 11.5;
    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final Map<UUID, UUID> TO_CABIN = new HashMap<>();
    private static final Map<UUID, UUID> SEAT_LATER = new HashMap<>();
    private static final BlockPos[] EXTENSIONS = {new BlockPos(11, 5, 4), new BlockPos(11, 5, 12), new BlockPos(-6, 5, 12)};

    public static void seatLater(ServerPlayer player, Submarine submarine) {
        SEAT_LATER.put(player.getUUID(), submarine.getUUID());
    }

    public static void upgrade(Submarine submarine) {
        if (!(submarine.level() instanceof ServerLevel level) || submarine.getInteriorId() < 0) return;
        ServerLevel inside = level.getServer().getLevel(INTERIOR);
        Cabins cabins = Cabins.get(level.getServer());
        if (inside == null || !cabins.built.contains(submarine.getInteriorId())) return;
        placeExtension(inside, submarine.getInteriorId(), submarine.getTier());
        cabins.setExtended(submarine.getInteriorId(), submarine.getTier());
    }

    private static void placeExtension(ServerLevel inside, int cabin, int tier) {
        BlockPos at = corner(cabin).offset(EXTENSIONS[tier - 1]);
        inside.getStructureManager().get(id("submarine/extension_" + tier))
                .ifPresent(t -> t.placeInWorld(inside, at, at, sec(), inside.random, Block.UPDATE_CLIENTS));
    }

    private static void fishes(ServerLevel inside, int cabin, CompoundTag where) {
        BlockPos c = corner(cabin);
        net.minecraft.world.phys.AABB zone = new net.minecraft.world.phys.AABB(c.getX() - 13, c.getY() - 10, c.getZ() - 13, c.getX() + 28, c.getY() + 25, c.getZ() + 34);
        net.minecraft.world.phys.AABB tout = new net.minecraft.world.phys.AABB(c.getX() - 48, c.getY() - 20, c.getZ() - 44, c.getX() + 56, c.getY() + 34, c.getZ() + 52);
        java.util.List<net.minecraft.world.entity.Mob> poissons = inside.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, tout,
                m -> !m.hasCustomName() && (m instanceof net.minecraft.world.entity.animal.WaterAnimal || m.getType() == net.ussm.the_shell.init.ModEntities.ABYSSAL_ANGLER.get()));
        int restants = 0;
        for (net.minecraft.world.entity.Mob m : poissons) {
            if (zone.contains(m.position()) && restants < 7) restants++;
            else m.discard();
        }
        for (net.minecraft.world.entity.item.ItemEntity item : inside.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, tout)) {
            BlockPos ip = item.blockPosition().subtract(c);
            boolean dedans = false;
            for (int[] b : DANS_LA_CABINE) dedans |= ip.getX() >= b[0] && ip.getX() <= b[3] && ip.getY() >= b[1] && ip.getY() <= b[4] && ip.getZ() >= b[2] && ip.getZ() <= b[5];
            if (!dedans) item.discard();
        }
        if (restants >= 7) return;
        String dim = where.getString("Dim");
        boolean abysses = dim.equals(AbyssTravel.ABYSS.location().toString());
        boolean clair = dim.equals(AtollTravel.ATOLL.location().toString()) && where.getDouble("Y") > net.ussm.the_shell.worldgen.TestChunkGenerator.SEA - 24;
        net.minecraft.world.entity.EntityType<?>[] types = abysses
                ? new net.minecraft.world.entity.EntityType<?>[]{net.ussm.the_shell.init.ModEntities.HATCHETFISH.get(), net.ussm.the_shell.init.ModEntities.HATCHETFISH.get(),
                        net.minecraft.world.entity.EntityType.GLOW_SQUID, net.ussm.the_shell.init.ModEntities.ABYSSAL_ANGLER.get()}
                : clair ? new net.minecraft.world.entity.EntityType<?>[]{net.minecraft.world.entity.EntityType.TROPICAL_FISH, net.minecraft.world.entity.EntityType.TROPICAL_FISH,
                        net.minecraft.world.entity.EntityType.COD, net.minecraft.world.entity.EntityType.SALMON, net.minecraft.world.entity.EntityType.PUFFERFISH}
                : new net.minecraft.world.entity.EntityType<?>[]{net.minecraft.world.entity.EntityType.COD, net.minecraft.world.entity.EntityType.SQUID, net.minecraft.world.entity.EntityType.GLOW_SQUID};
        net.minecraft.util.RandomSource r = inside.random;
        for (int essai = 0; essai < 12; essai++) {
            BlockPos p = c.offset(-4 + r.nextInt(23), r.nextInt(17), -4 + r.nextInt(29));
            if (!inside.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER) || !inside.getFluidState(p.above()).is(net.minecraft.tags.FluidTags.WATER)) continue;
            if (!(types[r.nextInt(types.length)].create(inside) instanceof net.minecraft.world.entity.Mob mob)) return;
            mob.moveTo(p.getX() + 0.5, p.getY() + 0.2, p.getZ() + 0.5, r.nextFloat() * 360.0F, 0.0F);
            net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(mob, inside, inside.getCurrentDifficultyAt(p), net.minecraft.world.entity.MobSpawnType.NATURAL, null);
            inside.addFreshEntity(mob);
            return;
        }
    }

    private static final int[][] DANS_LA_CABINE = {{3, 3, 3, 11, 14, 19}, {11, 5, 4, 19, 10, 10}, {11, 5, 12, 19, 10, 18}, {-6, 5, 12, 3, 10, 20}};

    private static void mirror(ServerLevel inside, int cabin, CompoundTag where, int tier, int passe) {
        ServerLevel world = inside.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(where.getString("Dim"))));
        if (world == null) return;
        BlockPos sub = BlockPos.containing(where.getDouble("X"), where.getDouble("Y"), where.getDouble("Z"));
        world.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.PORTAL, new net.minecraft.world.level.ChunkPos(sub), 3, sub);
        net.minecraft.world.level.block.Rotation versCabine = switch (net.minecraft.core.Direction.fromYRot(where.getFloat("Yaw"))) {
            case EAST -> net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90;
            case SOUTH -> net.minecraft.world.level.block.Rotation.CLOCKWISE_180;
            case WEST -> net.minecraft.world.level.block.Rotation.CLOCKWISE_90;
            default -> net.minecraft.world.level.block.Rotation.NONE;
        };
        net.minecraft.world.level.block.Rotation versMonde = switch (versCabine) {
            case COUNTERCLOCKWISE_90 -> net.minecraft.world.level.block.Rotation.CLOCKWISE_90;
            case CLOCKWISE_90 -> net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90;
            default -> versCabine;
        };
        BlockPos c = corner(cabin);
        net.minecraft.world.level.block.state.BlockState vide = world.getFluidState(sub).is(net.minecraft.tags.FluidTags.WATER)
                ? net.minecraft.world.level.block.Blocks.WATER.defaultBlockState() : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        for (int lx = -40; lx <= 40; lx++) {
            if (Math.floorMod(lx, 8) != passe) continue;
            for (int ly = -16; ly <= 24; ly++) {
                for (int lz = -36; lz <= 37; lz++) {
                    int tx = 7 + lx, ty = 3 + ly, tz = 11 + lz;
                    int proche = 99;
                    for (int n = 0; n <= tier && n < DANS_LA_CABINE.length; n++) {
                        int[] b = DANS_LA_CABINE[n];
                        int d = Math.max(Math.max(Math.max(b[0] - tx, tx - b[3]), Math.max(b[1] - ty, ty - b[4])), Math.max(b[2] - tz, tz - b[5]));
                        proche = Math.min(proche, d);
                    }
                    if (proche <= 0) continue;
                    boolean bord = Math.abs(lx) == 40 || ly == -16 || ly == 24 || lz == -36 || lz == 37;
                    net.minecraft.world.level.block.state.BlockState st = bord ? net.minecraft.world.level.block.Blocks.BARRIER.defaultBlockState()
                            : world.getBlockState(sub.offset(new BlockPos(lx, ly, lz).rotate(versMonde))).rotate(versCabine);
                    if (!bord && proche <= 3 && !st.isAir() && st.getFluidState().isEmpty()) st = vide;
                    BlockPos target = c.offset(tx, ty, tz);
                    if (inside.getBlockState(target) != st) inside.setBlock(target, st, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
                }
            }
        }
    }

    private static void updateWindows(ServerLevel inside, int cabin, CompoundTag where) {
        String dim = where.getString("Dim");
        int zone = dim.equals(AbyssTravel.ABYSS.location().toString()) ? 2
                : dim.equals(AtollTravel.ATOLL.location().toString()) && where.getDouble("Y") > net.ussm.the_shell.worldgen.TestChunkGenerator.SEA - 24 ? 0 : 1;
        BlockPos c = corner(cabin);
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-8, 0, 0), c.offset(22, 17, 24))) {
            net.minecraft.world.level.block.state.BlockState st = inside.getBlockState(p);
            if (st.is(net.ussm.the_shell.init.ModBlocks.SUBMARINE_WINDOW.get()) && st.getValue(SubmarineWindowBlock.ZONE) != zone) {
                inside.setBlock(p, st.setValue(SubmarineWindowBlock.ZONE, zone), Block.UPDATE_CLIENTS);
            }
        }
    }

    private record Pending(UUID submarine, long until) {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, path);
    }

    private static StructurePlaceSettings sec() {
        return new StructurePlaceSettings().setLiquidSettings(net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings.IGNORE_WATERLOGGING);
    }

    private static void secher(ServerLevel inside, int cabin, int tier) {
        BlockPos c = corner(cabin);
        for (int n = 0; n <= tier && n < DANS_LA_CABINE.length; n++) {
            int[] b = DANS_LA_CABINE[n];
            for (BlockPos p : BlockPos.betweenClosed(c.offset(b[0], b[1], b[2]), c.offset(b[3], b[4], b[5]))) {
                net.minecraft.world.level.block.state.BlockState st = inside.getBlockState(p);
                if (st.is(net.minecraft.world.level.block.Blocks.WATER)) {
                    inside.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                } else if (st.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)
                        && st.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                    inside.setBlock(p, st.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, false), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private static BlockPos corner(int cabin) {
        return new BlockPos(cabin * SPACING, BOTTOM, 0);
    }

    public static void enter(ServerPlayer player, Submarine submarine) {
        ServerLevel inside = player.server.getLevel(INTERIOR);
        if (inside == null) return;
        Cabins cabins = Cabins.get(player.server);
        if (submarine.getInteriorId() < 0) submarine.setInteriorId(cabins.next++);
        int cabin = submarine.getInteriorId();
        cabins.remember(cabin, submarine);
        BlockPos corner = corner(cabin);
        if (cabins.built.add(cabin)) {
            cabins.extendedTo.remove(cabin);
            cabins.setDirty();
            inside.getEntitiesOfClass(net.minecraft.world.entity.animal.WaterAnimal.class, new net.minecraft.world.phys.AABB(corner.offset(-8, -2, -2).getCenter(), corner.offset(24, 20, 26).getCenter()))
                    .forEach(net.minecraft.world.entity.Entity::discard);
            inside.getStructureManager().get(CABIN).ifPresent(t -> t.placeInWorld(inside, corner, corner, sec(), inside.random, Block.UPDATE_CLIENTS));
        }
        for (int tier = 1; tier <= submarine.getTier(); tier++) {
            if (cabins.extended(cabin) < tier) {
                placeExtension(inside, cabin, tier);
            }
        }
        cabins.setExtended(cabin, submarine.getTier());
        secher(inside, cabin, submarine.getTier());
        for (BlockPos h : new BlockPos[]{new BlockPos(11, 7, 7), new BlockPos(11, 7, 15), new BlockPos(3, 7, 15)}) {
            if (inside.getBlockState(corner.offset(h)).is(net.ussm.the_shell.init.ModBlocks.REINFORCED_GLASS.get())) {
                inside.setBlock(corner.offset(h), net.ussm.the_shell.init.ModBlocks.METAL_PLATING.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        for (int passe = 0; passe < 8; passe++) mirror(inside, cabin, cabins.subs.get(cabin), cabins.extended(cabin), passe);
        player.setPortalCooldown(20);
        player.changeDimension(new DimensionTransition(inside, Vec3.atLowerCornerOf(corner).add(ARRIVAL), Vec3.ZERO, 180.0F, 0.0F, DimensionTransition.DO_NOTHING));
    }

    public static int takeFuel(Submarine submarine) {
        if (!(submarine.level() instanceof ServerLevel level)) return 0;
        CompoundTag where = Cabins.get(level.getServer()).subs.get(submarine.getInteriorId());
        if (where == null || !where.contains("Fuel")) return 0;
        int fuel = where.getInt("Fuel");
        where.remove("Fuel");
        return fuel;
    }

    @SubscribeEvent
    public static void useInside(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().dimension() != INTERIOR) return;
        net.minecraft.world.level.block.state.BlockState state = player.level().getBlockState(event.getPos());
        int cabin = Math.floorDiv((int) Math.floor(player.getX()) + SPACING / 2, SPACING);
        Cabins cabins = Cabins.get(player.server);
        CompoundTag where = cabins.subs.get(cabin);
        if (where == null) return;
        int energy = where.getInt("Energy") + where.getInt("Fuel");
        if (state.is(net.ussm.the_shell.init.ModBlocks.MACHINE.get())) {
            int fuel = Submarine.fuelValue(event.getItemStack());
            if (fuel <= 0) return;
            where.putInt("Fuel", where.getInt("Fuel") + fuel);
            cabins.setDirty();
            event.getItemStack().consume(1, player);
            player.level().playSound(null, event.getPos(), net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT, net.minecraft.sounds.SoundSource.BLOCKS, 0.6F, 1.4F);
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.the_shell.submarine.energy", Math.min(100, (energy + fuel) * 100 / Submarine.MAX_ENERGY)), true);
            event.setCanceled(true);
        } else if (state.is(net.ussm.the_shell.init.ModBlocks.CONTROL_CONSOLE.get())) {
            int sx = (int) Math.floor(where.getDouble("X")), sy = (int) Math.floor(where.getDouble("Y")), sz = (int) Math.floor(where.getDouble("Z"));
            int batterie = Math.min(100, energy * 100 / Submarine.MAX_ENERGY);
            int[] faille = nearestRift(sx, sz);
            player.displayClientMessage(faille == null
                    ? net.minecraft.network.chat.Component.translatable("message.the_shell.submarine.console_far", batterie, sy)
                    : net.minecraft.network.chat.Component.translatable("message.the_shell.submarine.console", batterie, sy, faille[0], faille[1],
                            (int) Math.sqrt((faille[0] - sx) * (faille[0] - sx) + (faille[1] - sz) * (faille[1] - sz))), false);
            event.setCanceled(true);
        }
    }

    public static int[] nearestRift(int x, int z) {
        for (int r = 0; r <= 600; r += 8) {
            for (int dx = -r; dx <= r; dx += 8) {
                for (int dz = -r; dz <= r; dz += 8) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    if (net.ussm.the_shell.worldgen.TestChunkGenerator.isRift(x + dx, z + dz)) return new int[]{x + dx, z + dz};
                }
            }
        }
        return null;
    }

    public static void store(Submarine submarine) {
        if (!(submarine.level() instanceof ServerLevel level) || submarine.getInteriorId() < 0) return;
        Cabins cabins = Cabins.get(level.getServer());
        cabins.remember(submarine.getInteriorId(), submarine);
        cabins.subs.get(submarine.getInteriorId()).putBoolean("Stored", true);
        cabins.setDirty();
    }

    public static boolean claim(ServerLevel level, int cabin) {
        Cabins cabins = Cabins.get(level.getServer());
        CompoundTag where = cabins.subs.get(cabin);
        if (where == null || !where.getBoolean("Stored")) return false;
        where.remove("Stored");
        where.remove("Gone");
        cabins.setDirty();
        return true;
    }

    public static void lost(Submarine submarine) {
        if (!(submarine.level() instanceof ServerLevel level) || submarine.getInteriorId() < 0) return;
        Cabins cabins = Cabins.get(level.getServer());
        CompoundTag where = cabins.subs.get(submarine.getInteriorId());
        if (where == null) return;
        where.putBoolean("Gone", true);
        cabins.setDirty();
    }

    @SubscribeEvent
    public static void respawnSansSousMarin(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.getRespawnDimension() != INTERIOR || player.getRespawnPosition() == null) return;
        int cabin = Math.floorDiv(player.getRespawnPosition().getX() + SPACING / 2, SPACING);
        CompoundTag where = Cabins.get(player.server).subs.get(cabin);
        if (where != null && !where.getBoolean("Stored") && !where.getBoolean("Gone")) return;
        player.setRespawnPosition(Level.OVERWORLD, null, 0.0F, false, false);
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.the_shell.submarine.respawn_lost"));
    }

    public static void remember(Submarine submarine) {
        if (submarine.level() instanceof ServerLevel level) Cabins.get(level.getServer()).remember(submarine.getInteriorId(), submarine);
    }

    private static void leave(ServerPlayer player, boolean drive) {
        int cabin = Math.floorDiv((int) Math.floor(player.getX()) + SPACING / 2, SPACING);
        CompoundTag where = Cabins.get(player.server).subs.get(cabin);
        ServerLevel level = where == null ? null : player.server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(where.getString("Dim"))));
        player.setPortalCooldown(20);
        if (level == null) {
            ServerLevel spawn = player.server.overworld();
            player.changeDimension(new DimensionTransition(spawn, Vec3.atBottomCenterOf(spawn.getSharedSpawnPos()), Vec3.ZERO, player.getYRot(), 0.0F, DimensionTransition.DO_NOTHING));
            return;
        }
        if (drive && level.getEntity(where.getUUID("Sub")) instanceof Submarine sub && sub.isVehicle()) {
            player.teleportTo(player.getX(), BOTTOM + 6.0, player.getZ());
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.the_shell.submarine.occupied"), true);
            return;
        }
        float yaw = where.getFloat("Yaw");
        Vec3 pos = new Vec3(where.getDouble("X"), where.getDouble("Y"), where.getDouble("Z"));
        if (!drive) pos = pos.add(Vec3.directionFromRotation(0.0F, yaw + 90.0F).scale(2.5));
        player.changeDimension(new DimensionTransition(level, pos.add(0.0, 0.3, 0.0), Vec3.ZERO, yaw, 0.0F, DimensionTransition.DO_NOTHING));
        if (drive) PENDING.put(player.getUUID(), new Pending(where.getUUID("Sub"), level.getGameTime() + 100));
    }

    public static void downFromSeat(ServerPlayer player) {
        if (player.isOnPortalCooldown() || !(player.getVehicle() instanceof Submarine sub) || !sub.isAlive()) return;
        player.stopRiding();
        TO_CABIN.put(player.getUUID(), sub.getUUID());
        if (!player.getData(net.ussm.the_shell.init.ModAttachments.CABIN_BUTTON_USED)) {
            player.setData(net.ussm.the_shell.init.ModAttachments.CABIN_BUTTON_USED, true);
            player.syncData(net.ussm.the_shell.init.ModAttachments.CABIN_BUTTON_USED);
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID seat = SEAT_LATER.get(player.getUUID());
        if (seat != null && !player.isShiftKeyDown()) {
            SEAT_LATER.remove(player.getUUID());
            if (player.serverLevel().getEntity(seat) instanceof Submarine sub && !sub.isVehicle() && player.distanceToSqr(sub) < 36) player.startRiding(sub);
        }
        if (player.level().dimension() == INTERIOR && player.tickCount % 10 == 0) {
            int c = Math.floorDiv((int) Math.floor(player.getX()) + SPACING / 2, SPACING);
            CompoundTag w = Cabins.get(player.server).subs.get(c);
            if (w != null) {
                mirror(player.serverLevel(), c, w, Cabins.get(player.server).extended(c), (player.tickCount / 10) % 8);
                if (player.tickCount % 40 == 0) fishes(player.serverLevel(), c, w);
            }
        }
        UUID back = TO_CABIN.remove(player.getUUID());
        if (back != null && !player.isPassenger() && player.serverLevel().getEntity(back) instanceof Submarine sub) enter(player, sub);
        Pending pending = PENDING.get(player.getUUID());
        if (pending != null) {
            if (player.serverLevel().getEntity(pending.submarine()) instanceof Submarine submarine && !submarine.isVehicle()) {
                player.startRiding(submarine, true);
                PENDING.remove(player.getUUID());
            } else if (player.serverLevel().getGameTime() > pending.until()) {
                PENDING.remove(player.getUUID());
            }
        }
        if (player.level().dimension() != INTERIOR || player.isOnPortalCooldown() || player.isSpectator()) return;
        double y = player.getY() - BOTTOM;
        if (y < HATCH) leave(player, false);
        else if (y > ROOF) leave(player, true);
    }

    @SubscribeEvent
    public static void refuelFromSeat(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity().getVehicle() instanceof Submarine sub)) return;
        int fuel = Submarine.fuelValue(event.getItemStack());
        if (fuel <= 0) return;
        if (!event.getLevel().isClientSide && sub.getEnergy() < Submarine.MAX_ENERGY) {
            sub.setEnergy(sub.getEnergy() + fuel);
            event.getItemStack().consume(1, event.getEntity());
            event.getLevel().playSound(null, sub.blockPosition(), net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT, net.minecraft.sounds.SoundSource.NEUTRAL, 0.6F, 1.4F);
            event.getEntity().displayClientMessage(net.minecraft.network.chat.Component.translatable("message.the_shell.submarine.energy", sub.getEnergy() * 100 / Submarine.MAX_ENERGY), true);
        }
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void noBreakFromSeat(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().getVehicle() instanceof Submarine) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void noUseFromSeat(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().getVehicle() instanceof Submarine) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void noHitFromSeat(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        if (event.getEntity().getVehicle() instanceof Submarine) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void noBreakFromSeat(BlockEvent.BreakEvent event) {
        if (event.getPlayer().getVehicle() instanceof Submarine) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void protect(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && level.dimension() == INTERIOR && !event.getPlayer().isCreative() && event.getState().is(HULL)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void noExplosion(net.neoforged.neoforge.event.level.ExplosionEvent.Detonate event) {
        if (event.getLevel().dimension() == INTERIOR) event.getAffectedBlocks().clear();
    }

    static class Cabins extends SavedData {
        int next = 1;
        final Map<Integer, CompoundTag> subs = new HashMap<>();
        final Set<Integer> built = new HashSet<>();
        final Map<Integer, Integer> extendedTo = new HashMap<>();

        int extended(int cabin) {
            return built.contains(cabin) ? extendedTo.getOrDefault(cabin, 0) : 0;
        }

        void setExtended(int cabin, int tier) {
            extendedTo.put(cabin, tier);
            setDirty();
        }

        static Cabins get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(Cabins::new, Cabins::load, null), "the_shell_submarines");
        }

        void remember(int cabin, Submarine submarine) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Sub", submarine.getUUID());
            tag.putString("Dim", submarine.level().dimension().location().toString());
            tag.putDouble("X", submarine.getX());
            tag.putDouble("Y", submarine.getY());
            tag.putDouble("Z", submarine.getZ());
            tag.putFloat("Yaw", submarine.getYRot());
            tag.putInt("Energy", submarine.getEnergy());
            CompoundTag old = subs.get(cabin);
            if (old != null && old.contains("Fuel")) tag.putInt("Fuel", old.getInt("Fuel"));
            subs.put(cabin, tag);
            setDirty();
        }

        static Cabins load(CompoundTag tag, HolderLookup.Provider registries) {
            Cabins cabins = new Cabins();
            cabins.next = Math.max(1, tag.getInt("Next"));
            CompoundTag subs = tag.getCompound("Subs");
            for (String key : subs.getAllKeys()) cabins.subs.put(Integer.parseInt(key), subs.getCompound(key));
            for (int cabin : tag.getIntArray("Built5")) cabins.built.add(cabin);
            CompoundTag ext = tag.getCompound("Extended");
            for (String key : ext.getAllKeys()) cabins.extendedTo.put(Integer.parseInt(key), ext.getInt(key));
            return cabins;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putInt("Next", next);
            CompoundTag subsTag = new CompoundTag();
            subs.forEach((cabin, where) -> subsTag.put(String.valueOf(cabin), where));
            tag.put("Subs", subsTag);
            tag.putIntArray("Built5", built.stream().mapToInt(Integer::intValue).toArray());
            CompoundTag ext = new CompoundTag();
            extendedTo.forEach((cabin, tier) -> ext.putInt(String.valueOf(cabin), tier));
            tag.put("Extended", ext);
            return tag;
        }
    }
}
