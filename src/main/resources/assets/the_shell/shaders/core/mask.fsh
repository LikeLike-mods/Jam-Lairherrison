#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform int MaskEnabled;
uniform mat4 ModelToMask;
uniform vec3 MaskMin;
uniform vec3 MaskMax;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec3 modelPosition;

out vec4 fragColor;

void main() {
    if (MaskEnabled != 0) {
        vec3 inside = (ModelToMask * vec4(modelPosition, 1.0)).xyz;
        if (all(greaterThanEqual(inside, MaskMin)) &&
            all(lessThanEqual(inside, MaskMax))) {
            discard;
        }
    }

    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) discard;
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
