#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(set=1,binding=0) uniform texture2D srcTexture;
layout(set=2,binding=0) uniform sampler textureSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    vec4 srcColor=texture(sampler2D(srcTexture,textureSampler),fragTexCoords);

    outColor.r=srcColor.r*0.393+srcColor.g*0.769+srcColor.b*0.189;
    outColor.g=srcColor.r*0.349+srcColor.g*0.686+srcColor.b*0.168;
    outColor.b=srcColor.r*0.272+srcColor.g*0.534+srcColor.b*0.131;
    outColor.a=srcColor.a;
}
