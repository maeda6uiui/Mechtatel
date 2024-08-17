#version 450
#extension GL_ARB_separate_shader_objects:enable

const int TEXTURE_OPERATION_ADD=0;
const int TEXTURE_OPERATION_SUB=1;
const int TEXTURE_OPERATION_MUL=2;
const int TEXTURE_OPERATION_DIV=3;

const int MAX_NUM_TEXTURES=8;

layout(set=0,binding=0) uniform TextureOperationParametersUBO{
    vec4 factors[MAX_NUM_TEXTURES];
    int operationType;
    int numTextures;
}parameters;
layout(set=1,binding=0) uniform texture2D colorTextures[MAX_NUM_TEXTURES];
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    vec4 curColor=vec4(0.0);

    for(int i=0;i<min(parameters.numTextures,MAX_NUM_TEXTURES);i++){
        vec4 thisColor=texture(sampler2D(colorTextures[i],textureSampler),fragTexCoords);
        if(i==0){
            curColor=thisColor;
            continue;
        }
        
        int operationType=parameters.operationType;
        if(operationType==TEXTURE_OPERATION_ADD){
            curColor+=thisColor;
        }else if(operationType==TEXTURE_OPERATION_SUB){
            curColor-=thisColor;
        }else if(operationType==TEXTURE_OPERATION_MUL){
            curColor*=thisColor;
        }else if(operationType==TEXTURE_OPERATION_DIV){
            curColor/=thisColor;
        }
    }

    outColor=curColor;
}
