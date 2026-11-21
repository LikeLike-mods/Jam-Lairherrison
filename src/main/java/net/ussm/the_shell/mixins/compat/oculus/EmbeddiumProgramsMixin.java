package net.ussm.the_shell.mixins.compat.oculus;

import java.util.EnumMap;
import java.util.Map;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.ussm.the_shell.client.water.WaveShaderPatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.compat.embeddium.impl.oculus.EmbeddiumPrograms", remap = false)
public abstract class EmbeddiumProgramsMixin {
    @Inject(method = "transformShaders", at = @At("RETURN"), cancellable = true)
    private void the_shell$injectOceanWaves(ProgramSource source, AlphaTest alphaTest, ProgramSet programSet,
                                            CallbackInfoReturnable<Map<PatchShaderType, String>> cir) {
        Map<PatchShaderType, String> shaders = cir.getReturnValue();
        String vertex = shaders.get(PatchShaderType.VERTEX);
        String patched = WaveShaderPatcher.patchIrisVertex(source.getName(), vertex);
        if (patched == null || patched.equals(vertex)) return;
        Map<PatchShaderType, String> copy = new EnumMap<>(PatchShaderType.class);
        copy.putAll(shaders);
        copy.put(PatchShaderType.VERTEX, patched);
        cir.setReturnValue(copy);
    }
}
