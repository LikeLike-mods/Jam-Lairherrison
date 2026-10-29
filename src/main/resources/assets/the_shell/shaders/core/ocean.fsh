#version 150

#moj_import <fog.glsl>
#moj_import <the_shell:waves.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform vec2 PixelOrigin;
uniform float FoamStrength;
uniform float AboveWater;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 surfacePosition;
in vec3 waterLight;
in float waterSurface;

out vec4 fragColor;

float grain(vec2 p) {
    vec3 q = fract(vec3(p.xyx) * 0.1031);
    q += dot(q, q.yzx + 33.33);
    return fract((q.x + q.y) * q.z);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    vec3 n = normalize(cross(dFdx(surfacePosition), dFdy(surfacePosition)));
    n *= n.y < 0.0 ? -1.0 : 1.0;
    if (waterSurface > 0.01 && AboveWater > 0.5) {
        vec3 shore = shoreData(surfacePosition.xz);
        float top = smoothstep(0.55, 0.9, n.y);
        float coverage = shore.z * waterSurface * top;
        vec2 pixel = floor(mod(surfacePosition.xz + PixelOrigin, 4096.0) * 8.0);
        float noise = grain(pixel);
        float coarse = grain(floor(pixel / 4.0));
        float breakup = mix(noise, coarse, 0.55);
        float beach = beachFactor(shore);
        float breaker = sin(shorePhase(surfacePosition.xz, shore.y));
        float surf = smoothstep(0.48 + breakup * 0.35, 0.86 + breakup * 0.12, breaker);
        surf *= beach * smoothstep(0.4, 1.8, shore.y);
        float wash = (1.0 - smoothstep(0.5, 1.8 + breakup * 0.8, shore.y))
                   * (0.45 + 0.35 * sin(WavePhase.w + breakup * 2.0)) * beach;
        vec3 s = sin(oceanPhases(surfacePosition.xz));
        float crest = dot(s, vec3(0.60, 0.27, 0.13));
        float whitecap = smoothstep(0.74 + breakup * 0.17, 0.94 + breakup * 0.04, crest)
                       * smoothstep(4.0, 12.0, shore.x) * (1.0 - beach) * 0.55;
        float porosity = smoothstep(0.18, 0.52, breakup);
        float impactFoam = impactField(surfacePosition.xz).y;
        float foam = clamp(max(max(max(surf, wash), whitecap), impactFoam) * porosity * FoamStrength * coverage, 0.0, 1.0);
        vec3 faceNormal = floor(n * 12.0 + 0.5) / 12.0;
        float shade = clamp(dot(faceNormal, normalize(vec3(0.3, 1.0, 0.2))), 0.0, 1.0);
        vec3 water = color.rgb * (0.92 + 0.14 * shade);
        vec3 view = normalize(-surfacePosition);
        float fresnel = pow(1.0 - max(dot(n, view), 0.0), 3.0);
        water += vec3(0.035, 0.045, 0.05) * fresnel * waterLight * ColorModulator.rgb;
        color.rgb = mix(color.rgb, water, coverage);
        color.rgb = mix(color.rgb, vec3(0.91, 0.96, 0.95) * waterLight * ColorModulator.rgb, foam);
        color.a = mix(color.a, max(color.a, 0.94 * ColorModulator.a), foam);
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
