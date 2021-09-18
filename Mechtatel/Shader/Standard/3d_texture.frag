#version 450
#extension GL_ARB_separate_shader_objects:enable

const int MAX_NUM_TEXTURES=128;
const int SIZEOF_FLOAT=4;

layout(binding=1) uniform sampler textureSampler;
layout(binding=2) uniform texture2D textures[MAX_NUM_TEXTURES];
layout(push_constant) uniform FragPC{
    layout(offset=1*16*SIZEOF_FLOAT) int textureIndex;
}pc;

layout(location=0) in vec4 fragColor;
layout(location=1) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    outColor=texture(sampler2D(textures[pc.textureIndex],textureSampler),fragTexCoords);
}
