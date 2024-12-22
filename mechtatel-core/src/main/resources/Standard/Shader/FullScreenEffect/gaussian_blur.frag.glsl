#version 450
#extension GL_ARB_separate_shader_objects:enable

const int MAX_NUM_WEIGHTS=64;

layout(set=0,binding=0) uniform GaussianBlurInfoUBO{
    ivec2 textureSize;
    int numWeights;
    float weights[MAX_NUM_WEIGHTS];
}blurInfo;
layout(set=1,binding=0) uniform texture2D colorTexture;
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    vec4 result=vec4(0.0);
    vec2 texelSize=1.0/blurInfo.textureSize;
    int upperBound=min(blurInfo.numWeights,MAX_NUM_WEIGHTS);

    vec2 cur_uv;

    //Horizontal
    for(int i=1;i<upperBound;i++){
        cur_uv=fragTexCoords+vec2(texelSize.x*(-i),0.0);
        result+=texture(sampler2D(colorTexture,textureSampler),cur_uv)*blurInfo.weights[i];
    }
    result+=texture(sampler2D(colorTexture,textureSampler),fragTexCoords)*blurInfo.weights[0];
    for(int i=1;i<upperBound;i++){
        cur_uv=fragTexCoords+vec2(texelSize.x*i,0.0);
        result+=texture(sampler2D(colorTexture,textureSampler),cur_uv)*blurInfo.weights[i];
    }

    //Vertical
    for(int i=1;i<upperBound;i++){
        cur_uv=fragTexCoords+vec2(0.0,texelSize.y*(-i));
        result+=texture(sampler2D(colorTexture,textureSampler),cur_uv)*blurInfo.weights[i];
    }
    result+=texture(sampler2D(colorTexture,textureSampler),fragTexCoords)*blurInfo.weights[0];
    for(int i=1;i<upperBound;i++){
        cur_uv=fragTexCoords+vec2(0.0,texelSize.y*i);
        result+=texture(sampler2D(colorTexture,textureSampler),cur_uv)*blurInfo.weights[i];
    }

    outColor=result;
}
