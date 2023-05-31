#version 450
#extension GL_ARB_separate_shader_objects:enable

const int TEXTURE_OPERATION_ADD=0;
const int TEXTURE_OPERATION_SUB=1;
const int TEXTURE_OPERATION_MUL=2;
const int TEXTURE_OPERATION_DIV=3;
const int TEXTURE_OPERATION_MERGE_BY_DEPTH=4;

layout(set=0,binding=0) uniform TextureOperationParametersUBO{
    vec4 firstTextureFactor;
    vec4 secondTextureFactor;
    int operationType;
    float firstTextureFixedDepth;
    float secondTextureFixedDepth;
}parameters;
layout(set=1,binding=0) uniform texture2D colorTextures[2];
layout(set=1,binding=1) uniform texture2D depthTextures[2];
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    vec4 color_1=texture(sampler2D(colorTextures[0],textureSampler),fragTexCoords);
    vec4 color_2=texture(sampler2D(colorTextures[1],textureSampler),fragTexCoords);

    if(parameters.operationType==TEXTURE_OPERATION_ADD){
        outColor=color_1*parameters.firstTextureFactor+color_2*parameters.secondTextureFactor;
    }else if(parameters.operationType==TEXTURE_OPERATION_SUB){
        outColor=color_1*parameters.firstTextureFactor-color_2*parameters.secondTextureFactor;
    }else if(parameters.operationType==TEXTURE_OPERATION_MUL){
        outColor=(color_1*parameters.firstTextureFactor)*(color_2*parameters.secondTextureFactor);
    }else if(parameters.operationType==TEXTURE_OPERATION_DIV){
        outColor=(color_1*parameters.firstTextureFactor)/(color_2*parameters.secondTextureFactor);
    }else if(parameters.operationType==TEXTURE_OPERATION_MERGE_BY_DEPTH){
        float depth_1=texture(sampler2D(depthTextures[0],textureSampler),fragTexCoords).r;
        float depth_2=texture(sampler2D(depthTextures[1],textureSampler),fragTexCoords).r;

        if(parameters.firstTextureFixedDepth>=0.0&&parameters.firstTextureFixedDepth<=1.0){
            depth_1=parameters.firstTextureFixedDepth;
        }
        if(parameters.secondTextureFixedDepth>=0.0&&parameters.secondTextureFixedDepth<=1.0){
            depth_2=parameters.secondTextureFixedDepth;
        }

        if(depth_1<depth_2){
            outColor=color_1*parameters.firstTextureFactor;
        }else{
            outColor=color_2*parameters.secondTextureFactor;
        }
    }else{
        outColor=color_1;
    }
}
