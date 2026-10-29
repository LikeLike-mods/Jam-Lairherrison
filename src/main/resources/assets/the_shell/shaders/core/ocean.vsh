#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>
#moj_import <the_shell:waves.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;
uniform vec4 WaterStill;
uniform vec4 WaterFlow;
uniform vec4 WaterOverlay;
uniform float SeaOffset;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec3 surfacePosition;
out vec3 waterLight;
out float waterSurface;

bool inSprite(vec2 uv, vec4 bounds) {
    return all(greaterThan(uv, bounds.xy)) && all(lessThan(uv, bounds.zw));
}

void main() {
    vec3 pos = Position + ChunkOffset;
    bool water = inSprite(UV0, WaterStill) || inSprite(UV0, WaterFlow) || inSprite(UV0, WaterOverlay);
    waterSurface = water && Normal.y > 0.9 && abs(pos.y - SeaOffset) < 0.30 ? 1.0 : 0.0;
    if (waterSurface > 0.5) {
        pos.y += waveHeight(pos.xz, shoreData(pos.xz));
    }
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertexDistance = fog_distance(pos, FogShape);
    vec4 light = minecraft_sample_lightmap(Sampler2, UV2);
    vertexColor = Color * light;
    waterLight = light.rgb;
    texCoord0 = UV0;
    surfacePosition = pos;
}
