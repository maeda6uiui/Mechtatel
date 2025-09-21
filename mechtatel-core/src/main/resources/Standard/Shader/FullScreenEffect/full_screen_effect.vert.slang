struct VSInput {
    float2 inPosition : POSITION;
    float4 inColor : COLOR0;
    float2 inTexCoords : TEXCOORD0;
};
struct VSOutput {
    float4 position : SV_Position;
    float2 fragTexCoords : TEXCOORD0;
};

[shader("vertex")]
VSOutput main(VSInput input) {
    VSOutput output;
    output.position = float4(input.inPosition, 0.0, 1.0);
    output.fragTexCoords = input.inTexCoords;

    return output;
}
