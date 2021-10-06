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
struct Spotlight{
    vec3 position;
    vec3 direction;
    vec3 diffuseColor;
    vec3 specularColor;
    vec3 diffuseClampMin;
    vec3 diffuseClampMax;
    vec3 specularClampMin;
    vec3 specularClampMax;
    float k0;
    float k1;
    float k2;
    float theta;
    float phi;
    float falloff;
    float specularPowY;
};
layout(set=0,binding=2) uniform LightUBOs{
    Spotlight lights[MAX_NUM_LIGHTS];
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

    vec3 cameraDirection=normalize(camera.center-camera.eye);

    vec3 lightingColor=lightingInfo.ambientColor;

    for(int i=0;i<lightingInfo.numLights;i++){
        vec3 r=position-lights[i].position;
        float lenR=length(r);
        float attenuation=1.0/(lights[i].k0+lights[i].k1*pow(lenR,1.0)+lights[i].k2*pow(lenR,2.0));

        r=normalize(r);

        float cosAlpha=dot(r,lights[i].direction);
        float cosHalfTheta=cos(lights[i].theta/2.0);
        float cosHalfPhi=cos(lights[i].phi/2.0);

        vec3 spotlightColor;
        if(cosAlpha<=cosHalfPhi){
            spotlightColor=vec3(0.0);
        }else{
            if(cosAlpha<=cosHalfTheta){
                attenuation*=pow((cosAlpha-cosHalfPhi)/(cosHalfTheta-cosHalfPhi),lights[i].falloff);
            }

            vec3 halfLE=-normalize(cameraDirection+lights[i].direction);
            float specularCoefficient=pow(clamp(dot(normal,halfLE),0.0,1.0),lights[i].specularPowY);

            vec3 diffuseColor=lights[i].diffuseColor*attenuation;
            vec3 specularColor=lights[i].specularColor*attenuation;

            diffuseColor=clamp(diffuseColor,lights[i].diffuseClampMin,lights[i].diffuseClampMax);
            specularColor=clamp(specularColor,lights[i].specularClampMin,lights[i].specularClampMax);

            spotlightColor=diffuseColor+specularColor;
        }

        lightingColor+=spotlightColor;
    }

    lightingColor=clamp(lightingColor,lightingInfo.lightingClampMin,lightingInfo.lightingClampMax);
    vec3 postLightingColor=albedo.rgb*lightingColor;

    outColor=vec4(postLightingColor,albedo.a);
}
