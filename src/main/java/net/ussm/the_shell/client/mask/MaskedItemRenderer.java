package net.ussm.the_shell.client.mask;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// cuts the listed bones (and their children) along the mask volumes
public class MaskedItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Set<String> reportedWarnings = new HashSet<>();
    private final Set<String> clippedBoneNames;
    private MaskState activeState;
    private boolean clipGeometry;

    public MaskedItemRenderer(GeoModel<T> model, String... clippedBoneNames) {
        super(model);
        if (clippedBoneNames.length == 0) {
            throw new IllegalArgumentException("At least one bone name to cut is required.");
        }
        for (String name : clippedBoneNames) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Bone names to cut can't be empty.");
            }
        }
        this.clippedBoneNames = Set.copyOf(List.of(clippedBoneNames));
    }

    @Override
    public void actuallyRender(PoseStack stack, T item, BakedGeoModel model,
                               @Nullable RenderType type, MultiBufferSource buffers,
                               @Nullable VertexConsumer consumer, boolean isReRender,
                               float partialTick, int packedLight, int packedOverlay, int colour) {
        MaskState previousState = activeState;
        boolean previousClip = clipGeometry;
        Matrix4f previousTranslations = this.modelRenderTranslations;
        try {
            clipGeometry = false;
            super.actuallyRender(stack, item, model, type, buffers, null, isReRender,
                    partialTick, packedLight, packedOverlay, colour);
            if (consumer == null) return;

            activeState = MaskState.capture(stack, model, this.clippedBoneNames, message -> {
                String key = getGeoModel().getModelResource(item, this) + ": " + message;
                if (reportedWarnings.add(key)) LOGGER.warn("[Mask] {}", key);
            });
            updateAnimatedTextureFrame(item);
            for (GeoBone root : model.topLevelBones()) {
                renderRecursively(stack, item, root, type, buffers, consumer, isReRender,
                        partialTick, packedLight, packedOverlay, colour);
            }
        } finally {
            activeState = previousState;
            clipGeometry = previousClip;
            if (previousState != null) this.modelRenderTranslations = previousTranslations;
        }
    }

    @Override
    public void renderRecursively(PoseStack stack, T item, GeoBone bone, RenderType type,
                                  MultiBufferSource buffers, VertexConsumer consumer, boolean isReRender,
                                  float partialTick, int packedLight, int packedOverlay, int colour) {
        if (inControlTree(bone)) return;
        boolean previousClip = clipGeometry;
        clipGeometry = MaskState.isInClippedTree(bone, this.clippedBoneNames);
        try {
            super.renderRecursively(stack, item, bone, type, buffers, consumer, isReRender,
                    partialTick, packedLight, packedOverlay, colour);
        } finally {
            clipGeometry = previousClip;
        }
    }

    @Override
    public void renderCubesOfBone(PoseStack stack, GeoBone bone, VertexConsumer consumer,
                                  int packedLight, int packedOverlay, int colour) {
        if (inControlTree(bone)) return;
        boolean previousClip = clipGeometry;
        clipGeometry = MaskState.isInClippedTree(bone, this.clippedBoneNames);
        try {
            super.renderCubesOfBone(stack, bone, consumer, packedLight, packedOverlay, colour);
        } finally {
            clipGeometry = previousClip;
        }
    }

    private static boolean inControlTree(GeoBone bone) {
        for (GeoBone cursor = bone; cursor != null; cursor = cursor.getParent()) {
            if (MaskState.isMask(cursor.getName())) return true;
        }
        return false;
    }

    @Override
    public void createVerticesOfQuad(GeoQuad quad, Matrix4f pose, Vector3f normal,
                                      VertexConsumer consumer, int packedLight, int packedOverlay, int colour) {
        if (!clipGeometry || activeState == null || activeState.volumes().isEmpty()) {
            super.createVerticesOfQuad(quad, pose, normal, consumer, packedLight, packedOverlay, colour);
            return;
        }
        List<MaskClipper.Vertex> polygon = new ArrayList<>(4);
        for (GeoVertex vertex : quad.vertices()) {
            Vector3f p = vertex.position();
            Vector4f transformed = pose.transform(new Vector4f(p.x(), p.y(), p.z(), 1));
            polygon.add(new MaskClipper.Vertex(transformed.x(), transformed.y(), transformed.z(),
                    vertex.texU(), vertex.texV()));
        }
        List<List<MaskClipper.Vertex>> pieces = MaskClipper.subtract(polygon, activeState.volumes());
        if (pieces.size() == 1 && pieces.getFirst() == polygon) {
            super.createVerticesOfQuad(quad, pose, normal, consumer, packedLight, packedOverlay, colour);
            return;
        }
        for (List<MaskClipper.Vertex> piece : pieces) {
            for (int i = 1; i + 1 < piece.size(); i += 2) {
                emit(consumer, piece.getFirst(), normal, packedLight, packedOverlay, colour);
                emit(consumer, piece.get(i), normal, packedLight, packedOverlay, colour);
                emit(consumer, piece.get(i + 1), normal, packedLight, packedOverlay, colour);
                emit(consumer, piece.get(Math.min(i + 2, piece.size() - 1)), normal, packedLight, packedOverlay, colour);
            }
        }
    }

    private static void emit(VertexConsumer consumer, MaskClipper.Vertex vertex, Vector3f normal,
                             int packedLight, int packedOverlay, int colour) {
        consumer.addVertex((float)vertex.x(), (float)vertex.y(), (float)vertex.z(), colour,
                (float)vertex.u(), (float)vertex.v(), packedOverlay, packedLight,
                normal.x(), normal.y(), normal.z());
    }
}
