package net.ussm.the_shell.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.ussm.the_shell.TheShell;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class AbyssSpawner {
    private static final int MAX_NEAR = 12;
    private static final MobCategory[] CATEGORIES = {MobCategory.UNDERGROUND_WATER_CREATURE, MobCategory.WATER_AMBIENT,
            MobCategory.WATER_AMBIENT, MobCategory.AXOLOTLS};

    @SubscribeEvent
    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != AbyssTravel.ABYSS) return;
        if (level.getGameTime() % 40 != 0 || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            if (level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(48)).size() >= MAX_NEAR) continue;
            trySpawn(level, player);
        }
    }

    private static void trySpawn(ServerLevel level, ServerPlayer player) {
        RandomSource r = level.random;
        BlockPos pos = player.blockPosition().offset(r.nextInt(61) - 30, r.nextInt(21) - 10, r.nextInt(61) - 30);
        if (pos.distSqr(player.blockPosition()) < 18 * 18 || !level.hasChunkAt(pos) || !level.getFluidState(pos).is(FluidTags.WATER)) return;
        MobCategory category = CATEGORIES[r.nextInt(CATEGORIES.length)];
        MobSpawnSettings.SpawnerData data = level.getBiome(pos).value().getMobSettings().getMobs(category).getRandom(r).orElse(null);
        if (data == null) return;
        // ceux qui marchent (crabes, isopodes) on les pose au fond
        BlockPos at = category == MobCategory.AXOLOTLS ? floorBelow(level, pos) : pos;
        if (at == null) return;
        int count = data.minCount + r.nextInt(data.maxCount - data.minCount + 1);
        for (int i = 0; i < count; i++) {
            BlockPos p = i == 0 ? at : at.offset(r.nextInt(5) - 2, 0, r.nextInt(5) - 2);
            if (!level.getFluidState(p).is(FluidTags.WATER) || !SpawnPlacements.checkSpawnRules(data.type, level, MobSpawnType.NATURAL, p, r)) continue;
            if (!(data.type.create(level) instanceof Mob mob)) return;
            mob.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, r.nextFloat() * 360F, 0F);
            if (!mob.checkSpawnObstruction(level) || !level.noCollision(mob)) continue;
            EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(p), MobSpawnType.NATURAL, null);
            level.addFreshEntityWithPassengers(mob);
        }
    }

    private static BlockPos floorBelow(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos p = pos.mutable();
        for (int i = 0; i < 32; i++) {
            BlockPos below = p.below();
            if (!level.getFluidState(below).is(FluidTags.WATER)) {
                return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) ? p.immutable() : null;
            }
            p.move(Direction.DOWN);
        }
        return null;
    }
}
