package net.ussm.theshell.api;

/*
 * Mozilla Public License Version 2.0
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

public final class RunevisionErosionFilter {

    private RunevisionErosionFilter() {
    }

    private static final float TAU = 6.28318530717959f;
    private static final float EPSILON = 1e-10f;

    // Settings
    private static final float HEIGHT_AMP = 0.125f;
    private static final float TERRAIN_HEIGHT_OFFSET_X = -0.65f;
    private static final float TERRAIN_HEIGHT_OFFSET_Y = 0f;

    private static final float SCALE = 0.25f;
    private static final float STRENGTH = 0.22f;
    private static final float GULLY_WEIGHT = 0.5f;
    private static final float DETAIL = 1.5f;

    private static final float RIDGES_ROUNDING = 0.0f;
    private static final float CREASES_ROUNDING = 0.0f;
    private static final float ROUNDING_Z = 0.0f;
    private static final float ROUNDING_W = 2.0f;

    private static final float ONSET_X = 1.25f;
    private static final float ONSET_Y = 1.25f;

    private static final float ASSUMED_SLOPE_X = 0.5f;
    private static final float ASSUMED_SLOPE_Y = 1.0f;

    private static final float CELL_SCALE = 0.5f;
    private static final float NORMALIZATION = 0.5f;

    private static final int OCTAVES = 3;
    private static final float LACUNARITY = 1.5f;
    private static final float GAIN = 0.6f;
    
    // Interns structs

    private static final class SHvec2 { // Originally a shader
        float x, y;

        SHvec2(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final class SHvec4 { // Originally a shader
        float x, y, dx, dy;

        SHvec4(float x, float y, float dx, float dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }
    }
    
    // Some functions

    private static float fract(float x) {
        return x - (float) Math.floor(x);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float dot(float ax, float ay, float bx, float by) {
        return ax * bx + ay * by;
    }

    private static float length(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    private static float safeLength(float x, float y) {
        return Math.max(length(x, y), EPSILON);
    }

    private static float powInv(float t, float power) {
        return  1f - (float)Math.pow(1f - Math.clamp(t, 0f, 1f), power);
    }

    private static float easeOut(float t) {
        float v = 1f - Math.clamp(t, 0f, 1f);
        return 1f - v * v;
    }

    private static float smoothStart(float t, float smoothing) {
        if (smoothing <= EPSILON) return t;
        if (t >= smoothing) return t - 0.5f * smoothing;
        return 0.5f * t * t / smoothing;
    }

    private static SHvec2 safeNormalize(float x, float y) {
        float len = length(x, y);

        if (len > EPSILON) return new SHvec2(x / len, y / len);
        return new SHvec2(0.0f, 0.0f);
    }

    // 2D Hash

    private static SHvec2 hash(float x, float y) {
        final float kx = 0.3183099f;
        final float ky = 0.3678794f;

        x = x * kx + ky;
        y = y * ky + kx;

        float f = fract(x * y * (x + y));

        return new SHvec2(
                2f * fract(16.0f * kx * f) - 1f,
                2f * fract(16.0f * ky * f) - 1f
        );
    }

    // Phacelle noise

    private static SHvec4 phacelleNoise(float px, float py, float normX, float normY, float freq, float offset, float normalization) {
        float sideX = -normY * freq * TAU;
        float sideY = normX * freq * TAU;

        offset *= TAU;

        float intX = (float)Math.floor(px);
        float intY = (float)Math.floor(py);

        float fracX = fract(px);
        float fracY = fract(py);

        float phaseX = 0f;
        float phaseY = 0f;
        float weightSum = 0f;

        for (int i = -1; i <= 2; i++) {
            for (int j = -1; j <= 2; j++) {
                float gridX = intX + i;
                float gridY = intY + j;

                SHvec2 random = hash(gridX, gridY);

                float randomX = random.x * 0.5f;
                float randomY = random.y * 0.5f;

                float vx = fracX - i - randomX;
                float vy = fracY - j - randomY;

                float weight = Math.max(0f, (float)Math.exp(-(vx * vx + vy * vy) * 2f) - 0.01111f);

                weightSum += weight;

                float waveInput = dot(vx, vy, sideX, sideY) + offset;

                phaseX += (float)Math.cos(waveInput) * weight;
                phaseY += (float)Math.sin(waveInput) * weight;
            }
        }

        if (weightSum > EPSILON) {
            phaseX /= weightSum;
            phaseY /= weightSum;
        }

        float magnitude = length(phaseX, phaseY);
        magnitude = Math.max(1f - normalization, magnitude);

        return new SHvec4(phaseX / magnitude, phaseY / magnitude, sideX, sideY);
    }

    // Erosion filter

    public static float ErosionFilter(float x, float y, float dx, float dy, float height) {
        height = Math.clamp(height * HEIGHT_AMP, -1f, 1f);
        dx *= HEIGHT_AMP;
        dy *= HEIGHT_AMP;

        float fadeTarget = Math.clamp(height / (0.6f * HEIGHT_AMP), -1f, 1f);
        float inputFadeTarget = fadeTarget;
        float hx = height;
        float slopeX = dx;
        float slopeY = dy;

        float strength = STRENGTH * 0.15f;
        float freq = 1f / (SCALE * CELL_SCALE);
        float slopeLength = safeLength(slopeX, slopeY);

        float magnitude = 0f;
        float roundingMult = 1f;

        float roundingForInput = lerp(CREASES_ROUNDING, RIDGES_ROUNDING, Math.clamp(fadeTarget + 0.5f, 0f, 1f));
        roundingForInput *= ROUNDING_Z;

        // The combined accumulating mask, based first on initial slope, and later on slope of each octave too.
        float combiMask = easeOut(smoothStart(slopeLength * ONSET_X, roundingForInput * ONSET_X));

        // Determining the strength of the initial slope used for gully directions
        // based on the specified mix of the actual slope and an assumed slope.
        float gullySlopeX = lerp(slopeX, slopeX / slopeLength * ASSUMED_SLOPE_X, ASSUMED_SLOPE_Y);
        float gullySlopeY = lerp(slopeY, slopeY / slopeLength * ASSUMED_SLOPE_X, ASSUMED_SLOPE_Y);

        for (int i = 0; i < OCTAVES; i++) {
            // Calculate and add gullies to the height and slope
            SHvec2 normalized = safeNormalize(gullySlopeX, gullySlopeY);
            SHvec4 ph = phacelleNoise(x * freq, y * freq, normalized.x, normalized.y, CELL_SCALE, 0.25f, NORMALIZATION);

            // Multiply with freq since p was multiplied with freq.
            // Negate since we use slope directions that point down.
            float phDX = ph.dx * -freq;
            float phDY = ph.dy * -freq;

            // Amount of slope as value from 0 to 1.
            float sloping = Math.abs(ph.y);

            // Add non-masked, normalized slope to gullySlope, for use by subsequent octaves.
            // It's normalized to use the steepest part of the sine wave everywhere.
            float slopeSign = Math.signum(ph.y);
            gullySlopeX += slopeSign * phDX * strength * GULLY_WEIGHT;
            gullySlopeY += slopeSign * phDY * strength * GULLY_WEIGHT;

            // Handle height offset and approximate output slope.

            // Process the gradient of the gullies.
            float gulliesDX = ph.y * phDX;
            float gulliesDY = ph.y * phDY;

            // Fade gullies towards fadeTarget based on combiMask.
            float fadedHeight = lerp(fadeTarget, ph.x * GULLY_WEIGHT, combiMask);
            gulliesDX *= GULLY_WEIGHT * combiMask;
            gulliesDY *= GULLY_WEIGHT * combiMask;

            // Apply height offset and derivative (slope) according to strength of current octave.
            hx += fadedHeight * strength;
            slopeX += gulliesDX * strength;
            slopeY += gulliesDY * strength;
            magnitude += strength;

            // Update fadeTarget to include the new octave.
            fadeTarget = fadedHeight;

            // Update the mask to include the new octave.
            float roundingForOctave = lerp(CREASES_ROUNDING, RIDGES_ROUNDING, Math.clamp(ph.x + 0.5f, 0f, 1f)) * roundingMult;
            float newMask = easeOut(smoothStart(sloping * ONSET_Y, roundingForOctave * ONSET_Y));
            combiMask = powInv(combiMask, DETAIL) * newMask;

            // Prepare the next octave.
            strength *= GAIN;
            freq *= LACUNARITY;
            roundingMult *= ROUNDING_W;
        }

        float offset = lerp(TERRAIN_HEIGHT_OFFSET_X, -inputFadeTarget, TERRAIN_HEIGHT_OFFSET_Y) * magnitude;

        return (hx + offset) / (HEIGHT_AMP * 2f);
    }
}