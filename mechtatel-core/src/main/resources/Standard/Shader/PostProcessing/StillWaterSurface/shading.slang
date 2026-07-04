//@mtt.extractionPath "shading.slang"
//
// Shading helpers for the water-surface postprocessing pass. Each function
// is a closed-form approximation of a real optical effect - none of them
// trace rays or read additional textures. The main shader composes them:
// fresnel mixes between refraction (tinted by underwaterColor) and sky
// reflection (skyColor), then specularHighlight adds a sun glint on top.
module shading;

// Schlick's approximation of the Fresnel reflectance.
// Returns the fraction of light reflected at a surface for a viewing
// angle whose cosine to the normal is cosTheta. f0 is the reflectance at
// normal incidence (cosTheta = 1); for water, f0 ~= 0.02.
// At grazing angles (cosTheta -> 0) the result approaches 1, which is why
// you see strong sky reflection at the far end of a calm pond.
// The closed form is f0 + (1 - f0) * (1 - cosTheta)^5.
public float fresnelSchlick(float cosTheta, float f0)
{
    float x = clamp(1.0 - cosTheta, 0.0, 1.0);
    float x2 = x * x;
    return f0 + (1.0 - f0) * x2 * x2 * x;
}

// Procedural sky used for the reflected component. A simple vertical
// gradient: pale near the horizon (reflectDir.y ~= 0), more saturated blue
// at the zenith (reflectDir.y ~= 1). Mapping (-1..1) -> (0..1) with
// reflectDir.y * 0.5 + 0.5 lets reflections that point downward (rare,
// only when the wave normal tilts the view ray below the horizon) still
// produce a sensible horizon-colour fallback.
public float3 skyColor(float3 reflectDir, float3 horizon, float3 zenith)
{
    float t = clamp(reflectDir.y * 0.5 + 0.5, 0.0, 1.0);
    return lerp(horizon, zenith, t);
}

// Tint applied to whatever is seen through the water as a function of how
// far the light has travelled in the water. This is a Beer-Lambert
// transmissivity: T = exp(-absorption * depth), the fraction of light that
// survives a path of length `depth`.
//
// At depth = 0 the water is perfectly clear, so the result is just the
// underlying albedo multiplied by `shallowColor` (a slight bluish-green
// tint applied even to nearby objects, modelling the fact that water is
// never truly colourless). As depth grows, T -> 0 and the result fades to
// `deepColor`, the "infinite-depth" colour of the water itself.
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

// Blinn-Phong specular highlight from a directional light (the sun).
// `viewDir` points from the eye toward the surface; `sunDir` points the
// way the sun's light is travelling (so the vector toward the sun is its
// negation). The half-vector H = normalize(V + L) bisects view and light
// when both point away from the surface; the specular term peaks when the
// surface normal aligns with H - i.e. when the surface is oriented to
// mirror sun light directly into the camera.
// The hard-coded exponent 128 gives a tight, glint-like highlight; raise
// it for sharper points of light, lower it for softer reflections.
public float specularHighlight(
    float3 normal,
    float3 viewDir,
    float3 sunDir,
    float strength)
{
    float3 toViewer = -viewDir;
    float3 toLight = -sunDir;
    if (length(toViewer + toLight) < 1e-4) {
        return 0.0;
    }

    float3 halfVec = normalize(toViewer + toLight);
    float ndoth = max(dot(normal, halfVec), 0.0);
    return pow(ndoth, 128.0) * strength;
}
