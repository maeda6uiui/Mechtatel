#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(set=0,binding=0) uniform texture2D depthTexture;
layout(set=1,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    float depth=texture(sampler2D(depthTexture,textureSampler),fragTexCoords).r;
    outColor=vec4(depth);
}
