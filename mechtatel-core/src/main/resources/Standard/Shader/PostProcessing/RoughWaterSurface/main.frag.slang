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
// six float4 blocks (each a float3 + a trailing scalar) followed by eight loose
// floats (two more float4 blocks), so a matching Java UBO would be
// SIZEOF_VEC4 * 6 + SIZEOF_FLOAT * 8.
// `transmissionStrength` controls how much of the underwater scene shows
// through, and `distortionStrength` (like the still-water effect) offsets that
// underwater sample by the wave normal to fake refraction — set it to 0 to read
// the scene straight through. The `swell*` trio drives the large, slow heave
// that makes the water level rise and fall like an ocean.
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
    float swellAmplitude;
    float swellFrequency;
    float swellSpeed;
    float time;
    float distortionStrength;
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

    // Ray intersection with the heaving water surface y = waterLevel + h(x,z,t).
    // The surface height h depends on the XZ we hit, which in turn depends on the
    // intersection distance, so we solve it by fixed-point iteration: start from
    // the flat plane at waterLevel, sample the wave there, re-intersect against
    // the displaced surface, and repeat. A couple of steps are plenty for the
    // gentle slopes of a water surface, and this is what makes the whole surface
    // visibly rise and fall rather than sit at a fixed level.
    float denom = viewDir.y;
    bool rayCrossesSurface = false;
    float tWater = 0.0;
    if (abs(denom) > 1e-4)
    {
        tWater = (water.waterLevel - eye.y) / denom;
        for (int iter = 0; iter < 2; ++iter)
        {
            float3 p = eye + viewDir * tWater;
            float h = sampleWave(
                p.xz,
                water.waveAmplitude,
                water.waveFrequency,
                water.waveSpeed,
                water.choppiness,
                water.swellAmplitude,
                water.swellFrequency,
                water.swellSpeed,
                water.time).height;
            tWater = (water.waterLevel + h - eye.y) / denom;
        }
        rayCrossesSurface = (tWater > 0.0) && (tWater < fragDist);
    }

    // If the view ray doesn't pass through the water surface between the camera
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
        water.swellAmplitude,
        water.swellFrequency,
        water.swellSpeed,
        water.time);
    float3 waveNormal = wave.normal;

    // Refraction: offset the underwater sample by the wave normal, just like the
    // still-water effect (distortionStrength = 0 disables it, reading the scene
    // straight through). We only accept the refracted tap if it is still part of
    // the underwater scene — not masked by the stencil, and at or below the water
    // level — otherwise we fall back to the straight-through sample so foreground
    // objects or sky above the surface don't bleed into the water.
    float2 distortion = waveNormal.xz * water.distortionStrength;
    float2 refractedUV = clamp(input.fragTexCoords + distortion, 0.0, 1.0);
    float3 refractedFragPos = positionTexture.Sample(textureSampler, refractedUV).rgb;
    float refractedStencil = stencilTexture.Sample(textureSampler, refractedUV).r;
    float3 sampledAlbedo = albedo.rgb;
    float sampledDist = fragDist;
    if (refractedStencil < 0.5 && refractedFragPos.y <= water.waterLevel+wave.height + 0.05)
    {
        sampledAlbedo = albedoTexture.Sample(textureSampler, refractedUV).rgb;
        sampledDist = length(refractedFragPos - eye);
    }

    // Water body colour: waterBodyColor keeps only a faint, depth-attenuated
    // amount of the (now possibly refracted) underwater sample. The in-water
    // path length is the distance the view ray travels below the surface before
    // reaching the visible geometry we actually shade.
    float depthInWater = max(sampledDist - tWater, 0.0);
    float3 body = waterBodyColor(
        sampledAlbedo,
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