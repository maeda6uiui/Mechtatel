#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(location=0) in vec4 fragColor;

layout(location=0) out vec4 outColor;

void main(){
    outColor.r=fragColor.r*0.393+fragColor.g*0.769+fragColor.b*0.189;
    outColor.g=fragColor.r*0.349+fragColor.g*0.686+fragColor.b*0.168;
    outColor.b=fragColor.r*0.272+fragColor.g*0.534+fragColor.b*0.131;
    outColor.a=fragColor.a;
}
