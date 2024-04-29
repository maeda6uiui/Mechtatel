#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(set=0,binding=0) uniform Pass1InfoUBO{
    mat4 lightView;
    mat4 lightProj;
}passInfo;
layout(push_constant) uniform VertPC{
    mat4 model;
}pc;

layout(location=0) in vec3 inPosition;
layout(location=1) in vec4 inColor;
layout(location=2) in vec2 inTexCoords;
layout(location=3) in vec3 inNormal;
layout(location=4) in vec4 inBoneWeights;
layout(location=5) in ivec4 inBoneIndices;

void main(){
    gl_Position=passInfo.lightProj*passInfo.lightView*pc.model*vec4(inPosition,1.0);
}
