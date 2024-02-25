#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(location=0) in vec3 fragPosition;
layout(location=1) in vec4 fragColor;
layout(location=2) in vec3 fragNormal;

layout(location=0) out vec4 outAlbedo;
layout(location=1) out vec3 outPosition;
layout(location=2) out vec3 outNormal;
layout(location=3) out float outStencil;

void main(){
    outAlbedo=fragColor;
    outPosition=fragPosition;
    outNormal=fragNormal;
    outStencil=0.0;
}
