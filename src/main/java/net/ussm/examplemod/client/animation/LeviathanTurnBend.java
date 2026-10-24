package net.ussm.examplemod.client.animation;

//rotations smooth
public final class LeviathanTurnBend {
    public static final float SIGN = 1.0F;

    private static final String[] FRONT = {"neck", "head"};
    private static final float[] FRONT_DEG = {6.0F, 5.0F};
    private static final String[] TAIL = {"tail1", "tail2", "tail3", "tail4", "tail_fin"};
    private static final float[] TAIL_DEG = {4.0F, 5.0F, 6.0F, 7.0F, 6.0F};
    private static final float BANK_DEG = 12.0F;
    private static final float FIN_TUCK_DEG = 18.0F;
    private static final float PITCH_FACTOR = 0.8F;
    private static final float PITCH_MAX_DEG = 35.0F;
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    @FunctionalInterface
    public interface BoneAdder {
        void add(String bone, float x, float y, float z);
    }

    private LeviathanTurnBend() {
    }

    public static void apply(float bend, float pitchDeg, BoneAdder bones) {
        float signedBend = bend * SIGN;
        if (signedBend != 0.0F) {
            for (int i = 0; i < FRONT.length; i++) {
                bones.add(FRONT[i], 0.0F, -signedBend * FRONT_DEG[i] * DEG_TO_RAD, 0.0F);
            }
            for (int i = 0; i < TAIL.length; i++) {
                bones.add(TAIL[i], 0.0F, signedBend * TAIL_DEG[i] * DEG_TO_RAD, 0.0F);
            }
            bones.add("fin_right", 0.0F, -Math.max(signedBend, 0.0F) * FIN_TUCK_DEG * DEG_TO_RAD, 0.0F);
            bones.add("fin_left", 0.0F, -Math.min(signedBend, 0.0F) * FIN_TUCK_DEG * DEG_TO_RAD, 0.0F);
        }

        float pitch = Math.max(-PITCH_MAX_DEG, Math.min(PITCH_MAX_DEG, pitchDeg)) * PITCH_FACTOR;
        if (signedBend != 0.0F || pitch != 0.0F) {
            bones.add("body", -pitch * DEG_TO_RAD, 0.0F, -signedBend * BANK_DEG * DEG_TO_RAD);
        }
    }
}
