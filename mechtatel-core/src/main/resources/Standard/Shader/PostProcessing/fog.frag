#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(set=0,binding=0) uniform CameraUBO{
    mat4 view;
    mat4 proj;
    
    vec3 eye;
    vec3 center;
}camera;
layout(set=0,binding=1) uniform FogUBO{
    vec3 color;
    float start;
    float end;
}fog;
layout(set=1,binding=0) uniform texture2D albedoTexture;
layout(set=1,binding=1) uniform texture2D depthTexture;
layout(set=1,binding=2) uniform texture2D positionTexture;
layout(set=1,binding=3) uniform texture2D normalTexture;
layout(set=1,binding=4) uniform texture2D stencilTexture;
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    vec4 albedo=texture(sampler2D(albedoTexture,textureSampler),fragTexCoords);
    vec3 position=texture(sampler2D(positionTexture,textureSampler),fragTexCoords).rgb;

    float linearPos=length(camera.eye-position);
    float fogFactor=clamp((fog.end-linearPos)/(fog.end-fog.start),0.0,1.0);

    vec4 fogColor=vec4(fog.color,1.0);
    outColor=mix(fogColor,albedo,fogFactor);
}
