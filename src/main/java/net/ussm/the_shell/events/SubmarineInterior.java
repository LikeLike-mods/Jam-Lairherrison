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
import net.ussm.the_shell.entites.Submarine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// l'interieur des sous-marins : une dimension vide ou chaque sous-marin a sa cabine, une tous les 64 blocs.
// clic droit sur le sous-marin pour entrer, la trappe dans le sol ressort a cote, l'echelle du fond monte aux commandes
@EventBusSubscriber(modid = TheShell.MOD_ID)
public class SubmarineInterior {
    public static final ResourceKey<Level> INTERIOR = ResourceKey.create(Registries.DIMENSION, id("submarine_interior"));
    public static final TagKey<Block> HULL = TagKey.create(Registries.BLOCK, id("submarine_hull"));
    private static final ResourceLocation CABIN = id("submarine/interior");
    private static final int SPACING = 64;
    private static final int BOTTOM = 64;
    // reperes dans la cabine depuis son coin : on arrive au milieu, sous 1.5 on est tombe par la trappe, au-dessus de 8.5 on a monte l'echelle
    private static final Vec3 ARRIVAL = new Vec3(4.5, 3.0, 9.5);
    private static final double HATCH = 1.5;
    private static final double ROOF = 8.5;
    private static final Map<UUID, Pending> PENDING = new HashMap<>();

    private record Pending(UUID submarine, long until) {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, path);
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
            inside.getStructureManager().get(CABIN).ifPresent(t -> t.placeInWorld(inside, corner, corner, new StructurePlaceSettings(), inside.random, Block.UPDATE_CLIENTS));
        }
        player.setPortalCooldown(20);
        player.changeDimension(new DimensionTransition(inside, Vec3.atLowerCornerOf(corner).add(ARRIVAL), Vec3.ZERO, 180.0F, 0.0F, DimensionTransition.DO_NOTHING));
    }

    public static void remember(Submarine submarine) {
        if (submarine.level() instanceof ServerLevel level) Cabins.get(level.getServer()).remember(submarine.getInteriorId(), submarine);
    }

    // on ressort a cote du sous-marin (trappe) ou dedans aux commandes (echelle)
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
        float yaw = where.getFloat("Yaw");
        Vec3 pos = new Vec3(where.getDouble("X"), where.getDouble("Y"), where.getDouble("Z"));
        if (!drive) pos = pos.add(Vec3.directionFromRotation(0.0F, yaw + 90.0F).scale(2.5));
        player.changeDimension(new DimensionTransition(level, pos.add(0.0, 0.3, 0.0), Vec3.ZERO, yaw, 0.0F, DimensionTransition.DO_NOTHING));
        if (drive) PENDING.put(player.getUUID(), new Pending(where.getUUID("Sub"), level.getGameTime() + 100));
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Pending pending = PENDING.get(player.getUUID());
        if (pending != null) {
            // le chunk du sous-marin vient de charger, on attend qu'il soit la pour monter dedans
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

    // on ne casse pas la coque, sinon on tombe dans le vide
    @SubscribeEvent
    public static void protect(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && level.dimension() == INTERIOR && !event.getPlayer().isCreative() && event.getState().is(HULL)) {
            event.setCanceled(true);
        }
    }

    // quel sous-marin va avec quelle cabine, et ou il etait la derniere fois
    static class Cabins extends SavedData {
        int next = 1;
        final Map<Integer, CompoundTag> subs = new HashMap<>();
        final Set<Integer> built = new HashSet<>();

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
            subs.put(cabin, tag);
            setDirty();
        }

        static Cabins load(CompoundTag tag, HolderLookup.Provider registries) {
            Cabins cabins = new Cabins();
            cabins.next = Math.max(1, tag.getInt("Next"));
            CompoundTag subs = tag.getCompound("Subs");
            for (String key : subs.getAllKeys()) cabins.subs.put(Integer.parseInt(key), subs.getCompound(key));
            for (int cabin : tag.getIntArray("Built")) cabins.built.add(cabin);
            return cabins;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putInt("Next", next);
            CompoundTag subsTag = new CompoundTag();
            subs.forEach((cabin, where) -> subsTag.put(String.valueOf(cabin), where));
            tag.put("Subs", subsTag);
            tag.putIntArray("Built", built.stream().mapToInt(Integer::intValue).toArray());
            return tag;
        }
    }
}
