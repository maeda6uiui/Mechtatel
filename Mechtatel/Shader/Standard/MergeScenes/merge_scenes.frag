#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(set=1,binding=0) uniform texture2D albedoTextures[2];
layout(set=1,binding=1) uniform texture2D depthTextures[2];
layout(set=1,binding=2) uniform texture2D positionTexture[2];
layout(set=1,binding=3) uniform texture2D normalTexture[2];
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outAlbedo;
layout(location=1) out float outDepth;
layout(location=2) out vec3 outPosition;
layout(location=3) out vec3 outNormal;

void main(){
    vec4 albedoA=texture(sampler2D(albedoTextures[0],textureSampler),fragTexCoords);
    float depthA=texture(sampler2D(depthTextures[0],textureSampler),fragTexCoords).r;
    vec3 positionA=texture(sampler2D(positionTextures[0],textureSampler),fragTexCoords).rgb;
    vec3 normalA=texture(sampler2D(normalTextures[0],textureSampler),fragTexCoords).rgb;

    vec4 albedoB=texture(sampler2D(albedoTextures[1],textureSampler),fragTexCoords);
    float depthB=texture(sampler2D(depthTextures[1],textureSampler),fragTexCoords).r;
    vec3 positionB=texture(sampler2D(positionTextures[1],textureSampler),fragTexCoords).rgb;
    vec3 normalB=texture(sampler2D(normalTextures[1],textureSampler),fragTexCoords).rgb;

    outAlbedo=(depthA<depthB)?albedoA:albedoB;
    outDepth=(depthA<depthB)?depthA:depthB;
    outPosition=(depthA<depthB)?positionA:positionB;
    outNormal=(depthA<depthB)?normalA:normalB;
}
