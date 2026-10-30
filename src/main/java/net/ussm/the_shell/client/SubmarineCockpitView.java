package net.ussm.the_shell.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.entites.Submarine;

// le cockpit vu de l'interieur : une bulle de verre avec ses montants, le sol en verre devant, le tableau de bord et le manche.
// il n'existe que dans la tete du pilote (vue a la 1re personne), dehors on voit le sous-marin normal
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class SubmarineCockpitView {
    private static final RenderType TYPE = RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "textures/misc/white.png"));
    private static final int METAL = 0xFF46505A, METAL_SOMBRE = 0xFF333A42, CADRE = 0xFF2A2F36, CADRE_CLAIR = 0xFF59636E,
            VERRE = 0x2C8FD3F0, SOL_VERRE = 0x3C7CC2DC;
    // le manche suit la vitesse du sous-marin en douceur
    private static float mancheAvant, mancheCote;

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof Submarine sub) || !mc.options.getCameraType().isFirstPerson()) return;
        float pt = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float yaw = Mth.rotLerp(pt, sub.yRotO, sub.getYRot());
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(TYPE);
        int lumiere = LightTexture.pack(11, 11), plein = LightTexture.FULL_BRIGHT;

        Vec3 v = sub.getDeltaMovement();
        Vec3 devant = Vec3.directionFromRotation(0.0F, yaw);
        float vitesseAvant = (float) Mth.clamp(v.dot(devant) / 0.35, -1.0, 1.0);
        float vitesseCote = (float) Mth.clamp(v.dot(new Vec3(devant.z, 0, -devant.x)) / 0.2, -1.0, 1.0);
        mancheAvant += (vitesseAvant - mancheAvant) * 0.08F;
        mancheCote += (vitesseCote - mancheCote) * 0.08F;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        // le sol : des plaques de metal sous le siege (avec leurs joints), du verre devant pour voir en dessous
        plaques(vc, pose, -1.1F, -1.27F, -1.0F, 1.1F, -1.22F, 0.5F, METAL, lumiere);
        for (float z = -0.9F; z < 0.5F; z += 0.35F) box(vc, pose, -1.1F, -1.22F, z, 1.1F, -1.215F, z + 0.02F, METAL_SOMBRE, lumiere);
        box(vc, pose, -1.1F, -1.27F, 0.5F, 1.1F, -1.25F, 1.7F, SOL_VERRE, lumiere);
        // la bulle
        box(vc, pose, -1.2F, -1.22F, 1.7F, 1.2F, 0.9F, 1.72F, VERRE, lumiere);
        box(vc, pose, -1.22F, -1.22F, -0.4F, -1.2F, 0.9F, 1.7F, VERRE, lumiere);
        box(vc, pose, 1.2F, -1.22F, -0.4F, 1.22F, 0.9F, 1.7F, VERRE, lumiere);
        box(vc, pose, -1.2F, 0.9F, -0.4F, 1.2F, 0.92F, 1.7F, VERRE, lumiere);
        // les montants, avec un liseré clair et des rivets
        for (float x : new float[]{-1.2F, 1.2F}) {
            plaques(vc, pose, x - 0.04F, -1.24F, 1.66F, x + 0.04F, 0.94F, 1.74F, CADRE, lumiere);
            box(vc, pose, x - 0.045F, -1.24F, 1.655F, x + 0.045F, 0.94F, 1.665F, CADRE_CLAIR, lumiere);
            plaques(vc, pose, x - 0.04F, 0.88F, -0.4F, x + 0.04F, 0.96F, 1.74F, CADRE, lumiere);
            box(vc, pose, x - 0.04F, -1.24F, -0.4F, x + 0.04F, -1.16F, 1.74F, CADRE, lumiere);
            for (float y = -1.0F; y < 0.9F; y += 0.4F) box(vc, pose, x - 0.05F, y, 1.64F, x + 0.05F, y + 0.03F, 1.66F, CADRE_CLAIR, lumiere);
        }
        box(vc, pose, -1.24F, 0.88F, 1.66F, 1.24F, 0.96F, 1.74F, CADRE, lumiere);
        box(vc, pose, -1.24F, -1.24F, 1.66F, 1.24F, -1.16F, 1.74F, CADRE, lumiere);
        box(vc, pose, -1.24F, 0.88F, 0.56F, 1.24F, 0.96F, 0.64F, CADRE, lumiere);
        box(vc, pose, -1.24F, 0.875F, 0.55F, 1.24F, 0.885F, 0.65F, CADRE_CLAIR, lumiere);
        // derriere : le mur de la cabine avec la trappe qui descend
        plaques(vc, pose, -1.2F, -1.22F, -0.42F, 1.2F, 0.92F, -0.38F, METAL, lumiere);
        box(vc, pose, -0.44F, -1.22F, -0.375F, 0.44F, 0.24F, -0.365F, CADRE_CLAIR, lumiere);
        box(vc, pose, -0.4F, -1.22F, -0.37F, 0.4F, 0.2F, -0.36F, 0xFF1C2024, lumiere);

        // le tableau de bord : un caisson avec un rebord clair et une facade plus sombre
        plaques(vc, pose, -0.7F, -0.95F, 1.05F, 0.7F, -0.6F, 1.45F, 0xFF2F353D, lumiere);
        box(vc, pose, -0.72F, -0.6F, 1.03F, 0.72F, -0.57F, 1.47F, CADRE_CLAIR, lumiere);
        box(vc, pose, -0.66F, -0.92F, 1.045F, 0.66F, -0.63F, 1.05F, 0xFF22272D, lumiere);
        box(vc, pose, -0.23F, -0.79F, 1.035F, 0.23F, -0.63F, 1.045F, 0xFF101418, lumiere);
        box(vc, pose, -0.21F, -0.77F, 1.03F, 0.21F, -0.65F, 1.035F, 0xFF2BAFD8, plein);
        float t = sub.tickCount + pt;
        // deux barres d'un pixel de haut, avec un pixel vide entre
        for (int i = 0; i < 2; i++) {
            float larg = 0.06F + 0.24F * (0.5F + 0.5F * (float) Math.sin(t * 0.13 + i * 1.7));
            box(vc, pose, -6 * P, -24 * P + i * 2 * P, 1.028F, -6 * P + larg, -23 * P + i * 2 * P, 1.03F, 0xFF9FF4FF, plein);
        }
        float[] aiguilles = {(float) Math.sin(t * 0.07) * 0.9F + (float) Math.sin(t * 0.31) * 0.15F, 1.2F - 2.4F * sub.getEnergy() / Submarine.MAX_ENERGY};
        float[] places = {-0.45F, 0.45F};
        for (int i = 0; i < 2; i++) {
            box(vc, pose, places[i] - 0.13F, -0.87F, 1.04F, places[i] + 0.13F, -0.61F, 1.045F, CADRE_CLAIR, lumiere);
            box(vc, pose, places[i] - 0.11F, -0.85F, 1.035F, places[i] + 0.11F, -0.63F, 1.04F, 0xFFE6E2D4, plein);
            for (int k = -2; k <= 2; k++) {
                pose.pushPose();
                pose.translate(places[i], -0.74F, 1.034F);
                pose.mulPose(Axis.ZP.rotation(k * 0.5F));
                box(vc, pose, -0.004F, 0.08F, -0.002F, 0.004F, 0.1F, 0.0F, 0xFF2A2A2A, plein);
                pose.popPose();
            }
            pose.pushPose();
            pose.translate(places[i], -0.74F, 1.03F);
            pose.mulPose(Axis.ZP.rotation(aiguilles[i]));
            box(vc, pose, -0.007F, 0.0F, -0.004F, 0.007F, 0.095F, 0.0F, i == 0 ? 0xFF2A2A2A : 0xFFD8402C, plein);
            pose.popPose();
            box(vc, pose, places[i] - 0.015F, -0.755F, 1.026F, places[i] + 0.015F, -0.725F, 1.03F, 0xFF555555, plein);
        }
        int[] voyants = {0xFF4FE07A, 0xFFE6B53A, 0xFFE0482F, 0xFF4FA8F0};
        for (int i = 0; i < 4; i++) {
            float x = -0.6F + i * 0.06F;
            boolean allume = i != 2 || (sub.tickCount / 10) % 2 == 0;
            box(vc, pose, x - 0.02F, -0.9F, 1.04F, x + 0.02F, -0.86F, 1.045F, allume ? voyants[i] : 0xFF30343A, plein);
        }
        // a droite du siege : une petite console avec le bouton pour descendre dans la cabine (ctrl pour tourner la tete vers elle).
        // le bouton s'allume quand on le vise. en pixels de la grille : cadre 7x5, bouton 5x3, une fleche de 3 puis 1 pixel vers la trappe
        boolean vise = regardeBouton(mc, sub);
        plaques(vc, pose, -36 * P, -39 * P, 0.0F, -22 * P, -24 * P, 20 * P, 0xFF2F353D, lumiere);
        box(vc, pose, -37 * P, -24 * P, -P, -21 * P, -23 * P, 21 * P, CADRE_CLAIR, lumiere);
        float dessus = -23 * P;
        box(vc, pose, -32 * P, dessus, 6 * P, -25 * P, dessus + 0.01F, 11 * P, 0xFFE6B53A, lumiere);
        box(vc, pose, -31 * P, dessus, 7 * P, -26 * P, dessus + 0.025F, 10 * P, vise ? 0xFFFFE27A : 0xFFC8962A, vise ? plein : lumiere);
        box(vc, pose, -30 * P, dessus, 8 * P, -27 * P, dessus + 0.03F, 9 * P, 0xFF1A1A1A, plein);
        box(vc, pose, -29 * P, dessus, 7 * P, -28 * P, dessus + 0.03F, 8 * P, 0xFF1A1A1A, plein);
        // le manche : un socle, une tige, une poignee avec ses bagues et un bouton rouge dessus
        pose.pushPose();
        pose.translate(0.0F, -1.22F, 0.55F);
        box(vc, pose, -0.09F, 0.0F, -0.09F, 0.09F, 0.03F, 0.09F, METAL_SOMBRE, lumiere);
        box(vc, pose, -0.05F, 0.03F, -0.05F, 0.05F, 0.06F, 0.05F, 0xFF1E2226, lumiere);
        pose.mulPose(Axis.XP.rotationDegrees(mancheAvant * 22.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(-mancheCote * 22.0F));
        box(vc, pose, -0.02F, 0.06F, -0.02F, 0.02F, 0.36F, 0.02F, 0xFF8C949C, lumiere);
        box(vc, pose, -0.04F, 0.36F, -0.04F, 0.04F, 0.52F, 0.04F, 0xFF22262B, lumiere);
        box(vc, pose, -0.03F, 0.52F, -0.03F, 0.03F, 0.545F, 0.03F, 0xFFB02226, lumiere);
        box(vc, pose, -0.015F, 0.545F, -0.015F, 0.015F, 0.555F, 0.015F, 0xFFE8524E, plein);
        pose.popPose();
        pose.popPose();
        buffers.endBatch(TYPE);

        // les bras du pilote (avec son skin) qui tiennent la poignee du manche
        org.joml.Vector3f poignee = new org.joml.Vector3f(0.0F, 0.5F, 0.0F)
                .rotateZ((float) Math.toRadians(-mancheCote * 22.0F)).rotateX((float) Math.toRadians(mancheAvant * 22.0F)).add(0.0F, -1.22F, 0.55F);
        RenderType peau = RenderType.entityTranslucent(mc.player.getSkin().texture());
        VertexConsumer bras = buffers.getBuffer(peau);
        int lumierePeau = LightTexture.pack(13, 13);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        // les epaules sous les coins de l'ecran (comme la main vanilla), un poing de chaque cote de la poignee
        bras(bras, pose, new org.joml.Vector3f(-0.4F, -0.65F, -0.1F), new org.joml.Vector3f(poignee).add(-0.13F, -0.03F, -0.02F), 40, 16, lumierePeau);
        bras(bras, pose, new org.joml.Vector3f(0.4F, -0.65F, -0.1F), new org.joml.Vector3f(poignee).add(0.13F, -0.03F, -0.02F), 32, 48, lumierePeau);
        pose.popPose();
        buffers.endBatch(peau);
    }

    // on vise le bouton ? on ramene le regard dans le repere du sous-marin et on regarde ou il touche la facade
    public static boolean regardeBouton(Minecraft mc, Submarine sub) {
        if (!mc.options.getCameraType().isFirstPerson()) return false;
        org.joml.Vector3f vue = mc.player.getViewVector(1.0F).toVector3f().rotateY((float) Math.toRadians(sub.getYRot()));
        // le bouton est sur le dessus de la console de droite
        if (vue.y > -0.05F) return false;
        float t = -23 * P / vue.y, x = vue.x * t, z = vue.z * t;
        return x > -32 * P && x < -25 * P && z > 6 * P && z < 11 * P;
    }

    // ctrl (la touche pour sprinter) : regard libre aux commandes, le sous-marin garde son cap
    @SubscribeEvent
    public static void regard(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        boolean ctrl = mc.player != null && mc.player.getVehicle() instanceof Submarine && mc.options.keySprint.isDown();
        if (Submarine.regardLibre && !ctrl) Submarine.rattrapage = true;
        if (ctrl || mc.player == null || !(mc.player.getVehicle() instanceof Submarine)) Submarine.rattrapage = false;
        Submarine.regardLibre = ctrl;
    }

    // clic droit sur le bouton : on previent le serveur
    @SubscribeEvent
    public static void clic(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (!event.isUseItem() || mc.player == null || !(mc.player.getVehicle() instanceof Submarine sub) || !regardeBouton(mc, sub)) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(net.ussm.the_shell.network.CabinButtonPayload.INSTANCE);
    }

    // un bras = une boite de l'epaule a la main, avec les uv du bras du skin (u, v = coin du bras dans le skin 64x64)
    private static void bras(VertexConsumer vc, PoseStack pose, org.joml.Vector3f epaule, org.joml.Vector3f main, int u, int v, int light) {
        org.joml.Vector3f dir = new org.joml.Vector3f(main).sub(epaule);
        float l = dir.length();
        pose.pushPose();
        pose.translate(epaule.x, epaule.y, epaule.z);
        pose.mulPose(new org.joml.Quaternionf().rotationTo(new org.joml.Vector3f(0.0F, 0.0F, 1.0F), dir.normalize()));
        PoseStack.Pose p = pose.last();
        // les proportions du bras vanilla : 4 pixels de large pour 12 de long, un pixel = 1/16 de bloc comme les blocs du monde
        float px = 1.0F / 16.0F, w = 2 * px;
        // les 4 faces longues et le bout du poing. les 12 pixels du skin partent de la main sans etre etires,
        // et plus haut (jusque derriere l'epaule, pour qu'on ne voie jamais ou il commence) on prolonge la rangee du haut
        float coude = l - 12 * px;
        float[][] cotes = {{-w, -w, -w, w}, {-w, w, w, w}, {w, w, w, -w}, {w, -w, -w, -w}};
        for (int i = 0; i < 4; i++) {
            float[] c = cotes[i];
            float u0 = (u + i * 4) / 64.0F, u1 = (u + i * 4 + 4) / 64.0F, v0 = (v + 4) / 64.0F, v1 = (v + 16) / 64.0F, vh = (v + 5) / 64.0F;
            float nx = (c[0] + c[2]) / 2, ny = (c[1] + c[3]) / 2;
            sommet(vc, p, c[0], c[1], -0.6F, u0, v0, light, nx, ny, 0);
            sommet(vc, p, c[0], c[1], coude, u0, vh, light, nx, ny, 0);
            sommet(vc, p, c[2], c[3], coude, u1, vh, light, nx, ny, 0);
            sommet(vc, p, c[2], c[3], -0.6F, u1, v0, light, nx, ny, 0);
            sommet(vc, p, c[0], c[1], coude, u0, v0, light, nx, ny, 0);
            sommet(vc, p, c[0], c[1], l, u0, v1, light, nx, ny, 0);
            sommet(vc, p, c[2], c[3], l, u1, v1, light, nx, ny, 0);
            sommet(vc, p, c[2], c[3], coude, u1, v0, light, nx, ny, 0);
        }
        float bu0 = (u + 8) / 64.0F, bu1 = (u + 12) / 64.0F, bv0 = v / 64.0F, bv1 = (v + 4) / 64.0F;
        sommet(vc, p, -w, -w, l, bu0, bv0, light, 0, 0, 1);
        sommet(vc, p, -w, w, l, bu0, bv1, light, 0, 0, 1);
        sommet(vc, p, w, w, l, bu1, bv1, light, 0, 0, 1);
        sommet(vc, p, w, -w, l, bu1, bv0, light, 0, 0, 1);
        pose.popPose();
    }

    private static void sommet(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z, float u, float v, int light, float nx, float ny, float nz) {
        vc.addVertex(p, x, y, z).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }

    // une surface en metal decoupee en petites plaques, chacune d'un gris un tout petit peu different
    private static void plaques(VertexConsumer vc, PoseStack pose, float x0, float y0, float z0, float x1, float y1, float z1, int argb, int light) {
        float pas = 6 * P;
        int nx = Math.max(1, Math.round((x1 - x0) / pas)), ny = Math.max(1, Math.round((y1 - y0) / pas)), nz = Math.max(1, Math.round((z1 - z0) / pas));
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                for (int k = 0; k < nz; k++) {
                    int h = (i * 73856093) ^ (j * 19349663) ^ (k * 83492791) ^ argb;
                    float f = 0.9F + ((h >>> 8) & 255) / 255.0F * 0.18F;
                    box(vc, pose, x0 + (x1 - x0) * i / nx, y0 + (y1 - y0) * j / ny, z0 + (z1 - z0) * k / nz,
                            x0 + (x1 - x0) * (i + 1) / nx, y0 + (y1 - y0) * (j + 1) / ny, z0 + (z1 - z0) * (k + 1) / nz, teinte(argb, f), light);
                }
            }
        }
    }

    // tout tombe sur une grille de 1/32 de bloc (= un pixel du skin sur les bras), sinon on a des pixels de toutes les tailles (mixels).
    // l'axe le plus fin d'une plaque reste tel quel : c'est juste l'epaisseur qui sert a empiler les couches
    private static final float P = 1.0F / 32.0F;

    private static float[] grille(float a0, float a1) {
        float b0 = Math.round(a0 / P) * P, b1 = Math.round(a1 / P) * P;
        if (b1 - b0 < P) {
            b0 = Math.round(((a0 + a1) / 2 - P / 2) / P) * P;
            b1 = b0 + P;
        }
        return new float[]{b0, b1};
    }

    // chaque face a sa propre lumiere (dessus clair, dessous sombre) pour que ca ait du relief
    private static void box(VertexConsumer vc, PoseStack pose, float x0, float y0, float z0, float x1, float y1, float z1, int argb, int light) {
        float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        int fin = dx <= dy && dx <= dz ? 0 : dy <= dz ? 1 : 2;
        boolean epais = Math.min(dx, Math.min(dy, dz)) >= P * 0.75F;
        float[] g;
        if (epais || fin != 0) { g = grille(x0, x1); x0 = g[0]; x1 = g[1]; }
        if (epais || fin != 1) { g = grille(y0, y1); y0 = g[0]; y1 = g[1]; }
        if (epais || fin != 2) { g = grille(z0, z1); z0 = g[0]; z1 = g[1]; }
        PoseStack.Pose p = pose.last();
        quad(vc, p, teinte(argb, 0.55F), light, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        quad(vc, p, teinte(argb, 1.0F), light, 0, 1, 0, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        quad(vc, p, teinte(argb, 0.8F), light, 0, 0, -1, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        quad(vc, p, teinte(argb, 0.8F), light, 0, 0, 1, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1);
        quad(vc, p, teinte(argb, 0.68F), light, -1, 0, 0, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0);
        quad(vc, p, teinte(argb, 0.68F), light, 1, 0, 0, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
    }

    private static int teinte(int argb, float f) {
        int r = Math.min(255, (int) (((argb >> 16) & 255) * f)), g = Math.min(255, (int) (((argb >> 8) & 255) * f)), b = Math.min(255, (int) ((argb & 255) * f));
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose p, int argb, int light, float nx, float ny, float nz, float... v) {
        for (int i = 0; i < 4; i++) {
            vc.addVertex(p, v[i * 3], v[i * 3 + 1], v[i * 3 + 2]).setColor(argb).setUv(i == 1 || i == 2 ? 1 : 0, i >= 2 ? 1 : 0)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
        }
    }
}
