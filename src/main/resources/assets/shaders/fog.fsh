#version 330 core

precision lowp float;

in vec2 worldPos;
out vec4 fragColor;

uniform float time;
uniform vec2 playerPos;
uniform vec4 shaderColor;


float smokeScale;    #constant<default = 0.5>
float smokeSpeed;    #constant<default = 0.15>
float smokeDensity;  #constant<default = 1.2>
float edgeSoftness;  #constant<default = 3.0>

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i + vec2(0.0, 0.0)), hash(i + vec2(1.0, 0.0)), u.x),
            mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
    for (int i = 0; i < 4; ++i) {
        v += a * noise(p);
        p = rot * p * 2.0;
        a *= 0.5;
    }
    return v;
}

void main() {
    vec2 uv = (worldPos + playerPos) * smokeScale;

    uv.y -= time * smokeSpeed;
    uv.x += sin(time * 0.5 + uv.y) * 0.2;

    float n = fbm(uv);

    float alpha = n * smokeDensity * shaderColor.a;

    if (alpha < 0.05)
        discard;

    fragColor = vec4(shaderColor.rgb, alpha);
}