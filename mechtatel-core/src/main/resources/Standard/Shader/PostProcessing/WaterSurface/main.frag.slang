//@mtt.extractionPath "main.frag.slang"
//@mtt.entryPoint
module main;

import wave;
import shading;

struct CameraUBO {
    float4x4 view;
    float4x4 proj;
    float3 eye;
    float3 center;
};
ConstantBuffer<CameraUBO> camera : register(b0, space0);

struct WaterSurfaceUBO {
    float3 shallowColor;
    float waterLevel;
    float3 deepColor;
    float distortionStrength;
    float3 sunDirection;
    float waveAmplitude;
    float waveFrequency;
    float waveSpeed;
    float absorptionCoefficient;
    float specularStrength;
    float time;
};
ConstantBuffer<WaterSurfaceUBO> water : register(b1, space0);

Texture2D albedoTexture : register(t0, space1);
Texture2D depthTexture : register(t1, space1);
Texture2D positionTexture : register(t2, space1);
Texture2D normalTexture : register(t3, space1);
Texture2D stencilTexture : register(t4, space1);

SamplerState textureSampler : register(s0, space2);

struct PSInput {
    float2 fragTexCoords : TEXCOORD0;
};
struct PSOutput {
    float4 color : SV_Target0;
};

[shader("fragment")]
PSOutput main(PSInput input) {
    PSOutput output;

    float4 albedo = albedoTexture.Sample(textureSampler, input.fragTexCoords);
    float stencil = stencilTexture.Sample(textureSampler, input.fragTexCoords).r;
    float3 fragPos = positionTexture.Sample(textureSampler, input.fragTexCoords).rgb;

    //If the stencil masks this pixel (no scene rendered), keep the albedo as-is.
    //The trailing stencil multiplication below preserves the existing convention
    //of clearing pixels outside the rendered area.
    if (stencil > 0.5) {
        output.color = albedo * (1.0 - stencil);
        return output;
    }

    float3 eye = camera.eye;
    float3 viewVec = fragPos - eye;
    float fragDist = length(viewVec);
    float3 viewDir = viewVec / max(fragDist, 1e-6);

    //Ray-plane intersection with y = waterLevel.
    //Parameter t such that eye + t*viewDir hits the plane.
    float denom = viewDir.y;
    bool rayCrossesSurface = false;
    float tWater = 0.0;
    if (abs(denom) > 1e-4) {
        tWater = (water.waterLevel - eye.y) / denom;
        rayCrossesSurface = (tWater > 0.0) && (tWater < fragDist);
    }

    //If the view ray doesn't pass through the water plane between the camera
    //and the visible fragment, leave the pixel unchanged.
    if (!rayCrossesSurface) {
        output.color = albedo * (1.0 - stencil);
        return output;
    }

    float3 surfacePos = eye + viewDir * tWater;
    float3 waveNormal = sampleWaveNormal(
            surfacePos.xz,
            water.waveAmplitude,
            water.waveFrequency,
            water.waveSpeed,
            water.time);

    //Refraction: distort the screen-space sample using the wave normal.
    //We re-sample only if the new tap is still part of the underwater scene
    //(its world position must be at or below the water level), otherwise
    //we fall back to the original tap to avoid bleeding sky into the water.
    float2 distortion = waveNormal.xz * water.distortionStrength;
    float2 refractedUV = clamp(input.fragTexCoords + distortion, 0.0, 1.0);
    float3 refractedFragPos = positionTexture.Sample(textureSampler, refractedUV).rgb;
    float refractedStencil = stencilTexture.Sample(textureSampler, refractedUV).r;
    float4 refractedAlbedo = albedo;
    float3 sampledFragPos = fragPos;
    float sampledDist = fragDist;
    if (refractedStencil < 0.5 && refractedFragPos.y <= water.waterLevel + 0.05) {
        refractedAlbedo = albedoTexture.Sample(textureSampler, refractedUV);
        sampledFragPos = refractedFragPos;
        sampledDist = length(refractedFragPos - eye);
    }

    //In-water path length: distance the view ray travels through water
    //before reaching the visible (underwater) geometry. We base this on the
    //fragment we actually shade (refracted tap when accepted), so the
    //absorption matches what the user sees through the surface.
    float depthInWater = max(sampledDist - tWater, 0.0);
    float3 tinted = underwaterColor(
            refractedAlbedo.rgb,
            water.shallowColor,
            water.deepColor,
            water.absorptionCoefficient,
            depthInWater);

    //Fresnel mix between refraction and reflected sky.
    //cosTheta uses the surface normal vs the view ray going TOWARDS the viewer.
    float cosTheta = saturate(dot(waveNormal, -viewDir));
    float fresnel = fresnelSchlick(cosTheta, 0.02);
    float3 reflectDir = reflect(viewDir, waveNormal);
    float3 sky = skyColor(reflectDir);
    float3 surfaceColor = lerp(tinted, sky, fresnel);

    //Specular highlight from the sun on the wave surface.
    float spec = specularHighlight(
            waveNormal,
            viewDir,
            normalize(water.sunDirection),
            water.specularStrength);
    surfaceColor += float3(spec);

    output.color = float4(surfaceColor, 1.0) * (1.0 - stencil);
    return output;
}
