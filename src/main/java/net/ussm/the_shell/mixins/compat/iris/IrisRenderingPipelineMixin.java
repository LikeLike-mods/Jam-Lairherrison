package net.ussm.the_shell.mixins.compat.iris;

import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.ussm.the_shell.client.sky.ShaderpackOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IrisRenderingPipeline.class, remap = false)
public abstract class IrisRenderingPipelineMixin {
    @Inject(method = "finalizeLevelRendering", at = @At("TAIL"))
    private void the_shell$drawAtollOverlay(CallbackInfo ci) {
        ShaderpackOverlay.apply();
    }
}
