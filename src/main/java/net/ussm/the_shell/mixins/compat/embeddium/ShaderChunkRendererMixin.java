package net.ussm.the_shell.mixins.compat.embeddium;

import net.ussm.the_shell.client.water.OceanRenderer;
import org.embeddedt.embeddium.impl.gl.shader.GlProgram;
import org.embeddedt.embeddium.impl.render.chunk.ShaderChunkRenderer;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderInterface;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
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
        if (pass.isReverseOrder() && activeProgram != null) {
            OceanRenderer.bindTerrainProgram(activeProgram, activeProgram.handle());
        }
    }
}
