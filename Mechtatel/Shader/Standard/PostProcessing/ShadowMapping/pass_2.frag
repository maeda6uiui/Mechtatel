#version 450
#extension GL_ARB_separate_shader_objects:enable

const int MAX_NUM_SHADOW_MAPS=16;

const int PROJECTION_TYPE_ORTHOGRAPHIC=0;
const int PROJECTION_TYPE_PERSPECTIVE=1;

const int OUTPUT_MODE_SHADOW_MAPPING=0;
const int OUTPUT_MODE_SHADOW_FACTORS=1;
const int OUTPUT_MODE_DEPTH_IMAGE=2;

layout(set=0,binding=0) uniform Pass2InfoUBO{
    int numShadowMaps;
    float biasCoefficient;
    float maxBias;
    float normalOffset;
    int outputMode;
    int outputDepthImageIndex;
}passInfo;
struct ShadowInfo{
    mat4 lightView;
    mat4 lightProj;
    vec3 lightDirection;
    vec3 attenuations;
    int projectionType;
};
layout(set=0,binding=1) uniform ShadowInfosUBO{
    ShadowInfo shadowInfos[MAX_NUM_SHADOW_MAPS];
};
layout(set=1,binding=0) uniform texture2D albedoTexture;
layout(set=1,binding=1) uniform texture2D depthTexture;
layout(set=1,binding=2) uniform texture2D positionTexture;
layout(set=1,binding=3) uniform texture2D normalTexture;
layout(set=1,binding=4) uniform texture2D shadowDepthTextures[MAX_NUM_SHADOW_MAPS];
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    vec4 albedo=texture(sampler2D(albedoTexture,textureSampler),fragTexCoords);
    float depth=texture(sampler2D(depthTexture,textureSampler),fragTexCoords).r;
    vec3 position=texture(sampler2D(positionTexture,textureSampler),fragTexCoords).rgb;
    vec3 normal=texture(sampler2D(normalTexture,textureSampler),fragTexCoords).rgb;

    mat4 biasMat;
    biasMat[0]=vec4(0.5,0.0,0.0,0.0);
    biasMat[1]=vec4(0.0,0.5,0.0,0.0);
    biasMat[2]=vec4(0.0,0.0,1.0,0.0);
    biasMat[3]=vec4(0.5,0.5,0.0,1.0);

    vec3 shadowFactors=vec3(1.0);

    for(int i=0;i<passInfo.numShadowMaps;i++){
        float cosTh=abs(dot(shadowInfos[i].lightDirection,normal));
        float bias=passInfo.biasCoefficient*tan(acos(cosTh));
        bias=clamp(bias,0.0,passInfo.maxBias);

        vec4 shadowCoords=biasMat*shadowInfos[i].lightProj*shadowInfos[i].lightView*vec4(position+normal*passInfo.normalOffset,1.0);

        if(shadowInfos[i].projectionType==PROJECTION_TYPE_ORTHOGRAPHIC){
            float shadowDepth=texture(sampler2D(shadowDepthTextures[i],textureSampler),shadowCoords.xy).r;
            if(shadowDepth<shadowCoords.z-bias){
                shadowFactors*=shadowInfos[i].attenuations;
            }
        }else if(shadowInfos[i].projectionType==PROJECTION_TYPE_PERSPECTIVE){
            float shadowDepth=texture(sampler2D(shadowDepthTextures[i],textureSampler),shadowCoords.xy/shadowCoords.w).r;
            if(shadowDepth<(shadowCoords.z-bias)/shadowCoords.w){
                shadowFactors*=shadowInfos[i].attenuations;
            }
        }
    }

    if(passInfo.outputMode==OUTPUT_MODE_SHADOW_MAPPING){
        outColor=vec4(shadowFactors*albedo.rgb,albedo.a);
    }else if(passInfo.outputMode==OUTPUT_MODE_SHADOW_FACTORS){
        outColor=vec4(shadowFactors,1.0);
    }else if(passInfo.outputMode==OUTPUT_MODE_DEPTH_IMAGE){
        float shadowDepth=texture(sampler2D(shadowDepthTextures[passInfo.outputDepthImageIndex],textureSampler),fragTexCoords).r;
        outColor=vec4(vec3(shadowDepth),1.0);
    }else{
        outColor=vec4(1.0);
    }
}
