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
    private static final String[][][] REPONSES = {
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
    private static final Map<UUID, Question> EN_COURS = new HashMap<>();

    private record Question(BlockPos autel, int niveau, int numero, long fin) {
    }

    private static Component notch(Component texte) {
        return Component.literal("<Notch> ").append(texte);
    }

    public static void poser(ServerPlayer player, BlockPos autel, int niveau) {
        int numero = player.getRandom().nextInt(REPONSES[niveau - 1].length);
        EN_COURS.put(player.getUUID(), new Question(autel.immutable(), niveau, numero, player.serverLevel().getGameTime() + 20 * 120));
        player.sendSystemMessage(notch(Component.translatable("message.the_shell.notch.intro." + niveau)));
        player.sendSystemMessage(notch(Component.translatable("quiz.the_shell." + niveau + "." + numero).withStyle(ChatFormatting.YELLOW)));
    }

    private static String propre(String s) {
        s = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\b(le|la|les|l|un|une|des|du|de|the|a|an)\\b", " ");
        return s.trim().replaceAll(" +", " ");
    }

    @SubscribeEvent
    public static void reponse(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        Question q = EN_COURS.get(player.getUUID());
        if (q == null) return;
        ServerLevel level = player.serverLevel();
        if (level.getGameTime() > q.fin() || player.blockPosition().distSqr(q.autel()) > 32 * 32) {
            EN_COURS.remove(player.getUUID());
            return;
        }
        event.setCanceled(true);
        EN_COURS.remove(player.getUUID());
        player.sendSystemMessage(Component.literal("<" + player.getGameProfile().getName() + "> " + event.getRawText()).withStyle(ChatFormatting.GRAY));
        String dit = propre(event.getRawText());
        boolean juste = false;
        for (String bonne : REPONSES[q.niveau() - 1][q.numero()]) juste |= propre(bonne).equals(dit);
        if (juste) {
            player.sendSystemMessage(notch(Component.translatable("message.the_shell.notch.correct." + q.niveau()).withStyle(ChatFormatting.GREEN)));
            level.playSound(null, q.autel(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.0F);
            NotchAltarBlock.resolu(level, q.autel());
            TempleSealBlock.ouvrirNiveau(level, q.autel(), q.niveau());
        } else {
            player.sendSystemMessage(notch(Component.translatable("message.the_shell.notch.wrong").withStyle(ChatFormatting.RED)));
            level.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 0.6F);
            if (q.niveau() >= 2) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            if (q.niveau() >= 3) player.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
            poser(player, q.autel(), q.niveau());
        }
    }
}
