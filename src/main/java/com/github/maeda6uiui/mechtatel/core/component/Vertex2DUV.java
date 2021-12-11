package com.github.maeda6uiui.mechtatel.core.component;

import org.joml.Vector2fc;
import org.joml.Vector4fc;

/**
 * Vertex2D with a UV
 */
public class Vertex2DUV {
    public static final int SIZEOF = (2 + 4 + 2) * Float.BYTES;
    public static final int OFFSETOF_POS = 0;
    public static final int OFFSETOF_COLOR = 2 * Float.BYTES;
    public static final int OFFSETOF_TEXCOORDS = (2 + 4) * Float.BYTES;

    public Vector2fc pos;
    public Vector4fc color;
    public Vector2fc texCoords;

    public Vertex2DUV(Vector2fc pos, Vector4fc color, Vector2fc texCoords) {
        this.pos = pos;
        this.color = color;
        this.texCoords = texCoords;
    }
}
