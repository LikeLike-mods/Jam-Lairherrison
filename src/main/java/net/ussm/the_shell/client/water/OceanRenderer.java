package net.ussm.the_shell.client.water;

import net.ussm.the_shell.config.ClientConfig;
import net.ussm.the_shell.world.water.ImpactWave;
import net.ussm.the_shell.world.water.WaveImpacts;
import net.ussm.the_shell.network.WaveImpactPayload;
import net.ussm.the_shell.worldgen.TestChunkGenerator;
import net.ussm.the_shell.TheShell;
import java.io.IOException;
import java.io.UncheckedIOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public final class OceanRenderer {
    private static final ResourceLocation OCEAN_SHADER = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "ocean");
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final ResourceLocation WATER_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
    private static final double TAU = Math.PI * 2;
    // First air block above the archipelago sea: the generator fills water up to and including SEA.
    private static final int SEA_LEVEL = TestChunkGenerator.SEA + 1;
    @Nullable private static ShaderInstance shader;
    @Nullable private static ShoreMap map;
    @Nullable private static ClientLevel preparedLevel;
    @Nullable private static ClientLevel impactLevel;
    private static final ImpactQueue impacts = new ImpactQueue();
    private static boolean checkedRenderer;
    private static boolean supportedRenderer;

    private OceanRenderer() {}

    private static boolean allowed(@Nullable ClientLevel level) {
        return level != null && level.dimension().location().equals(WaveImpacts.DIMENSION) && ClientConfig.WAVES_ENABLED.get()
                && rendererSupported();
    }

    private static boolean rendererSupported() {
        if (!checkedRenderer) {
            checkedRenderer = true;
            supportedRenderer = true;
            for (String id : new String[]{"sodium", "embeddium", "iris", "oculus", "optifine"}) {
                if (ModList.get().isLoaded(id)) {
                    supportedRenderer = false;
                    TheShell.LOGGER.warn("Ocean waves disabled: {} replaces the vanilla terrain/shader pipeline. "
                            + "This implementation targets the NeoForge vanilla renderer.", id);
                }
            }
        }
        return supportedRenderer;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!allowed(mc.level)) {
            releaseMap();
            resetImpacts();
            return;
        }
        syncImpactLevel(mc.level);
        impacts.prune(mc.level.getGameTime());
        if (mc.isPaused()) return;
        int size = ClientConfig.SHORE_MAP_SIZE.get();
        int sea = SEA_LEVEL;
        if (map == null || !map.matches(mc.level, size, sea)) {
            releaseMap();
            map = new ShoreMap(mc.level, size, sea);
        }
        map.tick(mc.gameRenderer.getMainCamera().getPosition());
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        releaseMap();
        resetImpacts();
    }

    private static void resetImpacts() {
        impacts.clear();
        impactLevel = null;
    }

    private static void syncImpactLevel(ClientLevel level) {
        if (impactLevel != level) {
            impacts.clear();
            impactLevel = level;
        }
    }

    public static void receiveImpact(WaveImpactPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (!allowed(level) || !payload.dimension().equals(level.dimension().location())) return;
        if (Math.abs(payload.wave().y() - (SEA_LEVEL - 1.0 / 9.0)) > 0.75) return;
        syncImpactLevel(level);
        impacts.add(payload.wave(), level.getGameTime());
    }

    private static void releaseMap() {
        preparedLevel = null;
        if (map != null) {
            map.close();
            map = null;
        }
    }

    @SubscribeEvent
    public static void prepareFrame(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        preparedLevel = null;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (!allowed(level) || shader == null || map == null || !map.ready()
                || !map.matches(level, ClientConfig.SHORE_MAP_SIZE.get(), SEA_LEVEL)) return;

        Vec3 camera = event.getCamera().getPosition();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        double seconds = ((double) level.getGameTime() + partial) / 20.0;
        syncImpactLevel(level);
        uploadImpacts(level.getGameTime(), partial, camera);
        shader.setSampler("ShoreMap", map.textureId());
        shader.safeGetUniform("MapInfo").set((float) (camera.x - map.originX()),
                (float) (camera.z - map.originZ()), (float) map.size);
        shader.safeGetUniform("SeaOffset").set((float) (SEA_LEVEL - 1.0 / 9.0 - 0.001 - camera.y));
        shader.safeGetUniform("WaveAmplitude").set(ClientConfig.WAVE_AMPLITUDE.get().floatValue());
        shader.safeGetUniform("FoamStrength").set(ClientConfig.FOAM_STRENGTH.get().floatValue());
        shader.safeGetUniform("WavePhase").set(
                phase(0.24 * (0.8 * camera.x + 0.6 * camera.z) - 0.90 * seconds),
                phase(0.48 * (-0.6 * camera.x + 0.8 * camera.z) - 1.28 * seconds),
                phase(0.78 * (0.96 * camera.x - 0.28 * camera.z) - 1.75 * seconds),
                phase(1.45 * seconds));
        shader.safeGetUniform("PixelOrigin").set(modulo(camera.x, 4096), modulo(camera.z, 4096));
        shader.safeGetUniform("AboveWater").set(camera.y >= SEA_LEVEL - 1.0 / 9.0 ? 1.0F : 0.0F);
        var atlas = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        spriteUniform("WaterStill", atlas.apply(WATER_STILL));
        spriteUniform("WaterFlow", atlas.apply(WATER_FLOW));
        spriteUniform("WaterOverlay", atlas.apply(WATER_OVERLAY));
        preparedLevel = level;
    }

    private static void uploadImpacts(long gameTick, float partial, Vec3 camera) {
        impacts.prune(gameTick);
        float[] radii = new float[ImpactWave.MAX_ACTIVE];
        for (int i = 0; i < ImpactWave.MAX_ACTIVE; i++) {
            if (i < impacts.size()) {
                ImpactWave w = impacts.get(i);
                shader.safeGetUniform("Impact" + i).set((float) (w.x() - camera.x),
                        (float) (w.z() - camera.z), w.ageSeconds(gameTick, partial), w.strength());
                radii[i] = w.initialRadius();
            } else {
                shader.safeGetUniform("Impact" + i).set(0.0F, 0.0F, 10.0F, 0.0F);
            }
        }
        shader.safeGetUniform("ImpactRadii0").set(radii[0], radii[1], radii[2], radii[3]);
        shader.safeGetUniform("ImpactRadii1").set(radii[4], radii[5], radii[6], radii[7]);
    }

    private static void spriteUniform(String name, TextureAtlasSprite sprite) {
        shader.safeGetUniform(name).set(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    private static float modulo(double value, double divisor) {
        return (float) (value - Math.floor(value / divisor) * divisor);
    }

    private static float phase(double value) { return modulo(value, TAU); }

    @Nullable
    public static ShaderInstance activeShader() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == preparedLevel && allowed(level) ? shader : null;
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        preparedLevel = null;
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    OCEAN_SHADER,
                    com.mojang.blaze3d.vertex.DefaultVertexFormat.BLOCK), loaded -> shader = loaded);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load " + OCEAN_SHADER, e);
        }
    }
}
