package com.github.maeda6uiui.mechtatel.core.component;

import org.joml.Vector2fc;
import org.joml.Vector4fc;

/**
 * Vertex2D
 */
public class Vertex2D {
    public static final int SIZEOF = (2 + 4) * Float.BYTES;
    public static final int OFFSETOF_POS = 0;
    public static final int OFFSETOF_COLOR = 2 * Float.BYTES;

    public Vector2fc pos;
    public Vector4fc color;

    public Vertex2D(Vector2fc pos, Vector4fc color) {
        this.pos = pos;
        this.color = color;
    }
}
