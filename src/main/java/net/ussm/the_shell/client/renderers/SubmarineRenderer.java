package net.ussm.the_shell.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.client.EyesGlowLayer;
import net.ussm.the_shell.entites.Submarine;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SubmarineRenderer extends GeoEntityRenderer<Submarine> {

    public SubmarineRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "submarine")) {
            // le manche suit le pilote et les aiguilles du tableau de bord bougent toutes seules
            @Override
            public void setCustomAnimations(Submarine sub, long instanceId, software.bernie.geckolib.animation.AnimationState<Submarine> state) {
                super.setCustomAnimations(sub, instanceId, state);
                float t = sub.tickCount + state.getPartialTick();
                float avant = 0, cote = 0;
                if (sub.getControllingPassenger() instanceof net.minecraft.world.entity.player.Player pilote) {
                    avant = pilote.zza;
                    cote = pilote.xxa;
                }
                float fa = avant, fc = cote;
                getBone("stick").ifPresent(b -> { b.setRotX(-fa * 0.4F); b.setRotZ(fc * 0.4F); });
                getBone("needle_l").ifPresent(b -> b.setRotZ((float) Math.sin(t * 0.07) * 0.9F + (float) Math.sin(t * 0.31) * 0.15F));
                getBone("needle_r").ifPresent(b -> b.setRotZ(1.2F - 2.4F * sub.getEnergy() / Submarine.MAX_ENERGY + (float) Math.sin(t * 0.5) * 0.05F));
            }
        });
        addRenderLayer(new EyesGlowLayer<>(this));
        this.shadowRadius = 1.0F;
    }

    @Override
    protected void applyRotations(Submarine submarine, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(partialTick, submarine.yRotO, submarine.getYRot())));
    }

    @Override
    public RenderType getRenderType(Submarine submarine, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
