package net.ussm.theshell.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.ussm.theshell.TheShell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Minecraft noircit le bas du ciel quand les yeux du joueur sont sous Y=63 (le niveau de la mer de l'Overworld).
// Dans l'atoll la mer est à Y=0 : on met l'horizon tout en bas du monde pour garder un ciel bleu partout.
@Mixin(ClientLevel.ClientLevelData.class)
public class ClientLevelDataMixin {
    @Inject(method = "getHorizonHeight", at = @At("HEAD"), cancellable = true)
    private void theshell$horizonAtoll(LevelHeightAccessor level, CallbackInfoReturnable<Double> cir) {
        if (level instanceof ClientLevel clientLevel && clientLevel.dimension().location().getNamespace().equals(TheShell.MOD_ID)
                && clientLevel.dimension().location().getPath().equals("test_dim")) {
            cir.setReturnValue((double) level.getMinBuildHeight());
        }
    }
}
