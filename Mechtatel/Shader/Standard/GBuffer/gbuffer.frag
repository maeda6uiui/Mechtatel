#version 450
#extension GL_ARB_separate_shader_objects:enable

const int MAX_NUM_TEXTURES=128;
const int SIZEOF_FLOAT=4;

layout(set=1,binding=0) uniform texture2D textures[MAX_NUM_TEXTURES];
layout(set=2,binding=0) uniform sampler textureSampler;
layout(push_constant) uniform FragPC{
    layout(offset=1*16*SIZEOF_FLOAT) int textureIndex;
}pc;

layout(location=0) in vec4 fragColor;
layout(location=1) in vec2 fragTexCoords;
layout(location=2) in vec3 fragPosition;
layout(location=3) in vec3 fragNormal;

layout(location=0) out vec4 outColor;
layout(location=1) out vec3 outPosition;
layout(location=2) out vec3 outNormal;

void main(){
    outColor=texture(sampler2D(textures[pc.textureIndex],textureSampler),fragTexCoords);
    outPosition=fragPosition;
    outNormal=fragNormal;
}