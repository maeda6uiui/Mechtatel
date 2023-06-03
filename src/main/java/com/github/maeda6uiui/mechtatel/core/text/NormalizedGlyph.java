package com.github.maeda6uiui.mechtatel.core.text;

/**
 * Normalized glyph
 *
 * @author maeda6uiui
 */
public class NormalizedGlyph {
    public final float width;
    public final float height;
    public final float x;
    public final float y;
    public final float advance;

    public NormalizedGlyph(float width, float height, float x, float y, float advance) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.advance = advance;
    }
}
