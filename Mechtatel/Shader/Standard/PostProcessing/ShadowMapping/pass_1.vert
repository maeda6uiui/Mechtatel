#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(set=0,binding=0) uniform ShadowMatricesUBO{
    mat4 view;
    mat4 proj;
}matrices;
layout(set=0,binding=1) uniform Pass1InfoUBO{
    float normalOffset;
}smInfo;

layout(location=0) in vec3 inPosition;
layout(location=1) in vec4 inColor;
layout(location=2) in vec2 inTexCoords;
layout(location=3) in vec3 inNormal;

layout(location=0) out vec4 fragShadowCoords;

void main(){
    gl_Position=matrices.proj*matrices.view*vec4(inPosition+inNormal*smInfo.normalOffset,1.0);
    fragShadowCoords=gl_Position;
}
