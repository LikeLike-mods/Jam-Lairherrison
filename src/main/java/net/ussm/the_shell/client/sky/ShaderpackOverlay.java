package net.ussm.the_shell.client.sky;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.io.UncheckedIOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.client.water.OceanRenderer;
import net.ussm.the_shell.config.ClientConfig;
import net.ussm.the_shell.world.water.WaveImpacts;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public final class ShaderpackOverlay {
    private static final ResourceLocation SHADER = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "shaderpack_overlay");
    @Nullable private static ShaderInstance shader;
    @Nullable private static TextureTarget scratch;
    private static final Matrix4f projectionInverse = new Matrix4f();
    private static final Matrix4f viewInverse = new Matrix4f();
    private static final float[] skyColor = new float[3];
    private static final float[] horizonColor = new float[3];
    private static final Vec3 NORMAL_SKY = new Vec3(0x78 / 255.0, 0xA7 / 255.0, 1.0);
    private static float daylight;
    private static boolean captured;

    private ShaderpackOverlay() {}

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), SHADER, DefaultVertexFormat.POSITION),
                    loaded -> shader = loaded);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load " + SHADER, e);
        }
    }

    @SubscribeEvent
    public static void capture(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        captured = false;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(WaveImpacts.DIMENSION)) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        projectionInverse.set(event.getProjectionMatrix()).invert();
        viewInverse.set(event.getModelViewMatrix()).invert();
        Vec3 sky = level.getSkyColor(event.getCamera().getPosition(), partial);
        skyColor[0] = (float) sky.x;
        skyColor[1] = (float) sky.y;
        skyColor[2] = (float) sky.z;
        float[] fog = RenderSystem.getShaderFogColor();
        System.arraycopy(fog, 0, horizonColor, 0, 3);
        daylight = level.getSkyDarken(partial);
        captured = true;
    }

    public static Vec3 normalSky(Vec3 sky) {
        return NORMAL_SKY.scale(luma(sky) / luma(NORMAL_SKY));
    }

    private static double luma(Vec3 c) {
        return 0.2126 * c.x + 0.7152 * c.y + 0.0722 * c.z;
    }

    public static void apply() {
        Minecraft mc = Minecraft.getInstance();
        if (!captured || shader == null) return;
        captured = false;
        if (mc.gameRenderer.getMainCamera().getFluidInCamera() != FogType.NONE) return;
        boolean colors = ClientConfig.SHADERPACK_COLORS.get();
        float sky = colors ? ClientConfig.SHADERPACK_SKY_STRENGTH.get().floatValue() : 0.0F;
        boolean sea = OceanRenderer.upload(shader, mc.gameRenderer.getMainCamera().getPosition(),
                mc.getTimer().getGameTimeDeltaPartialTick(false));
        if (!sea && sky <= 0.0F) return;

        RenderTarget main = mc.getMainRenderTarget();
        if (scratch == null) {
            scratch = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
        } else if (scratch.width != main.width || scratch.height != main.height) {
            scratch.resize(main.width, main.height, Minecraft.ON_OSX);
        }

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        scratch.bindWrite(true);
        shader.setSampler("MainColor", main.getColorTextureId());
        shader.setSampler("MainDepth", main.getDepthTextureId());
        shader.safeGetUniform("ProjInv").set(projectionInverse);
        shader.safeGetUniform("ViewInv").set(viewInverse);
        shader.safeGetUniform("SkyColor").set(skyColor[0], skyColor[1], skyColor[2]);
        shader.safeGetUniform("HorizonColor").set(horizonColor[0], horizonColor[1], horizonColor[2]);
        shader.safeGetUniform("OrangeHeight").set(ClientConfig.SHADERPACK_ORANGE_HEIGHT.get().floatValue());
        shader.safeGetUniform("SkyStrength").set(sky);
        shader.safeGetUniform("SeaOn").set(sea ? 1.0F : 0.0F);
        shader.safeGetUniform("FoamLight").set(daylight, daylight, daylight);
        float far = mc.gameRenderer.getRenderDistance();
        shader.safeGetUniform("FoamFade").set(far * 0.4F, far * 0.8F);
        shader.apply();
        BufferBuilder quad = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        quad.addVertex(0.0F, 0.0F, 0.0F);
        quad.addVertex(1.0F, 0.0F, 0.0F);
        quad.addVertex(1.0F, 1.0F, 0.0F);
        quad.addVertex(0.0F, 1.0F, 0.0F);
        BufferUploader.draw(quad.buildOrThrow());
        shader.clear();

        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, scratch.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, main.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, main.width, main.height, 0, 0, main.width, main.height,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        main.bindWrite(true);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
