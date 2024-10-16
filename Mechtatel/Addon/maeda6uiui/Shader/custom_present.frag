#version 450
#extension GL_ARB_separate_shader_objects:enable

layout(binding=0) uniform sampler2D texSampler;

layout(location=0) in vec2 fragTexCoords;

layout(location=0) out vec4 outColor;

void main(){
    outColor=texture(texSampler,fragTexCoords);
    outColor.rgb=vec3(1.0)-outColor.rgb;
}
