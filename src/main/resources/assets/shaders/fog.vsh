#version 330 core

in vec4 position;

#include<matrices>

out vec2 worldPos;

void main() {
    worldPos = position.xy;
    gl_Position = projection * view * position;
}