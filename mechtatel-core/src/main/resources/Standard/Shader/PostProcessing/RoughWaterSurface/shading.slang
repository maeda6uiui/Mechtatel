//@mtt.extractionPath "shading.slang"
//
// Shading helpers for the rough-water postprocessing pass. As with the
// still-water effect these are closed-form approximations of optical effects -
// none trace rays or read extra textures. The rough effect deliberately drops
// refraction: instead of resolving what is seen *through* the surface, the main
// shader builds the water body colour, mixes in a Fresnel-weighted sky
// reflection, lays foam on top of the crests, and finishes with a sun glint.
module shading;

// Schlick's approximation of the Fresnel reflectance: the fraction of light
// reflected at a surface for a viewing angle whose cosine to the normal is
// cosTheta. f0 is the reflectance at normal incidence (~0.02 for water). At
// grazing angles (cosTheta -> 0) it approaches 1 — on a rough sea the churned
// normals expose grazing angles everywhere, which is why so little of the
// underwater scene survives. Closed form: f0 + (1 - f0) * (1 - cosTheta)^5.
public float fresnelSchlick(float cosTheta, float f0)
{
    float x = clamp(1.0 - cosTheta, 0.0, 1.0);
    float x2 = x * x;
    return f0 + (1.0 - f0) * x2 * x2 * x;
}

// Procedural sky used for the reflected component: a vertical gradient, pale
// near the horizon (reflectDir.y ~= 0) and more saturated toward the zenith
// (reflectDir.y ~= 1). Mapping (-1..1) -> (0..1) via reflectDir.y * 0.5 + 0.5
// keeps downward-pointing reflections (common on a choppy surface, where wave
// faces tilt the reflected ray below the horizon) at a sensible horizon colour.
public float3 skyColor(float3 reflectDir, float3 horizon, float3 zenith)
{
    float t = clamp(reflectDir.y * 0.5 + 0.5, 0.0, 1.0);
    return lerp(horizon, zenith, t);
}

// Colour of the water body itself, before reflection and foam.
//
// On rough water the underwater scene is barely visible, so this is mostly
// `deepColor`. A small amount of the underlying scene is transmitted back:
// `albedo` tinted by `shallowColor` (water is never truly colourless), further
// attenuated by Beer-Lambert transmittance exp(-ABSORPTION * depth) so anything
// more than a little below the surface fades to deep water. `transmission` is
// the overall "how much shows through" knob (kept small for the rough look);
// at transmission = 0 the body is pure deepColor.
public float3 waterBodyColor(
    float3 albedo,
    float3 shallowColor,
    float3 deepColor,
    float transmission,
    float depth)
{
    // Fixed absorption: the rough effect has no separate absorption uniform,
    // so a moderate constant does the depth fade while `transmission` scales
    // the overall visibility.
    const float ABSORPTION = 0.5;
    float transmittance = transmission * exp(-ABSORPTION * depth);
    float3 tinted = albedo * shallowColor;
    return lerp(deepColor, tinted, saturate(transmittance));
}

// Blinn-Phong specular highlight from a directional light (the sun).
// `viewDir` points from the eye toward the surface; `sunDir` points the way the
// sun's light travels (so the vector toward the sun is its negation). The
// half-vector H = normalize(V + L) bisects the toward-viewer and toward-light
// vectors; the term peaks when the surface normal aligns with H, i.e. when the
// facet mirrors the sun into the camera. The exponent 80 is lower than the
// still-water 128, giving a broader, more scattered glitter that suits the many
// tilted facets of a rough sea rather than a single tight glint.
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
    return pow(ndoth, 80.0) * strength;
}