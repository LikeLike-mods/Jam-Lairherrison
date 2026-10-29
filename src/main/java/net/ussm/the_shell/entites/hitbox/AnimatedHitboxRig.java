package net.ussm.the_shell.entites.hitbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.ussm.the_shell.entites.animations.TurnBend;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.keyframe.AnimationPoint;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.animation.keyframe.KeyframeStack;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.object.BakedAnimations;

public final class AnimatedHitboxRig {
    private static final Map<String, AnimatedHitboxRig> CACHE = new ConcurrentHashMap<>();
    private final Map<String, Bone> bones = new LinkedHashMap<>();
    private final Map<String, Clip> clips = new HashMap<>();
    private record Bone(String name, String parent, Vector3f pivot, Vector3f rotation, AABB bounds) {}
    private record Clip(double length, Map<String, BoneAnimation> tracks) {}
    public record Section(String name, List<String> bones) {
        public static Section of(String name, String... bones) { return new Section(name, List.of(bones)); }
    }

    public static AnimatedHitboxRig load(String name) {
        return CACHE.computeIfAbsent(name, AnimatedHitboxRig::new);
    }

    private AnimatedHitboxRig(String name) {
        var geometry = read("geo/entity/" + name + ".geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        for (var element : geometry.getAsJsonArray("bones")) {
            var b = element.getAsJsonObject();
            String boneName = b.get("name").getAsString();
            Vector3f pivot = vector(b, "pivot", 0).mul(-1, 1, 1).div(16);
            Vector3f rotation = vector(b, "rotation", 0).mul(-Mth.DEG_TO_RAD, -Mth.DEG_TO_RAD, Mth.DEG_TO_RAD);
            AABB bounds = null;
            if (b.has("cubes")) for (var cubeElement : b.getAsJsonArray("cubes")) {
                var cube = cubeElement.getAsJsonObject();
                Vector3f origin = vector(cube, "origin", 0), size = vector(cube, "size", 0);
                AABB box = new AABB(-(origin.x + size.x) / 16, origin.y / 16, origin.z / 16,
                        -origin.x / 16, (origin.y + size.y) / 16, (origin.z + size.z) / 16);
                Vector3f cp = vector(cube, "pivot", 0).mul(-1, 1, 1).div(16);
                Vector3f cr = vector(cube, "rotation", 0).mul(-Mth.DEG_TO_RAD, -Mth.DEG_TO_RAD, Mth.DEG_TO_RAD);
                Matrix4f transform = new Matrix4f().translate(cp).rotateZYX(cr.z, cr.y, cr.x).translate(-cp.x, -cp.y, -cp.z);
                box = transformBounds(box, transform);
                bounds = bounds == null ? box : bounds.minmax(box);
            }
            bones.put(boneName, new Bone(boneName, b.has("parent") ? b.get("parent").getAsString() : null, pivot, rotation, bounds));
        }
        BakedAnimations animations = KeyFramesAdapter.GEO_GSON.fromJson(read("animations/entity/" + name + ".animation.json").get("animations"), BakedAnimations.class);
        animations.animations().forEach((key, animation) -> {
            Map<String, BoneAnimation> tracks = new HashMap<>();
            for (var track : animation.boneAnimations()) tracks.put(track.boneName(), track);
            clips.put(key, new Clip(animation.length(), tracks));
        });
    }

    public void validateSections(List<Section> sections) {
        for (Section section : sections) for (String name : section.bones()) {
            if (!bones.containsKey(name) || bones.get(name).bounds() == null)
                throw new IllegalArgumentException("Hitbox section " + section.name() + " references missing/empty bone " + name);
        }
    }

