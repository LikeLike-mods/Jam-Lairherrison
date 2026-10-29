uniform vec4 Impact0;
uniform vec4 Impact1;
uniform vec4 Impact2;
uniform vec4 Impact3;
uniform vec4 Impact4;
uniform vec4 Impact5;
uniform vec4 Impact6;
uniform vec4 Impact7;
uniform vec4 ImpactRadii0;
uniform vec4 ImpactRadii1;

vec2 impactPulse(vec2 p, vec4 impact, float initialRadius) {
    float age = impact.z;
    if (impact.w <= 0.0 || age <= 0.0 || age >= 10.0) return vec2(0.0);
    float front = initialRadius + age * 5.0;
    float offset = length(p - impact.xy) - front;
    float width = 1.8 + age * 0.12;
    float envelope = exp(-0.5 * (offset / width) * (offset / width));
    float life = smoothstep(0.0, 0.18, age) * (1.0 - smoothstep(5.0, 10.0, age));
    float spread = sqrt(max(initialRadius, 0.5) / max(front, 0.5));
    float energy = impact.w * life * spread * exp(-0.12 * age) * envelope;
    float crest = cos(offset * 1.35);
    return vec2(0.55 * energy * crest, 0.95 * energy * smoothstep(0.25, 0.85, crest));
}

void accumulateImpact(inout vec2 field, vec2 wave) {
    field.x += wave.x;
    field.y = max(field.y, wave.y);
}

vec2 impactField(vec2 p) {
    vec2 field = vec2(0.0);
    accumulateImpact(field, impactPulse(p, Impact0, ImpactRadii0.x));
    accumulateImpact(field, impactPulse(p, Impact1, ImpactRadii0.y));
    accumulateImpact(field, impactPulse(p, Impact2, ImpactRadii0.z));
    accumulateImpact(field, impactPulse(p, Impact3, ImpactRadii0.w));
    accumulateImpact(field, impactPulse(p, Impact4, ImpactRadii1.x));
    accumulateImpact(field, impactPulse(p, Impact5, ImpactRadii1.y));
    accumulateImpact(field, impactPulse(p, Impact6, ImpactRadii1.z));
    accumulateImpact(field, impactPulse(p, Impact7, ImpactRadii1.w));
    return vec2(clamp(field.x, -0.85, 0.85), clamp(field.y, 0.0, 1.0));
}
