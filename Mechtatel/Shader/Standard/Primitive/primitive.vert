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

layout(location=0) out vec4 fragColor;

void main(){
    gl_Position=camera.proj*camera.view*pc.model*vec4(inPosition,1.0);
    fragColor=inColor;
}
