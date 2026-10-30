package net.ussm.the_shell.client.mask;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.util.RenderUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class MaskState {
    private final List<MaskClipper.Volume> volumes = new ArrayList<>();
    private final List<MaskClipper.Volume> readOnlyVolumes = Collections.unmodifiableList(volumes);
    private boolean hasClippedBone;
    private boolean hasMask;

    private MaskState() {}

    public static boolean isMask(String name) {
        return name.equals("mask") || name.startsWith("mask_");
    }

    static boolean isInClippedTree(GeoBone bone, Set<String> clippedBoneNames) {
        boolean clipped = false;
        for (GeoBone cursor = bone; cursor != null; cursor = cursor.getParent()) {
            if (isMask(cursor.getName())) return false;
            clipped |= clippedBoneNames.contains(cursor.getName());
        }
        return clipped;
    }

    public List<MaskClipper.Volume> volumes() {
        return readOnlyVolumes;
    }

    public static MaskState capture(PoseStack stack, BakedGeoModel model, Set<String> clippedBoneNames,
                                    Consumer<String> warning) {
        MaskState state = new MaskState();
        for (GeoBone root : model.topLevelBones()) {
            state.captureTree(stack, root, false, clippedBoneNames, warning);
        }
        if (!state.hasClippedBone) {
            warning.accept("None of the bones to cut exist among " + clippedBoneNames + ": nothing will be cut.");
        }
        if (!state.hasMask) warning.accept("No mask or mask_* bone: the model is rendered without cutting.");
        return state;
    }

    private void captureTree(PoseStack stack, GeoBone bone, boolean inMask, Set<String> clippedBoneNames,
                             Consumer<String> warning) {
        boolean control = inMask || isMask(bone.getName());
        hasClippedBone |= !control && clippedBoneNames.contains(bone.getName());
        hasMask |= isMask(bone.getName());
        stack.pushPose();
        try {
            RenderUtil.prepMatrixForBone(stack, bone);
            if (control) {
                for (GeoCube cube : bone.getCubes()) captureCube(stack, bone, cube, warning);
            }
            for (GeoBone child : bone.getChildBones()) {
                captureTree(stack, child, control, clippedBoneNames, warning);
            }
        } finally {
            stack.popPose();
        }
    }

    private void captureCube(PoseStack stack, GeoBone bone, GeoCube cube, Consumer<String> warning) {
        double minX = Double.POSITIVE_INFINITY, minY = minX, minZ = minX;
        double maxX = Double.NEGATIVE_INFINITY, maxY = maxX, maxZ = maxX;
        for (GeoQuad quad : cube.quads()) {
            if (quad == null) continue;
            for (GeoVertex vertex : quad.vertices()) {
                Vector3f p = vertex.position();
                if (!Float.isFinite(p.x()) || !Float.isFinite(p.y()) || !Float.isFinite(p.z())) {
                    warning.accept("Non-finite coordinates in a cube of " + bone.getName() + "; volume ignored.");
                    return;
                }
                minX = Math.min(minX, p.x()); maxX = Math.max(maxX, p.x());
                minY = Math.min(minY, p.y()); maxY = Math.max(maxY, p.y());
                minZ = Math.min(minZ, p.z()); maxZ = Math.max(maxZ, p.z());
            }
        }
        if (!(maxX > minX && maxY > minY && maxZ > minZ)) {
            warning.accept("Flat, empty or incomplete cube in " + bone.getName() + "; volume ignored.");
            return;
        }
        stack.pushPose();
        try {
            RenderUtil.translateToPivotPoint(stack, cube);
            RenderUtil.rotateMatrixAroundCube(stack, cube);
            RenderUtil.translateAwayFromPivotPoint(stack, cube);
            float[] matrix = stack.last().pose().get(new float[16]);
            for (float value : matrix) {
                if (!Float.isFinite(value)) {
                    warning.accept("Non-finite transform for " + bone.getName() + "; volume ignored.");
                    return;
                }
            }
            MaskClipper.Volume volume = MaskClipper.Volume.fromAffine(matrix,
                    minX, minY, minZ, maxX, maxY, maxZ);
            if (volume != null) volumes.add(volume);
        } finally {
            stack.popPose();
        }
    }
}
