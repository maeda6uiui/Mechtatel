#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(set=0,binding=0) uniform BlurInfoUBO{
    int textureWidth;
    int textureHeight;
    int stride;
}blurInfo;
layout(set=1,binding=0) uniform texture2D albedoTexture;
layout(set=1,binding=1) uniform texture2D depthTexture;
layout(set=1,binding=2) uniform texture2D positionTexture;
layout(set=1,binding=3) uniform texture2D normalTexture;
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    float texelSizeH=1.0/blurInfo.textureWidth;
    float texelSizeV=1.0/blurInfo.textureHeight;

    vec4 sum=vec4(0.0);
    for(int i=0;i<blurInfo.stride;i++){
        sum+=texture(sampler2D(albedoTexture,textureSampler),fragTexCoords+vec2(texelSizeH*i,0.0));
    }
    for(int i=0;i<blurInfo.stride;i++){
        sum+=texture(sampler2D(albedoTexture,textureSampler),fragTexCoords+vec2(0.0,texelSizeV*i));
    }

    outColor=sum/pow(blurInfo.stride,2.0);
}
