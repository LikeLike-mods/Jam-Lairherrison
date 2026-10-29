package net.ussm.the_shell.mixins.client;

import net.ussm.the_shell.client.water.OceanRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getRendertypeTranslucentShader", at = @At("RETURN"), cancellable = true)
    private static void the_shell$selectOceanShader(CallbackInfoReturnable<ShaderInstance> cir) {
        ShaderInstance shader = OceanRenderer.activeShader();
        if (shader != null) cir.setReturnValue(shader);
    }
}
