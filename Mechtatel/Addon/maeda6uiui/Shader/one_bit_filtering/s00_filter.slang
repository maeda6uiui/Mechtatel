module s00_filter;

public float getFilteredValue(float3 color) {
    if (length(color) < 0.5) {
        return 0.0;
    } else {
        return 1.0;
    }
}
