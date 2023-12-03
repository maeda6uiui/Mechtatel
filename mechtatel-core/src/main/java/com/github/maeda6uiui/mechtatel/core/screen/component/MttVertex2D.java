package com.github.maeda6uiui.mechtatel.core.screen.component;

import org.joml.Vector2fc;
import org.joml.Vector4fc;

/**
 * 2D vertex
 *
 * @author maeda6uiui
 */
public class MttVertex2D {
    public static final int SIZEOF = (2 + 4) * Float.BYTES;
    public static final int OFFSETOF_POS = 0;
    public static final int OFFSETOF_COLOR = 2 * Float.BYTES;

    public Vector2fc pos;
    public Vector4fc color;

    public MttVertex2D(Vector2fc pos, Vector4fc color) {
        this.pos = pos;
        this.color = color;
    }
}
