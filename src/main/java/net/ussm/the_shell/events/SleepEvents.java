package net.ussm.the_shell.events;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.ussm.the_shell.TheShell;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class SleepEvents {
    public static final Set<ResourceKey<Level>> SAME_WORLD = Set.of(Level.OVERWORLD, AtollTravel.ATOLL, AbyssTravel.ABYSS, SubmarineInterior.INTERIOR);

    @SubscribeEvent
    public static void canSleep(CanPlayerSleepEvent event) {
        if (event.getProblem() != null || !SAME_WORLD.contains(event.getLevel().dimension())) return;
        if (event.getEntity().server.overworld().isDay()) event.setProblem(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        List<ServerPlayer> players = server.getPlayerList().getPlayers().stream()
                .filter(p -> !p.isSpectator() && SAME_WORLD.contains(p.level().dimension())).toList();
        long sleeping = players.stream().filter(LivingEntity::isSleeping).count();
        if (sleeping == 0) return;
        ServerLevel world = server.overworld();
        int needed = Math.max(1, Mth.ceil(players.size() * world.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE) / 100.0F));
        if (players.stream().filter(Player::isSleepingLongEnough).count() < needed) {
            if (players.size() > 1 && server.getTickCount() % 20 == 0) {
                Component text = Component.translatable("sleep.players_sleeping", sleeping, needed);
                players.forEach(p -> p.displayClientMessage(text, true));
            }
            return;
        }
        if (world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            long j = world.getDayTime() + 24000L;
            world.setDayTime(EventHooks.onSleepFinished(world, j - j % 24000L, world.getDayTime()));
        }
        players.stream().filter(LivingEntity::isSleeping).forEach(p -> p.stopSleepInBed(false, false));
        if (world.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && world.isRaining()) world.setWeatherParameters(0, 0, false, false);
    }
}
