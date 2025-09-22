struct MonochromeInfoUBO {
    int dummy;
};
ConstantBuffer<MonochromeInfoUBO> monochromeInfo : register(b0, space0);

Texture2D colorTexture : register(t0, space1);
SamplerState textureSampler : register(s0, space2);

struct PSInput {
    float2 fragTexCoords : TEXCOORD0;
};
struct PSOutput {
    float4 outColor : SV_Target0;
};

[shader("fragment")]
PSOutput main(PSInput input) {
    PSOutput output;

    float3 color = colorTexture.Sample(textureSampler, input.fragTexCoords).rgb;
    float gray = length(color);
    output.outColor = float4(gray, gray, gray, 1.0);

    return output;
}
