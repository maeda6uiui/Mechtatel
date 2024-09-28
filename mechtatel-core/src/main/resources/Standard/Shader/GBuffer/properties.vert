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

layout(location=0) out vec3 fragPosition;
layout(location=1) out vec3 fragNormal;

void process3DDrawing(){
    vec4 accumPos=vec4(0.0,0.0,0.0,0.0);
    vec4 accumNormal=vec4(0.0,0.0,0.0,0.0);

    int count=0;
    for(int i=0;i<min(MAX_NUM_WEIGHTS,4);i++){
        float weight=inBoneWeights[i];
        int boneIndex=inBoneIndices[i];
        
        if(boneIndex>=0){
            vec4 tmpPos=animation.boneMatrices[boneIndex]*vec4(inPosition,1.0);
            accumPos+=weight*tmpPos;

            vec4 tmpNormal=animation.boneMatrices[boneIndex]*vec4(inNormal,0.0);
            accumNormal+=weight*tmpNormal;

            count++;
        }
    }

    if(count==0){
        accumPos=vec4(inPosition,1.0);
        accumNormal=vec4(inNormal,0.0);
    }

    gl_Position=camera.proj*camera.view*pc.model*vec4(accumPos.xyz,1.0);

    fragPosition=(pc.model*vec4(accumPos.xyz,1.0)).xyz;
    fragNormal=(pc.model*vec4(accumNormal.xyz,0.0)).xyz;
}

void process2DDrawing(){
    gl_Position=pc.model*vec4(inPosition,1.0);

    fragPosition=(pc.model*vec4(inPosition,1.0)).xyz;
    fragNormal=inNormal;
}

void main(){
    if(pc.is2DDrawing==0){
        process3DDrawing();
    }else{
        process2DDrawing();
    }
}
