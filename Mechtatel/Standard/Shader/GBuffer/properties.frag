#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(location=0) in vec3 fragPosition;
layout(location=1) in vec3 fragNormal;

layout(location=0) out vec3 outPosition;
layout(location=1) out vec3 outNormal;

void main(){
    outPosition=fragPosition;
    outNormal=fragNormal;
}
