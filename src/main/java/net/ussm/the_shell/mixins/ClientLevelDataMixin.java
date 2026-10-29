package net.ussm.the_shell.mixins;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.ussm.the_shell.TheShell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.ClientLevelData.class)
public class ClientLevelDataMixin {
    @Inject(method = "getHorizonHeight", at = @At("HEAD"), cancellable = true)
    private void the_shell$horizonAtoll(LevelHeightAccessor level, CallbackInfoReturnable<Double> cir) {
        if (level instanceof ClientLevel clientLevel && clientLevel.dimension().location().getNamespace().equals(TheShell.MOD_ID)
                && clientLevel.dimension().location().getPath().equals("test_dim")) {
            cir.setReturnValue((double) level.getMinBuildHeight());
        }
    }
}
