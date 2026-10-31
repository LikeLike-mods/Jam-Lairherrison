package net.ussm.the_shell.mixins;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.TheShell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(method = {"getRainLevel", "getThunderLevel"}, at = @At("HEAD"), cancellable = true)
    private void the_shell$atollSansMeteo(float partialTick, CallbackInfoReturnable<Float> cir) {
        ResourceLocation dimension = ((Level) (Object) this).dimension().location();
        if (dimension.getNamespace().equals(TheShell.MOD_ID) && dimension.getPath().equals("test_dim")) {
            cir.setReturnValue(0.0F);
        }
    }
}
