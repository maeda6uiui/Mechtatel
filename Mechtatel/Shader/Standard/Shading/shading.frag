#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(set=0,binding=0) uniform CameraUBO{
    mat4 view;
    mat4 proj;
    
    vec3 eye;
    vec3 center;
}camera;
layout(set=0,binding=1) uniform ParallelLightUBO{
    vec3 direction;
    vec3 ambientColor;
    vec3 diffuseColor;
    vec3 specularColor;
    vec3 ambientClampMin;
    vec3 ambientClampMax;
    vec3 diffuseClampMin;
    vec3 diffuseClampMax;
    vec3 specularClampMin;
    vec3 specularClampMax;
    float specularPowY;
}light;
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
    vec3 halfLE=-normalize(cameraDirection+light.direction);

    float diffuseCoefficient=clamp(dot(normal,-light.direction),0.0,1.0);
    float specularCoefficient=pow(clamp(dot(normal,halfLE),0.0,1.0),light.specularPowY);

    vec3 ambientColor=clamp(light.ambientColor,light.ambientClampMin,light.ambientClampMax);
    vec3 diffuseColor=clamp(light.diffuseColor*diffuseCoefficient,light.diffuseClampMin,light.diffuseClampMax);
    vec3 specularColor=clamp(light.specularColor*specularCoefficient,light.specularClampMin,light.specularClampMax);
    vec3 postLightingColor=albedo.rgb*(ambientColor+diffuseColor+specularColor);

    outColor=vec4(postLightingColor,albedo.a);
}
