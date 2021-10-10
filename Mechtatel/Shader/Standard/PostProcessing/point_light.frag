#version 450
#extension GL_ARB_separate_shader_objects:enable

const int MAX_NUM_LIGHTS=64;

layout(set=0,binding=0) uniform CameraUBO{
    mat4 view;
    mat4 proj;
    
    vec3 eye;
    vec3 center;
}camera;
layout(set=0,binding=1) uniform LightingInfoUBO{
    vec3 ambientColor;
    vec3 lightingClampMin;
    vec3 lightingClampMax;
    int numLights;
}lightingInfo;
struct PointLight{
    vec3 position;
    vec3 diffuseColor;
    vec3 diffuseClampMin;
    vec3 diffuseClampMax;
    float k0;
    float k1;
    float k2;
};
layout(set=0,binding=2) uniform LightsUBO{
    PointLight lights[MAX_NUM_LIGHTS];
};
layout(set=1,binding=0) uniform texture2D albedoTexture;
layout(set=1,binding=1) uniform texture2D depthTexture;
layout(set=1,binding=2) uniform texture2D positionTexture;
layout(set=1,binding=3) uniform texture2D normalTexture;
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    vec4 albedo=texture(sampler2D(albedoTexture,textureSampler),fragTexCoords);
    float depth=texture(sampler2D(depthTexture,textureSampler),fragTexCoords).r;
    vec3 position=texture(sampler2D(positionTexture,textureSampler),fragTexCoords).rgb;
    vec3 normal=texture(sampler2D(normalTexture,textureSampler),fragTexCoords).rgb;

    vec3 lightingColor=lightingInfo.ambientColor;

    for(int i=0;i<lightingInfo.numLights;i++){
        vec3 r=position-lights[i].position;
        float lenR=length(r);
        float attenuation=1.0/(lights[i].k0+lights[i].k1*pow(lenR,1.0)+lights[i].k2*pow(lenR,2.0));

        vec3 diffuseColor=lights[i].diffuseColor*attenuation;
        diffuseColor=clamp(diffuseColor,lights[i].diffuseClampMin,lights[i].diffuseClampMax);

        lightingColor+=diffuseColor;
    }

    lightingColor=clamp(lightingColor,lightingInfo.lightingClampMin,lightingInfo.lightingClampMax);
    vec3 postLightingColor=albedo.rgb*lightingColor;

    outColor=vec4(postLightingColor,albedo.a);
}
