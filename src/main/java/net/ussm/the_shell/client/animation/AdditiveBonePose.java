package net.ussm.the_shell.client.animation;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import software.bernie.geckolib.cache.object.GeoBone;

public final class AdditiveBonePose {
    private final Map<GeoBone, Rotation> animatedRotations = new IdentityHashMap<>();
    private record Rotation(float x, float y, float z) {}

    public void clear() {
        animatedRotations.forEach((bone, rotation) -> {
            bone.setRotX(rotation.x());
            bone.setRotY(rotation.y());
            bone.setRotZ(rotation.z());
            bone.resetStateChanges();
        });
        animatedRotations.clear();
    }

    public void add(Function<String, GeoBone> lookup, String name, float x, float y, float z) {
        GeoBone bone = lookup.apply(name);
        if (bone == null) return;
        animatedRotations.computeIfAbsent(bone, b -> new Rotation(b.getRotX(), b.getRotY(), b.getRotZ()));
        bone.setRotX(bone.getRotX() + x);
        bone.setRotY(bone.getRotY() + y);
        bone.setRotZ(bone.getRotZ() + z);
    }
}
