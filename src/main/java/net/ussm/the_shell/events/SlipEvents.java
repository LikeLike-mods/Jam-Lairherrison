package net.ussm.the_shell.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.init.ModAttachments;
import net.ussm.the_shell.init.ModSounds;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class SlipEvents {

    public static void received(ServerPlayer player, boolean lyingDown) {
        if (player.getData(ModAttachments.SLIPPED) == lyingDown) return;
        player.setData(ModAttachments.SLIPPED, lyingDown);
        player.syncData(ModAttachments.SLIPPED);
        if (lyingDown) player.level().playSound(player, player.blockPosition(), ModSounds.BANANA_SLIP.get(), SoundSource.PLAYERS, 1.0F, 0.95F + player.getRandom().nextFloat() * 0.1F);
    }

    // mobs get up by themselves after 3 seconds
    private static final Map<UUID, Long> MOB_END = new HashMap<>();

    public static void slipMob(LivingEntity mob) {
        mob.setData(ModAttachments.SLIPPED, true);
        MOB_END.put(mob.getUUID(), mob.level().getGameTime() + 60);
    }

    @SubscribeEvent
    public static void mobSlipping(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide || !mob.getData(ModAttachments.SLIPPED)) return;
        long now = mob.level().getGameTime();
        if (now >= MOB_END.getOrDefault(mob.getUUID(), 0L) || mob.isInWaterOrBubble() || mob.isPassenger()) {
            mob.setData(ModAttachments.SLIPPED, false);
            MOB_END.remove(mob.getUUID());
            return;
        }
        mob.setTarget(null);
        mob.getNavigation().stop();
        // it slides for the first half second, then stays put
        if (MOB_END.get(mob.getUUID()) - now < 50) mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);
    }

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
