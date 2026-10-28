package net.ussm.examplemod.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.ussm.examplemod.ExampleMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// dans l'atoll la mer est a y=0, sinon le ciel devient noir sous y=63
@Mixin(ClientLevel.ClientLevelData.class)
public class ClientLevelDataMixin {
    @Inject(method = "getHorizonHeight", at = @At("HEAD"), cancellable = true)
    private void examplemod$horizonAtoll(LevelHeightAccessor level, CallbackInfoReturnable<Double> cir) {
        if (level instanceof ClientLevel clientLevel && clientLevel.dimension().location().getNamespace().equals(ExampleMod.MOD_ID)
                && clientLevel.dimension().location().getPath().equals("test_dim")) {
            cir.setReturnValue((double) level.getMinBuildHeight());
        }
    }
}
