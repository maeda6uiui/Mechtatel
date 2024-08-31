#version 450
#extension GL_ARB_separate_shader_objects:enable

const int MAX_NUM_WEIGHTS=4;
const int MAX_NUM_BONES=150;

layout(set=0,binding=0) uniform CameraUBO{
    mat4 view;
    mat4 proj;
    
    vec3 eye;
    vec3 center;
}camera;
layout(set=0,binding=1) uniform AnimationUBO{
    mat4 boneMatrices[MAX_NUM_BONES];
}animation;
layout(push_constant) uniform VertPC{
    mat4 model;
    int is2DDrawing;
}pc;

layout(location=0) in vec3 inPosition;
layout(location=1) in vec4 inColor;
layout(location=2) in vec2 inTexCoords;
layout(location=3) in vec3 inNormal;
layout(location=4) in vec4 inBoneWeights;
layout(location=5) in ivec4 inBoneIndices;

layout(location=0) out vec4 fragColor;
layout(location=1) out vec2 fragTexCoords;

void process3DDrawing(){
    vec4 initPos=vec4(0.0,0.0,0.0,0.0);

    int count=0;
    for(int i=0;i<min(MAX_NUM_WEIGHTS,4);i++){
        float weight=inBoneWeights[i];
        int boneIndex=inBoneIndices[i];

        if(boneIndex>=0){
            vec4 tmpPos=animation.boneMatrices[boneIndex]*vec4(inPosition,1.0);
            initPos+=weight*tmpPos;

            count++;
        }
    }

    if(count==0){
        initPos=vec4(inPosition,1.0);
    }

    gl_Position=camera.proj*camera.view*pc.model*vec4(initPos.xyz,1.0);
}

void process2DDrawing(){
    gl_Position=pc.model*vec4(inPosition,1.0);
}

void main(){
    if(pc.is2DDrawing==0){
        process3DDrawing();
    }else{
        process2DDrawing();
    }

    fragColor=inColor;
    fragTexCoords=inTexCoords;
}
