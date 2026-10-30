package net.ussm.the_shell.events;

import java.util.List;
import java.util.Set;
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
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.ussm.the_shell.TheShell;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class SleepEvents {
    public static final Set<ResourceKey<Level>> MEME_MONDE = Set.of(Level.OVERWORLD, AtollTravel.ATOLL, AbyssTravel.ABYSS, SubmarineInterior.INTERIOR);

    @SubscribeEvent
    public static void canSleep(CanPlayerSleepEvent event) {
        if (event.getProblem() != null || !MEME_MONDE.contains(event.getLevel().dimension())) return;
        if (event.getEntity().server.overworld().isDay()) event.setProblem(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        List<ServerPlayer> joueurs = server.getPlayerList().getPlayers().stream()
                .filter(p -> !p.isSpectator() && MEME_MONDE.contains(p.level().dimension())).toList();
        long dorment = joueurs.stream().filter(LivingEntity::isSleeping).count();
        if (dorment == 0) return;
        ServerLevel monde = server.overworld();
        int besoin = Math.max(1, Mth.ceil(joueurs.size() * monde.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE) / 100.0F));
        if (joueurs.stream().filter(Player::isSleepingLongEnough).count() < besoin) {
            if (joueurs.size() > 1 && server.getTickCount() % 20 == 0) {
                Component texte = Component.translatable("sleep.players_sleeping", dorment, besoin);
                joueurs.forEach(p -> p.displayClientMessage(texte, true));
            }
            return;
        }
        if (monde.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            long j = monde.getDayTime() + 24000L;
            monde.setDayTime(net.neoforged.neoforge.event.EventHooks.onSleepFinished(monde, j - j % 24000L, monde.getDayTime()));
        }
        joueurs.stream().filter(LivingEntity::isSleeping).forEach(p -> p.stopSleepInBed(false, false));
        if (monde.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && monde.isRaining()) monde.setWeatherParameters(0, 0, false, false);
    }
}
