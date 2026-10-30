package net.ussm.the_shell.events;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.blocks.NotchAltarBlock;
import net.ussm.the_shell.blocks.TempleSealBlock;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class TempleQuiz {
    private static final String[][][] ANSWERS = {
            {
                    {"creeper"}, {"10", "dix", "ten"}, {"pioche", "pickaxe"}, {"lapis", "lapis lazuli"},
                    {"mouton", "sheep"}, {"nether"}, {"lit", "bed"}, {"9", "neuf", "nine"}},
            {
                    {"15", "quinze", "fifteen"}, {"enderman"}, {"4", "quatre", "four"}, {"os", "bone"},
                    {"ancre de reapparition", "ancre", "respawn anchor"}, {"noye", "drowned"}, {"20", "vingt", "twenty"},
                    {"potion de faiblesse", "faiblesse", "weakness", "potion of weakness"}},
            {
                    {"20", "vingt", "twenty"}, {"319"}, {"2011"}, {"5", "cinq", "five", "v"},
                    {"4", "quatre", "four"}, {"markus", "markus persson"}, {"warden", "gardien"}, {"breeze", "brise"}},
    };
    private static final Map<UUID, Question> IN_PROGRESS = new HashMap<>();

    private record Question(BlockPos altar, int altarTier, int number, long end) {
    }

    private static Component notch(Component text) {
        return Component.literal("<Notch> ").append(text);
    }

    public static void askQuestion(ServerPlayer player, BlockPos altar, int altarTier) {
        int number = player.getRandom().nextInt(ANSWERS[altarTier - 1].length);
        IN_PROGRESS.put(player.getUUID(), new Question(altar.immutable(), altarTier, number, player.serverLevel().getGameTime() + 20 * 120));
        player.sendSystemMessage(notch(Component.translatable("message.the_shell.notch.intro." + altarTier)));
        player.sendSystemMessage(notch(Component.translatable("quiz.the_shell." + altarTier + "." + number).withStyle(ChatFormatting.YELLOW)));
    }

    private static String normalise(String s) {
        s = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\b(le|la|les|l|un|une|des|du|de|the|a|an)\\b", " ");
        return s.trim().replaceAll(" +", " ");
    }

    @SubscribeEvent
    public static void answer(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        Question q = IN_PROGRESS.get(player.getUUID());
        if (q == null) return;
        ServerLevel level = player.serverLevel();
        if (level.getGameTime() > q.end() || player.blockPosition().distSqr(q.altar()) > 32 * 32) {
            IN_PROGRESS.remove(player.getUUID());
            return;
        }
        event.setCanceled(true);
        IN_PROGRESS.remove(player.getUUID());
        player.sendSystemMessage(Component.literal("<" + player.getGameProfile().getName() + "> " + event.getRawText()).withStyle(ChatFormatting.GRAY));
        String said = normalise(event.getRawText());
        boolean answeredRight = false;
        for (String correct : ANSWERS[q.altarTier() - 1][q.number()]) answeredRight |= normalise(correct).equals(said);
        if (answeredRight) {
            player.sendSystemMessage(notch(Component.translatable("message.the_shell.notch.correct." + q.altarTier()).withStyle(ChatFormatting.GREEN)));
            level.playSound(null, q.altar(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.0F);
            NotchAltarBlock.markSolved(level, q.altar());
            TempleSealBlock.openTier(level, q.altar(), q.altarTier());
        } else {
            player.sendSystemMessage(notch(Component.translatable("message.the_shell.notch.wrong").withStyle(ChatFormatting.RED)));
            level.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 0.6F);
            if (q.altarTier() >= 2) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            if (q.altarTier() >= 3) player.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
            askQuestion(player, q.altar(), q.altarTier());
        }
    }
}
