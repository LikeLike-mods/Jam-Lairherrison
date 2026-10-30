#version 150

#moj_import <the_shell:waves.glsl>
#moj_import <the_shell:foam.glsl>

uniform sampler2D MainColor;
uniform sampler2D MainDepth;
uniform mat4 ProjInv;
uniform mat4 ViewInv;
uniform vec3 SkyColor;
uniform vec3 HorizonColor;
uniform float OrangeHeight;
uniform float SkyStrength;
uniform float SeaOn;
uniform float SeaOffset;
uniform vec3 FoamLight;
uniform vec2 FoamFade;

in vec2 texCoord;

out vec4 fragColor;

float luma(vec3 c) {
    return dot(c, vec3(0.2126, 0.7152, 0.0722));
}


void main() {
    vec4 color = texture(MainColor, texCoord);
    float depth = texture(MainDepth, texCoord).r;
    vec4 view = ProjInv * vec4(vec3(texCoord, depth) * 2.0 - 1.0, 1.0);
    vec3 position = (ViewInv * vec4(view.xyz / view.w, 1.0)).xyz;
    vec3 n = normalize(cross(dFdx(position), dFdy(position)));
    n *= n.y < 0.0 ? -1.0 : 1.0;
    if (depth >= 0.999999) {
        float elevation = max(normalize(position).y, 0.0);
        float band = mix(0.12, 0.45, OrangeHeight);
        vec3 target = mix(HorizonColor, SkyColor, smoothstep(0.0, band, elevation));
        float weight = 1.0 - smoothstep(band * 0.5, band + 0.3, elevation);
        vec3 recolored = clamp(target * (luma(color.rgb) / max(luma(target), 0.02)), 0.0, 1.0);
        color.rgb = mix(color.rgb, recolored, SkyStrength * weight);
    } else if (SeaOn > 0.5 && AboveWater > 0.5) {
        vec3 shore = shoreData(position.xz);
        float distance = length(position);
        float gap = abs(position.y - SeaOffset - waveHeight(position.xz, shore)) - distance * 0.0004;
        float surface = shore.z * (1.0 - smoothstep(0.04, 0.12, gap));
        float coverage = surface * smoothstep(0.55, 0.9, n.y) * (1.0 - smoothstep(FoamFade.x, FoamFade.y, distance));
        float foam = foamAmount(position, shore, coverage);
        color.rgb = mix(color.rgb, vec3(0.91, 0.96, 0.95) * FoamLight, foam);
    }
    fragColor = color;
}
