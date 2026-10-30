#version 150

#moj_import <fog.glsl>
#moj_import <the_shell:waves.glsl>
#moj_import <the_shell:foam.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 surfacePosition;
in vec3 waterLight;
in float waterSurface;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    color = applyWaterSurface(color, surfacePosition, waterSurface, waterLight, ColorModulator);
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
