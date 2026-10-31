package net.ussm.the_shell.client.water;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.ussm.the_shell.client.compat.Shaderpacks;
import net.ussm.the_shell.config.ClientConfig;
import net.ussm.the_shell.world.water.ImpactWave;
import net.ussm.the_shell.world.water.WaveImpacts;
import net.ussm.the_shell.network.WaveImpactPayload;
import net.ussm.the_shell.worldgen.TestChunkGenerator;
import net.ussm.the_shell.TheShell;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.WeakHashMap;
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

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
    private static final Map<Object, Object2IntMap<String>> programLocations = new WeakHashMap<>();
    private static int shoreUnit = -1;
    private static boolean checkedRenderer;
    private static boolean supportedRenderer;

    private OceanRenderer() {}

    @FunctionalInterface
    private interface UniformSink {
        void set(String name, float... values);
    }

    private static boolean allowed(@Nullable ClientLevel level) {
        return level != null && level.dimension().location().equals(WaveImpacts.DIMENSION) && ClientConfig.WAVES_ENABLED.get()
                && rendererSupported();
    }

    private static boolean ready(@Nullable ClientLevel level) {
        return allowed(level) && map != null && map.ready() && map.matches(level, ClientConfig.SHORE_MAP_SIZE.get(), SEA_LEVEL);
    }

    private static boolean rendererSupported() {
        if (!checkedRenderer) {
            checkedRenderer = true;
            supportedRenderer = true;
            for (String id : new String[]{"embeddium", "oculus", "optifine"}) {
                if (ModList.get().isLoaded(id)) {
                    supportedRenderer = false;
                    TheShell.LOGGER.warn("Ocean waves disabled: {} replaces the terrain renderer and is not supported "
                            + "(vanilla, Sodium and Iris are).", id);
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
        ClientLevel level = Minecraft.getInstance().level;
        if (shader == null || !ready(level)) return;
        upload(shader, level, event.getCamera().getPosition(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
        preparedLevel = level;
    }

    public static boolean upload(ShaderInstance target, Vec3 camera, float partial) {
        ClientLevel level = Minecraft.getInstance().level;
        if (!ready(level)) return false;
        upload(target, level, camera, partial);
        return true;
    }

    private static void upload(ShaderInstance target, ClientLevel level, Vec3 camera, float partial) {
        target.setSampler("ShoreMap", map.textureId());
        writeUniforms((name, v) -> {
            var uniform = target.safeGetUniform(name);
            switch (v.length) {
                case 1 -> uniform.set(v[0]);
                case 2 -> uniform.set(v[0], v[1]);
                case 3 -> uniform.set(v[0], v[1], v[2]);
                default -> uniform.set(v[0], v[1], v[2], v[3]);
            }
        }, level, camera, partial);
    }

    public static void bindTerrainProgram(Object program, int handle) {
        Object2IntMap<String> locations = programLocations.computeIfAbsent(program, p -> {
            Object2IntOpenHashMap<String> created = new Object2IntOpenHashMap<>();
            created.defaultReturnValue(Integer.MIN_VALUE);
            return created;
        });
        int wavesOn = location(locations, handle, "WavesOn");
        if (wavesOn < 0) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (!ready(level)) {
            GL20.glUniform1f(wavesOn, 0.0F);
            return;
        }
        GL20.glUniform1f(wavesOn, 1.0F);
        bindShoreMap(location(locations, handle, "ShoreMap"));
        writeUniforms((name, v) -> {
            int at = location(locations, handle, name);
            if (at < 0) return;
            switch (v.length) {
                case 1 -> GL20.glUniform1f(at, v[0]);
                case 2 -> GL20.glUniform2f(at, v[0], v[1]);
                case 3 -> GL20.glUniform3f(at, v[0], v[1], v[2]);
                default -> GL20.glUniform4f(at, v[0], v[1], v[2], v[3]);
            }
        }, level, mc.gameRenderer.getMainCamera().getPosition(), mc.getTimer().getGameTimeDeltaPartialTick(false));
    }

    private static int location(Object2IntMap<String> locations, int handle, String name) {
        int at = locations.getInt(name);
        if (at == Integer.MIN_VALUE) {
            at = GL20.glGetUniformLocation(handle, WaveShaderPatcher.PREFIX + name);
            locations.put(name, at);
        }
        return at;
    }

    private static void bindShoreMap(int sampler) {
        if (sampler < 0) return;
        if (shoreUnit < 0) shoreUnit = GL11.glGetInteger(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS) - 1;
        int previous = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + shoreUnit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, map.textureId());
        GL13.glActiveTexture(previous);
        GL20.glUniform1i(sampler, shoreUnit);
    }

    private static void writeUniforms(UniformSink out, ClientLevel level, Vec3 camera, float partial) {
        double seconds = ((double) level.getGameTime() + partial) / 20.0;
        syncImpactLevel(level);
        writeImpacts(out, level.getGameTime(), partial, camera);
        out.set("MapInfo", (float) (camera.x - map.originX()), (float) (camera.z - map.originZ()), (float) map.size);
        out.set("SeaOffset", (float) (SEA_LEVEL - 1.0 / 9.0 - 0.001 - camera.y));
        out.set("WaveAmplitude", ClientConfig.WAVE_AMPLITUDE.get().floatValue());
        out.set("FoamStrength", ClientConfig.FOAM_STRENGTH.get().floatValue());
        out.set("WavePhase",
                phase(0.24 * (0.8 * camera.x + 0.6 * camera.z) - 0.90 * seconds),
                phase(0.48 * (-0.6 * camera.x + 0.8 * camera.z) - 1.28 * seconds),
                phase(0.78 * (0.96 * camera.x - 0.28 * camera.z) - 1.75 * seconds),
                phase(1.45 * seconds));
        out.set("PixelOrigin", modulo(camera.x, 4096), modulo(camera.z, 4096));
        out.set("AboveWater", camera.y >= SEA_LEVEL - 1.0 / 9.0 ? 1.0F : 0.0F);
        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        sprite(out, "WaterStill", atlas.apply(WATER_STILL));
        sprite(out, "WaterFlow", atlas.apply(WATER_FLOW));
        sprite(out, "WaterOverlay", atlas.apply(WATER_OVERLAY));
    }

    private static void writeImpacts(UniformSink out, long gameTick, float partial, Vec3 camera) {
        impacts.prune(gameTick);
        float[] radii = new float[ImpactWave.MAX_ACTIVE];
        for (int i = 0; i < ImpactWave.MAX_ACTIVE; i++) {
            if (i < impacts.size()) {
                ImpactWave w = impacts.get(i);
                out.set("Impact" + i, (float) (w.x() - camera.x),
                        (float) (w.z() - camera.z), w.ageSeconds(gameTick, partial), w.strength());
                radii[i] = w.initialRadius();
            } else {
                out.set("Impact" + i, 0.0F, 0.0F, 10.0F, 0.0F);
            }
        }
        out.set("ImpactRadii0", radii[0], radii[1], radii[2], radii[3]);
        out.set("ImpactRadii1", radii[4], radii[5], radii[6], radii[7]);
    }

    private static void sprite(UniformSink out, String name, TextureAtlasSprite sprite) {
        out.set(name, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    private static float modulo(double value, double divisor) {
        return (float) (value - Math.floor(value / divisor) * divisor);
    }

    private static float phase(double value) { return modulo(value, TAU); }

    @Nullable
    public static ShaderInstance activeShader() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == preparedLevel && allowed(level) && !Shaderpacks.inUse() ? shader : null;
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
