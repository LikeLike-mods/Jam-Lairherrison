package net.ussm.the_shell.mixins;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.ussm.the_shell.events.SleepEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/SleepStatus;areEnoughDeepSleeping(ILjava/util/List;)Z"))
    private boolean the_shell$nuitPartagee(SleepStatus status, int percentage, List<ServerPlayer> players) {
        return !SleepEvents.MEME_MONDE.contains(((ServerLevel) (Object) this).dimension()) && status.areEnoughDeepSleeping(percentage, players);
    }
}
