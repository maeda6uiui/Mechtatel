//@mtt.extractionPath "wave.slang"
//
// Procedural rough-water surface, modelled as a height field h(x, z) over the
// horizontal plane. Like the still-water version this is a sum of travelling
// sine waves, but tuned for a choppy ocean: more octaves, faster amplitude
// decay, and a `choppiness` control that both spreads the octave frequencies
// apart and boosts the amplitude of the fine detail. The result is a surface
// whose *normal* varies violently, which is what makes the reflection break up
// into the "barely see anything underneath" look the rough effect wants.
//
// Unlike the still-water module there is no horizontal (Gerstner) displacement
// here either — the surface stays flat to the postprocessing pass and only its
// normal (and a derived foam mask) feed the shading downstream. Because every
// per-octave factor is a compile-time-ish constant, the wave normal is still an
// exact analytic derivative of the height field rather than a finite difference.
module wave;

// Number of summed sine octaves. More octaves than the still-water effect (4)
// so the rough surface carries visible high-frequency chop.
static const int NUM_WAVES = 6;

// Wave parameters per octave. Index i selects the i-th sine wave.
//   WAVE_DIRS[i]            - unit XZ direction the wave travels along
//   WAVE_WEIGHTS[i]         - amplitude scale for this octave (decaying, so the
//                             dominant swell is recognisable and later octaves
//                             only add chop)
//   WAVE_FREQ_MULTIPLIERS[i] - wavelength scale; deliberately non-integer ratios
//                             so the sum never lines up into a tiling pattern
// (0.707 ~= 1/sqrt(2); 0.383/0.924 ~= sin/cos(22.5 deg) — all unit length.)
static const float2 WAVE_DIRS[6] = {
    float2(1.0, 0.0),
    float2(0.0, 1.0),
    float2(0.707, 0.707),
    float2(-0.707, 0.707),
    float2(0.383, 0.924),
    float2(0.924, -0.383)
};
static const float WAVE_WEIGHTS[6] = { 1.0, 0.7, 0.5, 0.35, 0.25, 0.18 };
static const float WAVE_FREQ_MULTIPLIERS[6] = { 1.0, 1.9, 2.7, 3.8, 5.3, 7.1 };

// Large-scale ocean swell: a couple of slow, long-wavelength waves layered on
// top of the chop. Their job is to heave the whole surface up and down (the
// "water level fluctuates" part) rather than add ripple detail, so they have a
// much lower frequency and speed than the octaves above and their amplitude is
// controlled separately. Two crossed directions keep the heave from looking
// like a single 1-D corrugation.
static const int NUM_SWELLS = 2;
static const float2 SWELL_DIRS[2] = {
    float2(0.86, 0.5),
    float2(0.5, -0.86)
};
static const float SWELL_WEIGHTS[2] = { 1.0, 0.45 };
static const float SWELL_FREQ_MULTIPLIERS[2] = { 1.0, 1.7 };

// One evaluation of the wave field at a point: the surface height, its unit
// normal, and the maximum possible height (the sum of all octave amplitudes).
// `maxHeight` lets downstream code normalise `height` into [-1, 1] without
// knowing the octave weights, which is how the foam mask decides "is this a
// crest?" independent of the chosen amplitude.
public struct WaveSample
{
    public float height;
    public float3 normal;
    public float maxHeight;
};

// Per-octave effective amplitude weight after applying choppiness.
//   detailGain = 1 + choppiness * (i / (NUM_WAVES - 1))
// so the coarsest octave (i = 0) is untouched and the finest (i = NUM_WAVES-1)
// is boosted by (1 + choppiness). Higher choppiness therefore pushes energy
// into the fine chop, roughening the surface without changing the swell.
float effectiveWeight(int i, float choppiness)
{
    float detailGain = 1.0 + choppiness * (float(i) / float(NUM_WAVES - 1));
    return WAVE_WEIGHTS[i] * detailGain;
}

