//@mtt.extractionPath "wave.slang"
//
// Procedural water surface, modelled as a height field h(x, z) over the
// horizontal plane. The height is the sum of four travelling sine waves,
// each with its own direction, weight, frequency multiplier, and speed
// offset. Summing multiple sines (rather than using a single one) produces
// a less obviously-repeating surface — the four components beat against
// each other and the eye sees a more "natural" pattern. This is the same
// idea as a Gerstner wave sum, minus the horizontal displacement, so the
// surface stays flat to the postprocessing pass and only the *normal* of
// the surface affects the shading downstream.
module wave;

// Wave parameters per component. Index i selects the i-th sine wave.
//   WAVE_DIRS[i]            - unit XZ direction the wave travels along
//   WAVE_WEIGHTS[i]         - amplitude scale for this component
//                             (later ones are smaller so the dominant
//                             wave is recognisable and the higher-freq
//                             ones only add detail)
//   WAVE_FREQ_MULTIPLIERS[i] - wavelength scale; non-integer ratios are
//                             intentional so the sum never lines up into
//                             a tiling pattern
// (0.707 ~= 1/sqrt(2), so the diagonal directions are unit length.)
static const float2 WAVE_DIRS[4] = {
    float2(1.0, 0.0),
    float2(0.0, 1.0),
    float2(0.707, 0.707),
    float2(-0.707, 0.707)
};
static const float WAVE_WEIGHTS[4] = { 1.0, 0.85, 0.6, 0.45 };
static const float WAVE_FREQ_MULTIPLIERS[4] = { 1.0, 1.7, 2.3, 3.1 };

// Wave height at a world-space XZ position.
//
// For each component the phase angle is
//     phase_i = (dir_i . worldXZ) * k_i + time * speed * (1 + 0.2 * i)
// where:
//   dir_i . worldXZ    - position along the wave's travel axis; multiplying
//                        by k_i turns world-space distance into radians
//   k_i = frequency * WAVE_FREQ_MULTIPLIERS[i]   - effective angular freq
//   time * speed * ... - phase advances over time, making the wave move;
//                        the per-component (1 + 0.2 * i) factor makes each
//                        wave drift at a slightly different speed so the
//                        sum never loops back to an earlier configuration
// The contribution from each wave is then amplitude * weight_i * sin(phase_i),
// and they are simply summed.
public float sampleWaveHeight(
    float2 worldXZ,
    float amplitude,
    float frequency,
    float speed,
    float time)
{
    float h = 0.0;
    for (int i = 0; i < 4; ++i)
    {
        float k = frequency * WAVE_FREQ_MULTIPLIERS[i];
        float phase = dot(WAVE_DIRS[i], worldXZ) * k + time * speed * (1.0 + 0.2 * i);
        h += amplitude * WAVE_WEIGHTS[i] * sin(phase);
    }
    return h;
}

// World-space normal of the wave surface at a given XZ.
//
// For any height field y = h(x, z), the surface normal is
//     n = normalize(-dh/dx, 1, -dh/dz).
// (Intuition: if h slopes up in +x, the surface tilts away from +x, so
// the normal tilts in the opposite direction. The "1" in y comes from the
// fact that the surface is parameterised by (x, z) with y = h, so the
// gradient of the implicit form F(x,y,z) = y - h(x,z) is exactly that.)
//
// dh/dx and dh/dz are obtained by differentiating sampleWaveHeight term
// by term. The chain rule gives:
//     d/dx [ amp * w_i * sin(phase_i) ]
//         = amp * w_i * cos(phase_i) * d(phase_i)/dx
//         = amp * w_i * cos(phase_i) * dir_i.x * k_i
// (Note WAVE_DIRS[i].y is the Z component, not Y — the wave directions live
// on the XZ plane, packed in float2.) The variable `w` below accumulates
// the common amp * weight * k factor, then we multiply by dir.x / dir.z
// to project the gradient onto each horizontal axis.
public float3 sampleWaveNormal(
    float2 worldXZ,
    float amplitude,
    float frequency,
    float speed,
    float time)
{
    float dhdx = 0.0;
    float dhdz = 0.0;
    for (int i = 0; i < 4; ++i)
    {
        float k = frequency * WAVE_FREQ_MULTIPLIERS[i];
        float phase = dot(WAVE_DIRS[i], worldXZ) * k + time * speed * (1.0 + 0.2 * i);
        float c = cos(phase);
        float w = amplitude * WAVE_WEIGHTS[i] * k;
        dhdx += w * WAVE_DIRS[i].x * c;
        dhdz += w * WAVE_DIRS[i].y * c;
    }
    return normalize(float3(-dhdx, 1.0, -dhdz));
}
