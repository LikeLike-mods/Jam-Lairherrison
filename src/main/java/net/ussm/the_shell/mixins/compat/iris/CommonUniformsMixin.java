package net.ussm.the_shell.mixins.compat.iris;

import net.irisshaders.iris.uniforms.CommonUniforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.client.sky.ShaderpackOverlay;
import net.ussm.the_shell.config.ClientConfig;
import net.ussm.the_shell.world.water.WaveImpacts;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CommonUniforms.class, remap = false)
public abstract class CommonUniformsMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private static void the_shell$normalAtollSky(CallbackInfoReturnable<Vector3d> cir) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(WaveImpacts.DIMENSION) || !ClientConfig.SHADERPACK_COLORS.get()) return;
        Vector3d sky = cir.getReturnValue();
        Vec3 normal = ShaderpackOverlay.normalSky(new Vec3(sky.x, sky.y, sky.z));
        cir.setReturnValue(new Vector3d(normal.x, normal.y, normal.z));
    }
}
