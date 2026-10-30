package net.ussm.the_shell.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.init.ModAttachments;
import net.ussm.the_shell.init.ModSounds;

// glisse sur une peau de banane : allonge sur le dos (la pose du lit, sans lit) jusqu'a ce qu'on appuie sur espace
@EventBusSubscriber(modid = TheShell.MOD_ID)
public class SlipEvents {

    // le serveur apprend que le joueur a glisse ou s'est releve, et le dit aux autres
    public static void received(ServerPlayer player, boolean allonge) {
        if (player.getData(ModAttachments.SLIPPED) == allonge) return;
        player.setData(ModAttachments.SLIPPED, allonge);
        player.syncData(ModAttachments.SLIPPED);
        if (allonge) player.level().playSound(player, player.blockPosition(), ModSounds.BANANA_SLIP.get(), SoundSource.PLAYERS, 1.0F, 0.95F + player.getRandom().nextFloat() * 0.1F);
    }

    // le jeu remet la pose debout a chaque tick, on la remet couchee juste apres (des deux cotes, pour que les autres le voient)
    @SubscribeEvent
    public static void pose(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.getData(ModAttachments.SLIPPED)) {
            if (player.isPassenger() || player.isInWater() || player.getAbilities().flying) {
                player.setData(ModAttachments.SLIPPED, false);
                if (player instanceof ServerPlayer sp) sp.syncData(ModAttachments.SLIPPED);
            } else {
                player.setPose(Pose.SLEEPING);
            }
        }
    }
}
