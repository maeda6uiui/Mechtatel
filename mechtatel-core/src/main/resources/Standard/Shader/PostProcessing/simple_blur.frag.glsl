#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(set=0,binding=0) uniform BlurInfoUBO{
    ivec2 textureSize;
    int blurSize;
    int stride;
}blurInfo;
layout(set=1,binding=0) uniform texture2D albedoTexture;
layout(set=1,binding=1) uniform texture2D depthTexture;
layout(set=1,binding=2) uniform texture2D positionTexture;
layout(set=1,binding=3) uniform texture2D normalTexture;
layout(set=1,binding=4) uniform texture2D stencilTexture;
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    float stencil=texture(sampler2D(stencilTexture,textureSampler),fragTexCoords).r;
    vec2 texelSize=1.0/blurInfo.textureSize;

    vec4 sum=vec4(0.0);
    int count=0;
    for(int x=0;x<blurInfo.blurSize;x+=blurInfo.stride){
        for(int y=0;y<blurInfo.blurSize;y+=blurInfo.stride){
            sum+=texture(sampler2D(albedoTexture,textureSampler),fragTexCoords+vec2(texelSize.x*x,texelSize.y*y));
            count+=1;
        }
    }

    outColor=sum/count;
    outColor=outColor*(1.0-stencil);
}
