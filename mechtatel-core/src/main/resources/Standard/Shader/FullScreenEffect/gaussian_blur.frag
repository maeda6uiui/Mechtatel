#version 450
#extension GL_ARB_separate_shader_objects:enable

const int MAX_NUM_WEIGHTS=64;

layout(set=0,binding=0) uniform GaussianBlurInfoUBO{
    ivec2 texture_size;
    int numWeights;
    float weights[MAX_NUM_WEIGHTS];
}blurInfo;
layout(set=1,binding=0) uniform texture2D colorTexture;
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    vec4 result=vec4(0.0);
    vec2 texel_size=1.0/blurInfo.texture_size;

    vec2 cur_uv;

    //Horizontal
    for(int i=1;i<blurInfo.numWeights;i++){
        cur_uv=fragTexCoords+vec2(texel_size.x*(-i),0.0);
        result+=texture(sampler2D(colorTexture,textureSampler),cur_uv)*blurInfo.weights[i];
    }
    result+=texture(sampler2D(colorTexture,textureSampler),fragTexCoords)*blurInfo.weights[0];
    for(int i=1;i<blurInfo.numWeights;i++){
        cur_uv=fragTexCoords+vec2(texel_size.x*i,0.0);
        result+=texture(sampler2D(colorTexture,textureSampler),cur_uv)*blurInfo.weights[i];
    }

    //Vertical
    for(int i=1;i<blurInfo.numWeights;i++){
        cur_uv=fragTexCoords+vec2(0.0,texel_size.y*(-i));
        result+=texture(sampler2D(colorTexture,textureSampler),cur_uv)*blurInfo.weights[i];
    }
    result+=texture(sampler2D(colorTexture,textureSampler),fragTexCoords)*blurInfo.weights[0];
    for(int i=1;i<blurInfo.numWeights;i++){
        cur_uv=fragTexCoords+vec2(0.0,texel_size.y*i);
        result+=texture(sampler2D(colorTexture,textureSampler),cur_uv)*blurInfo.weights[i];
    }

    outColor=result;
}
