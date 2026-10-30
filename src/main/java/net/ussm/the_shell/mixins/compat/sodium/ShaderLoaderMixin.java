package net.ussm.the_shell.mixins.compat.sodium;

import net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.resources.ResourceLocation;
import net.ussm.the_shell.client.water.WaveShaderPatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShaderLoader.class, remap = false)
public abstract class ShaderLoaderMixin {
    @Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
    private static void the_shell$patchOceanWaves(ResourceLocation name, CallbackInfoReturnable<String> cir) {
        if (!name.getNamespace().equals("sodium")) return;
        switch (name.getPath()) {
            case "blocks/block_layer_opaque.vsh" -> cir.setReturnValue(WaveShaderPatcher.patchSodiumVertex(cir.getReturnValue()));
            case "blocks/block_layer_opaque.fsh" -> cir.setReturnValue(WaveShaderPatcher.patchSodiumFragment(cir.getReturnValue()));
            default -> {}
        }
    }
}
