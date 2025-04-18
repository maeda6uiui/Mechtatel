#version 450
#extension GL_ARB_separate_shader_objects:enable

const int MAX_NUM_SCENES=8;

layout(set=0,binding=0) uniform texture2D albedoTextures[MAX_NUM_SCENES];
layout(set=0,binding=1) uniform texture2D depthTextures[MAX_NUM_SCENES];
layout(set=0,binding=2) uniform texture2D positionTextures[MAX_NUM_SCENES];
layout(set=0,binding=3) uniform texture2D normalTextures[MAX_NUM_SCENES];
layout(set=0,binding=4) uniform texture2D stencilTextures[MAX_NUM_SCENES];
layout(set=1,binding=0) uniform sampler textureSampler;
layout(set=2,binding=0) uniform MergeScenesInfoUBO{
    int numScenes;
}mergeScenesInfo;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outAlbedo;
layout(location=1) out vec4 outDepth;
layout(location=2) out vec4 outPosition;
layout(location=3) out vec4 outNormal;
layout(location=4) out vec4 outStencil;

void main(){
    vec4 curAlbedo=vec4(0.0);
    float curDepth=1.0;
    vec3 curPosition=vec3(0.0);
    vec3 curNormal=vec3(0.0);
    float curStencil=1.0;

    for(int i=0;i<min(mergeScenesInfo.numScenes,MAX_NUM_SCENES);i++){
        vec4 albedo=texture(sampler2D(albedoTextures[i],textureSampler),fragTexCoords);
        float depth=texture(sampler2D(depthTextures[i],textureSampler),fragTexCoords).r;
        vec3 position=texture(sampler2D(positionTextures[i],textureSampler),fragTexCoords).rgb;
        vec3 normal=texture(sampler2D(normalTextures[i],textureSampler),fragTexCoords).rgb;
        float stencil=texture(sampler2D(stencilTextures[i],textureSampler),fragTexCoords).r;

        if(i==0){
            curAlbedo=albedo;
            curDepth=depth;
            curPosition=position;
            curNormal=normal;
            curStencil=stencil;
        }else{
            //Overwrite the values if the depth of this pixel 
            //is shallower than the current depth retained
            curAlbedo=(depth<curDepth)?albedo:curAlbedo;
            curPosition=(depth<curDepth)?position:curPosition;
            curNormal=(depth<curDepth)?normal:curNormal;
            curStencil=(depth<curDepth)?stencil:curStencil;

            //Update current depth
            curDepth=(depth<curDepth)?depth:curDepth;
        }
    }

    outAlbedo=curAlbedo;
    outDepth=vec4(vec3(curDepth),1.0);
    outPosition=vec4(curPosition,1.0);
    outNormal=vec4(curNormal,1.0);
    outStencil=vec4(vec3(curStencil),1.0);
}
