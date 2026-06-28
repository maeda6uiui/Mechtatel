//@mtt.extractionPath "wave.slang"
module wave;

static const float2 WAVE_DIRS[4] = {
    float2(1.0, 0.0),
    float2(0.0, 1.0),
    float2(0.707, 0.707),
    float2(-0.707, 0.707)
};
static const float WAVE_WEIGHTS[4] = { 1.0, 0.85, 0.6, 0.45 };
static const float WAVE_FREQ_MULTIPLIERS[4] = { 1.0, 1.7, 2.3, 3.1 };

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