    public <T extends LivingEntity & AnimatedHitboxEntity> void update(T entity, List<Section> sections,
                                                                       AnimatedHitboxPart[] parts, float partialTick) {
        float scale = entity.getModelScale() * entity.getScale();
        float yaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        Matrix4f root = new Matrix4f().translate(0, (float) entity.getModelYOffset() * entity.getScale(), 0)
                .rotateY((180 - yaw) * Mth.DEG_TO_RAD);
        if (entity.rotatesModelWithPitch() && entity.isInWater() && !entity.isPlayingSpecialPose()) root.rotateX(-entity.getViewXRot(partialTick) * Mth.DEG_TO_RAD);
        root.scale(scale);
        Map<String, Vector3f> offsets = new HashMap<>();
        if (entity.isTurnBendingAllowed()) TurnBend.apply(entity.getTurnProfile(), entity.getTurnBend(partialTick), entity.getViewXRot(partialTick),
                (bone, x, y, z) -> offsets.put(bone, new Vector3f(x, y, z)));
        Clip clip = clips.get(entity.getHitboxAnimation());
        double tick = entity.getHitboxAnimationTick(partialTick);
        if (clip != null) tick = entity.isPlayingSpecialPose() ? Math.min(tick, clip.length()) : tick % clip.length();
        Map<String, Matrix4f> matrices = new HashMap<>();
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            AABB bounds = null;
            for (String name : section.bones()) {
                Matrix4f transform = boneTransform(name, root, clip, tick, offsets, matrices);
                AABB boneBounds = transformBounds(bones.get(name).bounds(), transform);
                bounds = bounds == null ? boneBounds : bounds.minmax(boneBounds);
            }
            parts[i].updateBounds(bounds.inflate(0.1).move(entity.getX(), entity.getY(), entity.getZ()));
        }
    }

    private Matrix4f boneTransform(String name, Matrix4f root, Clip clip, double tick,
                                   Map<String, Vector3f> offsets, Map<String, Matrix4f> cache) {
        Matrix4f existing = cache.get(name);
        if (existing != null) return existing;
        Bone bone = bones.get(name);
        Matrix4f matrix = new Matrix4f(bone.parent() == null ? root : boneTransform(bone.parent(), root, clip, tick, offsets, cache));
        BoneAnimation track = clip == null ? null : clip.tracks().get(name);
        Vector3f position = track == null ? new Vector3f() : sample(track.positionKeyFrames(), tick, 0);
        Vector3f rotation = new Vector3f(bone.rotation());
        if (track != null) rotation.add(sample(track.rotationKeyFrames(), tick, 0));
        Vector3f extra = offsets.get(name);
        if (extra != null) rotation.add(extra);
        Vector3f scale = track == null ? new Vector3f(1) : sample(track.scaleKeyFrames(), tick, 1);
        Vector3f pivot = bone.pivot();
        matrix.translate(-position.x / 16, position.y / 16, position.z / 16)
                .translate(pivot).rotateZYX(rotation.z, rotation.y, rotation.x).scale(scale)
                .translate(-pivot.x, -pivot.y, -pivot.z);
        cache.put(name, matrix);
        return matrix;
    }

    private static Vector3f sample(KeyframeStack<Keyframe<MathValue>> frames, double tick, float fallback) {
        return new Vector3f(sampleAxis(frames.xKeyframes(), tick, fallback), sampleAxis(frames.yKeyframes(), tick, fallback), sampleAxis(frames.zKeyframes(), tick, fallback));
    }

    private static float sampleAxis(List<Keyframe<MathValue>> frames, double tick, float fallback) {
        double left = tick;
        float last = fallback;
        for (var frame : frames) {
            last = (float) frame.endValue().get();
            if (frame.length() == 0) continue;
            if (left <= frame.length()) return (float) EasingType.lerpWithOverride(new AnimationPoint(frame, left, frame.length(), frame.startValue().get(), frame.endValue().get()), null);
            left -= frame.length();
        }
        return last;
    }

    private static AABB transformBounds(AABB box, Matrix4f matrix) {
        double minX = Double.POSITIVE_INFINITY, minY = minX, minZ = minX;
        double maxX = Double.NEGATIVE_INFINITY, maxY = maxX, maxZ = maxX;
        for (int i = 0; i < 8; i++) {
            Vector3f p = matrix.transformPosition(new Vector3f((float) ((i & 1) == 0 ? box.minX : box.maxX),
                    (float) ((i & 2) == 0 ? box.minY : box.maxY), (float) ((i & 4) == 0 ? box.minZ : box.maxZ)));
            minX = Math.min(minX, p.x); minY = Math.min(minY, p.y); minZ = Math.min(minZ, p.z);
            maxX = Math.max(maxX, p.x); maxY = Math.max(maxY, p.y); maxZ = Math.max(maxZ, p.z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static Vector3f vector(JsonObject object, String key, float fallback) {
        if (!object.has(key)) return new Vector3f(fallback);
        JsonArray array = object.getAsJsonArray(key);
        return new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
    }

    private static JsonObject read(String path) {
        try (var input = AnimatedHitboxRig.class.getClassLoader().getResourceAsStream("assets/the_shell/" + path)) {
            if (input == null) throw new IllegalStateException("Missing animated hitbox asset: " + path);
            try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) { return JsonParser.parseReader(reader).getAsJsonObject(); }
        } catch (java.io.IOException e) { throw new IllegalStateException("Cannot read hitbox asset " + path, e); }
    }
}