// Per-octave angular frequency after applying choppiness.
//   k = frequency * WAVE_FREQ_MULTIPLIERS[i] * (1 + 0.1 * choppiness * i)
// The extra i-dependent term spreads the higher octaves further up the
// spectrum as choppiness rises, so the chop reads as many short, sharp ripples
// rather than a scaled copy of the swell.
float effectiveFrequency(int i, float frequency, float choppiness)
{
    return frequency * WAVE_FREQ_MULTIPLIERS[i] * (1.0 + 0.1 * choppiness * float(i));
}

// Evaluate the full wave field at a world-space XZ position.
//
// Height is sum_i amp * w_i * sin(phase_i), the same construction as the
// still-water module, summed over both the chop octaves and the swell. The
// normal of a height field y = h(x, z) is
//     n = normalize(-dh/dx, 1, -dh/dz)
// and each component differentiates to
//     d/dx [ amp * w_i * sin(phase_i) ] = amp * w_i * k_i * dir_i.x * cos(phase_i)
// (the .y of each direction is the Z component — the directions live on the XZ
// plane, packed in float2). `w_i` and `k_i` are the choppiness-adjusted values
// above for the chop, and the swell constants for the swell.
//
// The returned `height` is what displaces the surface plane vertically, so with
// the swell present the whole ocean visibly heaves up and down over time.
public WaveSample sampleWave(
    float2 worldXZ,
    float amplitude,
    float frequency,
    float speed,
    float choppiness,
    float swellAmplitude,
    float swellFrequency,
    float swellSpeed,
    float time)
{
    float h = 0.0;
    float dhdx = 0.0;
    float dhdz = 0.0;
    float maxHeight = 0.0;

    for (int i = 0; i < NUM_WAVES; ++i)
    {
        float w = effectiveWeight(i, choppiness);
        float k = effectiveFrequency(i, frequency, choppiness);
        float phase = dot(WAVE_DIRS[i], worldXZ) * k + time * speed * (1.0 + 0.2 * float(i));

        float s = sin(phase);
        float c = cos(phase);
        float amp = amplitude * w;

        h += amp * s;
        dhdx += amp * k * WAVE_DIRS[i].x * c;
        dhdz += amp * k * WAVE_DIRS[i].y * c;
        maxHeight += amp;
    }

    for (int i = 0; i < NUM_SWELLS; ++i)
    {
        float k = swellFrequency * SWELL_FREQ_MULTIPLIERS[i];
        float phase = dot(SWELL_DIRS[i], worldXZ) * k + time * swellSpeed * (1.0 + 0.15 * float(i));

        float s = sin(phase);
        float c = cos(phase);
        float amp = swellAmplitude * SWELL_WEIGHTS[i];

        h += amp * s;
        dhdx += amp * k * SWELL_DIRS[i].x * c;
        dhdz += amp * k * SWELL_DIRS[i].y * c;
        maxHeight += amp;
    }

    WaveSample result;
    result.height = h;
    result.normal = normalize(float3(-dhdx, 1.0, -dhdz));
    result.maxHeight = maxHeight;
    return result;
}

// Foam coverage in [0, 1] derived from a wave sample.
//
// Two cues drive foam on a real rough sea:
//   * crests   - the tops of waves froth first. `crest` maps the normalised
//                height (height / maxHeight, in [-1, 1]) through a smoothstep
//                that starts at `threshold`, so only the upper part of each
//                wave contributes. threshold near 1 -> foam only on the very
//                tips; near 0 -> foam over most of the upper surface.
//   * steepness - near-vertical faces (where the surface normal tilts far from
//                straight up) also churn. `slope` = 1 - normal.y grows as the
//                face steepens; the smoothstep ignores the gentle swell and
//                only lets genuinely steep chop add foam.
// The two are combined (steepness at half weight so crests dominate), scaled by
// `intensity`, and clamped. `intensity` is the overall foam amount knob.
public float computeFoam(WaveSample wave, float threshold, float intensity)
{
    float normalisedHeight = wave.height / max(wave.maxHeight, 1e-4);
    float crest = smoothstep(threshold, 1.0, normalisedHeight);

    float slope = 1.0 - wave.normal.y;
    float steepFoam = smoothstep(0.2, 0.6, slope);

    return saturate((crest + steepFoam * 0.5) * intensity);
}