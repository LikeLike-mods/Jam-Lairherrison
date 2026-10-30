package net.ussm.the_shell.mixins.compat.sodium;

import net.caffeinemc.mods.sodium.client.gl.shader.GlProgram;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.ussm.the_shell.client.water.OceanRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShaderChunkRenderer.class, remap = false)
public abstract class ShaderChunkRendererMixin {
    @Shadow
    protected GlProgram<ChunkShaderInterface> activeProgram;

    @Inject(method = "begin", at = @At("TAIL"))
    private void the_shell$uploadOceanWaves(TerrainRenderPass pass, CallbackInfo ci) {
        if (pass.isTranslucent() && activeProgram != null) {
            OceanRenderer.bindTerrainProgram(activeProgram, activeProgram.handle());
        }
    }
}
