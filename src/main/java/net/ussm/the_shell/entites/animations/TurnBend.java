package net.ussm.the_shell.entites.animations;

public final class TurnBend {
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    @FunctionalInterface
    public interface BoneAdder {
        void add(String bone, float x, float y, float z);
    }

    private TurnBend() {}

    public static void apply(TurnBendProfile profile, float bend, float pitchDegrees, BoneAdder bones) {
        float signedBend = Math.max(-1, Math.min(1, bend)) * profile.sign();
        for (var bone : profile.spine()) {
            float yaw = signedBend * bone.yawDegrees() * DEG_TO_RAD;
            if (yaw != 0) bones.add(bone.name(), 0, yaw, 0);
        }
        if (profile.rightFin() != null && signedBend > 0) {
            bones.add(profile.rightFin(), 0, -signedBend * profile.finFoldDegrees() * DEG_TO_RAD, 0);
        }
        if (profile.leftFin() != null && signedBend < 0) {
            bones.add(profile.leftFin(), 0, -signedBend * profile.finFoldDegrees() * DEG_TO_RAD, 0);
        }
        float pitch = Math.max(-profile.maxPitchDegrees(), Math.min(profile.maxPitchDegrees(), pitchDegrees))
                * profile.pitchFactor();
        if (signedBend != 0 || pitch != 0) {
            bones.add(profile.bodyBone(), -pitch * DEG_TO_RAD, 0, -signedBend * profile.bankDegrees() * DEG_TO_RAD);
        }
    }
}
