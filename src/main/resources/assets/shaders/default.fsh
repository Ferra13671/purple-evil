#version 330 core

precision lowp float;

#ifdef VERTEX_COLOR
in vec4 vertexColor;
#endif

#ifdef VERTEX_UV
in vec2 texPos;
#endif



#ifdef VERTEX_UV
uniform sampler2D u_Texture;
#endif
uniform vec4 shaderColor;


out vec4 fragColor;

void main() {
    fragColor = shaderColor;

    #ifdef VERTEX_COLOR
    fragColor *= vertexColor;
    #endif

    #ifdef VERTEX_UV
    fragColor *= texture(u_Texture, texPos);
    #endif
}