package net.ussm.the_shell.entites.animations;

import java.util.ArrayList;
import java.util.List;

public record TurnBendProfile(String bodyBone, List<SpineBone> spine, String rightFin, String leftFin,
                              float finFoldDegrees, float bankDegrees, float pitchFactor,
                              float maxPitchDegrees, float sign) {
    public TurnBendProfile {
        spine = List.copyOf(spine);
        for (SpineBone bone : spine) {
            if (bone.name().equals(bodyBone) || bone.name().equals("root")) {
                throw new IllegalArgumentException("The renderer owns root/body yaw");
            }
        }
    }

    public record SpineBone(String name, float yawDegrees) {}

    public static Builder builder(String bodyBone) {
        return new Builder(bodyBone);
    }

    public static final class Builder {
        private final String body;
        private final List<SpineBone> spine = new ArrayList<>();
        private String rightFin;
        private String leftFin;
        private float fold;
        private float bank;
        private float pitchFactor;
        private float pitchLimit;
        private float sign = 1;

        private Builder(String body) { this.body = body; }

        public Builder spine(String bone, float degrees) {
            spine.add(new SpineBone(bone, degrees));
            return this;
        }

        public Builder fins(String right, String left, float degrees) {
            rightFin = right;
            leftFin = left;
            fold = degrees;
            return this;
        }

        public Builder bank(float degrees) { bank = degrees; return this; }

        public Builder pitch(float factor, float limitDegrees) {
            pitchFactor = factor;
            pitchLimit = limitDegrees;
            return this;
        }

        public Builder sign(float value) { sign = value; return this; }

        public TurnBendProfile build() {
            return new TurnBendProfile(body, spine, rightFin, leftFin, fold, bank, pitchFactor, pitchLimit, sign);
        }
    }
}
