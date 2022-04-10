#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(set=0,binding=0) uniform CameraUBO{
    mat4 view;
    mat4 proj;
    
    vec3 eye;
    vec3 center;
}camera;
layout(push_constant) uniform VertPC{
    mat4 model;
}pc;

layout(location=0) in vec3 inPosition;
layout(location=1) in vec4 inColor;
layout(location=2) in vec2 inTexCoords;
layout(location=3) in vec3 inNormal;

layout(location=0) out vec3 fragPosition;
layout(location=1) out vec3 fragNormal;

void main(){
    gl_Position=camera.proj*camera.view*pc.model*vec4(inPosition,1.0);
    fragPosition=(pc.model*vec4(inPosition,1.0)).xyz;
    fragNormal=inNormal;
}
