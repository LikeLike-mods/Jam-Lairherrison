#moj_import <the_shell:impacts.glsl>
uniform sampler2D ShoreMap;
uniform vec3 MapInfo;
uniform vec4 WavePhase;
uniform float WaveAmplitude;

vec3 shoreData(vec2 p) {
    vec2 cell = p + MapInfo.xy;
    if (any(lessThan(cell, vec2(0.5))) || any(greaterThan(cell, vec2(MapInfo.z - 0.5)))) {
        return vec3(0.0);
    }
    vec3 data = texture(ShoreMap, cell / MapInfo.z).rgb;
    float edge = min(min(cell.x, cell.y), min(MapInfo.z - cell.x, MapInfo.z - cell.y));
    data.b *= smoothstep(1.0, 16.0, edge);
    return vec3(data.r * 32.0, data.g * 32.0, data.b);
}

vec3 oceanPhases(vec2 p) {
    return vec3(0.24 * dot(p, vec2(0.8, 0.6)),
                0.48 * dot(p, vec2(-0.6, 0.8)),
                0.78 * dot(p, vec2(0.96, -0.28))) + WavePhase.xyz;
}

float beachFactor(vec3 shore) {
    return (1.0 - smoothstep(10.0, 25.0, shore.y))
         * (1.0 - smoothstep(4.0, 10.0, shore.x));
}

float shorePhase(vec2 p, float distance) {
    vec3 phase = oceanPhases(p);
    return distance * 0.95 + WavePhase.w + 0.50 * sin(phase.y) + 0.24 * sin(phase.z);
}

float waveHeight(vec2 p, vec3 shore) {
    vec3 s = sin(oceanPhases(p));
    float swell = dot(s, vec3(0.60, 0.27, 0.13));
    float beach = beachFactor(shore);
    float breaker = sin(shorePhase(p, shore.y));
    breaker = 0.72 * breaker + 0.28 * pow(max(breaker, 0.0), 3.0);
    float height = mix(swell, breaker * 0.85 + swell * 0.15, beach);
    float combined = WaveAmplitude * height + impactField(p).x;
    return combined * smoothstep(0.65, 3.0, shore.y) * shore.z;
}
