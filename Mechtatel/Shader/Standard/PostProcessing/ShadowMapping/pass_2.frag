#version 450
#extension GL_ARB_separate_shader_objects:enable

const int MAX_NUM_SHADOW_MAPS=16;

const int PROJECTION_TYPE_ORTHOGRAPHIC=0;
const int PROJECTION_TYPE_PERSPECTIVE=1;

struct ShadowMappingLightInfo{
    int projectionType;
    vec3 lightDirection;
    vec3 attenuations;
    float biasCoefficient;
    float maxBias;
};
layout(set=0,binding=0) uniform ShadowMappingLightInfosUBO{
    ShadowMappingLightInfo smlInfos[MAX_NUM_SHADOW_MAPS];
};
layout(set=1,binding=0) uniform texture2D albedoTexture;
layout(set=1,binding=1) uniform texture2D depthTexture;
layout(set=1,binding=2) uniform texture2D positionTexture;
layout(set=1,binding=3) uniform texture2D normalTexture;
layout(set=1,binding=4) uniform texture2D shadowCoordsTextures[MAX_NUM_SHADOW_MAPS];
layout(set=1,binding=5) uniform texture2D shadowDepthTextures[MAX_NUM_SHADOW_MAPS];
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    vec4 albedo=texture(sampler2D(albedoTexture,textureSampler),fragTexCoords);
    float depth=texture(sampler2D(depthTexture,textureSampler),fragTexCoords).r;
    vec3 position=texture(sampler2D(positionTexture,textureSampler),fragTexCoords).rgb;
    vec3 normal=texture(sampler2D(normalTexture,textureSampler),fragTexCoords).rgb;

    vec3 shadowFactors=vec3(1.0);

    for(int i=0;i<smInfo.numShadowMaps;i++){
        float cosTh=abs(dot(smlInfos[i].lightDirection,normal));
        float bias=smlInfos[i].biasCoefficient*tan(acos(cosTh));
        bias=clamp(bias,0.0,smlInfos[i].maxBias);

        vec3 shadowCoords=texture(sampler2D(shadowCoordsTextures[i],textureSampler),fragTexCoords);
        float shadowDepth=texture(sampler2D(shadowDepthTextures[i],textureSampler),shadowCoords.xy).r;

        if(smlInfos[i].projectionType==PROJECTION_TYPE_ORTHOGRAPHIC){
            if(shadowDepth<shadowCoords.z-bias){
                shadowFactors*=smlInfos[i].attenuations;
            }
        }else if(smlInfos[i].projectionType==PROJECTION_TYPE_PERSPECTIVE){
            if(shadowDepth<(shadowCoords.z-bias)/shadowCoords.w){
                shadowFactors*=smlInfos[i].attenuations;
            }
        }
    }

    outColor=vec4(shadowFactors*albedo.rgb,albedo.a);
}
