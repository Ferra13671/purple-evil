#version 330 core

in vec4 position;

#ifdef VERTEX_COLOR
in vec4 vertexColor_in;
#endif

#ifdef VERTEX_UV
in vec2 texPos_in;
#endif



#include<matrices>



#ifdef VERTEX_COLOR
out vec4 vertexColor;
#endif

#ifdef VERTEX_UV
out vec2 texPos;
#endif

void main() {
    gl_Position = projection * view * position;

    #ifdef VERTEX_COLOR
    vertexColor = vertexColor_in;
    #endif

    #ifdef VERTEX_UV
    texPos = texPos_in;
    #endif
}