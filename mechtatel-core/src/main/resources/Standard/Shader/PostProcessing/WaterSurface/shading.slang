//@mtt.extractionPath "shading.slang"
module shading;

public float fresnelSchlick(float cosTheta, float f0)
{
    float x = clamp(1.0 - cosTheta, 0.0, 1.0);
    float x2 = x * x;
    return f0 + (1.0 - f0) * x2 * x2 * x;
}

public float3 skyColor(float3 reflectDir)
{
    float t = clamp(reflectDir.y * 0.5 + 0.5, 0.0, 1.0);
    float3 horizon = float3(0.7, 0.8, 0.85);
    float3 zenith = float3(0.25, 0.45, 0.75);
    return lerp(horizon, zenith, t);
}

public float3 underwaterColor(
    float3 albedo,
    float3 shallowColor,
    float3 deepColor,
    float absorption,
    float depth)
{
    float transmissivity = exp(-absorption * depth);
    float3 tinted = albedo * shallowColor;
    return lerp(deepColor, tinted, transmissivity);
}

public float specularHighlight(
    float3 normal,
    float3 viewDir,
    float3 sunDir,
    float strength)
{
    float3 toViewer = -viewDir;
    float3 toLight = -sunDir;
    float3 halfVec = normalize(toViewer + toLight);
    float ndoth = max(dot(normal, halfVec), 0.0);
    return pow(ndoth, 128.0) * strength;
}
