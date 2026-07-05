//@mtt.extractionPath "main.frag.slang"
//@mtt.entryPoint
module main;

import wave;
import shading;

struct CameraUBO
{
    float4x4 view;
    float4x4 proj;
    float3 eye;
    float3 center;
};
ConstantBuffer<CameraUBO> camera : register(b0, space0);

// Uniforms for the rough-water surface. The std140 layout below is packed as
// six float4 blocks (each a float3 + a trailing scalar) followed by four loose
// floats, so a matching Java UBO would be SIZEOF_VEC4 * 6 + SIZEOF_FLOAT * 4.
// Note there is no distortionStrength / absorptionCoefficient here (rough water
// does not refract); `transmissionStrength` replaces them as the single "how
// much of the scene shows through" control, and `choppiness` / foam fields are
// new.
struct WaterSurfaceUBO
{
    float3 deepColor;
    float waterLevel;
    float3 shallowColor;
    float transmissionStrength;
    float3 sunDirection;
    float waveAmplitude;
    float3 horizonColor;
    float waveFrequency;
    float3 zenithColor;
    float waveSpeed;
    float3 foamColor;
    float foamThreshold;
    float choppiness;
    float foamIntensity;
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

struct PSInput
{
    float2 fragTexCoords : TEXCOORD0;
};
struct PSOutput
{
    float4 color : SV_Target0;
};

[shader("fragment")]
PSOutput main(PSInput input)
{
    PSOutput output;

    float4 albedo = albedoTexture.Sample(textureSampler, input.fragTexCoords);
    float stencil = stencilTexture.Sample(textureSampler, input.fragTexCoords).r;
    float3 fragPos = positionTexture.Sample(textureSampler, input.fragTexCoords).rgb;

    // If the stencil masks this pixel (no scene rendered), keep the albedo as-is.
    // The trailing stencil multiplication below preserves the existing convention
    // of clearing pixels outside the rendered area.
    if (stencil > 0.5)
    {
        output.color = albedo * (1.0 - stencil);
        return output;
    }

    float3 eye = camera.eye;
    float3 viewVec = fragPos - eye;
    float fragDist = length(viewVec);
    float3 viewDir = viewVec / max(fragDist, 1e-6);

    // Ray-plane intersection with y = waterLevel.
    // Parameter t such that eye + t*viewDir hits the plane.
    float denom = viewDir.y;
    bool rayCrossesSurface = false;
    float tWater = 0.0;
    if (abs(denom) > 1e-4)
    {
        tWater = (water.waterLevel - eye.y) / denom;
        rayCrossesSurface = (tWater > 0.0) && (tWater < fragDist);
    }

    // If the view ray doesn't pass through the water plane between the camera
    // and the visible fragment, leave the pixel unchanged.
    if (!rayCrossesSurface)
    {
        output.color = albedo * (1.0 - stencil);
        return output;
    }

    float3 surfacePos = eye + viewDir * tWater;
    WaveSample wave = sampleWave(
        surfacePos.xz,
        water.waveAmplitude,
        water.waveFrequency,
        water.waveSpeed,
        water.choppiness,
        water.time);
    float3 waveNormal = wave.normal;

    // Water body colour. Rough water does not refract, so we read the scene
    // straight through the original tap (no wave-normal distortion) and let
    // waterBodyColor keep only a faint, depth-attenuated amount of it. The
    // in-water path length is the distance the view ray travels below the
    // surface before hitting the visible geometry.
    float depthInWater = max(fragDist - tWater, 0.0);
    float3 body = waterBodyColor(
        albedo.rgb,
        water.shallowColor,
        water.deepColor,
        water.transmissionStrength,
        depthInWater);

    // Fresnel mix between the water body and the reflected sky.
    // cosTheta uses the surface normal vs the view ray going TOWARDS the viewer;
    // the churned normals of rough water produce grazing angles all over, so the
    // sky reflection dominates and hides most of what is underneath.
    float cosTheta = saturate(dot(waveNormal, -viewDir));
    float fresnel = fresnelSchlick(cosTheta, 0.02);
    float3 reflectDir = reflect(viewDir, waveNormal);
    float3 sky = skyColor(reflectDir, water.horizonColor, water.zenithColor);
    float3 surfaceColor = lerp(body, sky, fresnel);

    // Foam: white water on the crests and steep faces, laid over everything.
    float foam = computeFoam(wave, water.foamThreshold, water.foamIntensity);
    surfaceColor = lerp(surfaceColor, water.foamColor, foam);

    // Specular sun glitter. Suppressed where foam covers the surface, since
    // froth scatters rather than mirrors the sun.
    float spec = specularHighlight(
        waveNormal,
        viewDir,
        normalize(water.sunDirection),
        water.specularStrength);
    surfaceColor += float3(spec * (1.0 - foam));

    output.color = float4(surfaceColor, 1.0) * (1.0 - stencil);
    return output;
}