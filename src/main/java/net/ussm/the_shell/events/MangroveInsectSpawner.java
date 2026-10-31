package net.ussm.the_shell.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.entites.Dragonfly;
import net.ussm.the_shell.entites.Firefly;
import net.ussm.the_shell.init.ModEntities;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class MangroveInsectSpawner {
    private static final ResourceKey<Biome> MANGROVE =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "tropical_mangrove"));
    private static final double RADIUS = 40.0;

    @SubscribeEvent
    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % 20 != 0) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;
        boolean night = level.isNight();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            AABB area = player.getBoundingBox().inflate(RADIUS, 24.0, RADIUS);
            int dragonflies = level.getEntitiesOfClass(Dragonfly.class, area).size();
            int fireflies = level.getEntitiesOfClass(Firefly.class, area).size();
            int dragonflyMissing = (night ? 4 : 14) - dragonflies;
            int fireflyMissing = (night ? 24 : 8) - fireflies;
            if (dragonflyMissing <= 0 && fireflyMissing <= 0) continue;
            boolean pickFirefly = fireflyMissing > 0
                    && (dragonflyMissing <= 0 || level.random.nextInt(dragonflyMissing + fireflyMissing) < fireflyMissing);
            if (pickFirefly) trySpawn(level, player, ModEntities.FIREFLY.get(), 3, 6, 0.8F, 3.2F);
            else trySpawn(level, player, ModEntities.DRAGONFLY.get(), 2, 4, 0.6F, 2.6F);
        }
    }

    private static void trySpawn(ServerLevel level, ServerPlayer player, EntityType<? extends Mob> type,
                                 int min, int max, float minHeight, float maxHeight) {
        RandomSource r = level.random;
        for (int attempt = 0; attempt < 4; attempt++) {
            float angle = r.nextFloat() * Mth.TWO_PI;
            double distance = 10.0 + r.nextDouble() * 20.0;
            int x = Mth.floor(player.getX() + Mth.cos(angle) * distance);
            int z = Mth.floor(player.getZ() + Mth.sin(angle) * distance);
            BlockPos column = new BlockPos(x, 0, z);
            if (!level.hasChunkAt(column)) continue;
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos center = new BlockPos(x, ground, z);
            if (!level.getBiome(center).is(MANGROVE) || Math.abs(ground - player.getY()) > 24) continue;
            int count = min + r.nextInt(max - min + 1);
            int spawned = 0;
            for (int i = 0; i < count; i++) {
                double px = x + 0.5 + (i == 0 ? 0 : r.nextInt(7) - 3);
                double pz = z + 0.5 + (i == 0 ? 0 : r.nextInt(7) - 3);
                int floor = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(px), Mth.floor(pz));
                double py = floor + minHeight + r.nextFloat() * (maxHeight - minHeight);
                BlockPos pos = BlockPos.containing(px, py, pz);
                if (!level.getBlockState(pos).isAir() || !level.getBiome(pos).is(MANGROVE)) continue;
                Mob mob = type.create(level);
                if (mob == null) return;
                mob.moveTo(px, py, pz, r.nextFloat() * 360.0F, 0.0F);
                if (!level.noCollision(mob)) {
                    mob.discard();
                    continue;
                }
                EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
                level.addFreshEntityWithPassengers(mob);
                spawned++;
            }
            if (spawned > 0) return;
        }
    }
}
