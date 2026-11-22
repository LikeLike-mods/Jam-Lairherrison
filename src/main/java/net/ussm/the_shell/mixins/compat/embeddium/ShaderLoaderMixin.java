package net.ussm.the_shell.mixins.compat.embeddium;

import net.minecraft.resources.ResourceLocation;
import net.ussm.the_shell.client.water.WaveShaderPatcher;
import org.embeddedt.embeddium.impl.gl.shader.ShaderLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShaderLoader.class, remap = false)
public abstract class ShaderLoaderMixin {
    @Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
    private static void the_shell$patchOceanWaves(ResourceLocation name, CallbackInfoReturnable<String> cir) {
        if (!name.getNamespace().equals("embeddium")) return;
        switch (name.getPath()) {
            case "blocks/block_layer_opaque.vsh" -> cir.setReturnValue(WaveShaderPatcher.patchChunkVertex("Embeddium", cir.getReturnValue()));
            case "blocks/block_layer_opaque.fsh" -> cir.setReturnValue(WaveShaderPatcher.patchChunkFragment("Embeddium", cir.getReturnValue()));
            default -> {}
        }
    }
}
