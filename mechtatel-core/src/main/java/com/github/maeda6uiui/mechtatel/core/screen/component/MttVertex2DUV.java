package com.github.maeda6uiui.mechtatel.core.screen.component;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;

/**
 * 2D vertex with UV
 *
 * @author maeda6uiui
 */
public class MttVertex2DUV {
    public static final int SIZEOF = (2 + 4 + 2) * Float.BYTES;
    public static final int OFFSETOF_POS = 0;
    public static final int OFFSETOF_COLOR = 2 * Float.BYTES;
    public static final int OFFSETOF_TEXCOORDS = (2 + 4) * Float.BYTES;

    public Vector2fc pos;
    public Vector4fc color;
    public Vector2fc texCoords;

    public void putToByteBuffer(ByteBuffer buffer) {
        buffer.putFloat(pos.x());
        buffer.putFloat(pos.y());

        buffer.putFloat(color.x());
        buffer.putFloat(color.y());
        buffer.putFloat(color.z());
        buffer.putFloat(color.w());

        buffer.putFloat(texCoords.x());
        buffer.putFloat(texCoords.y());
    }

    public MttVertex2DUV(Vector2fc pos, Vector4fc color, Vector2fc texCoords) {
        this.pos = pos;
        this.color = color;
        this.texCoords = texCoords;
    }

    public MttVertex2DUV() {
        pos = new Vector2f();
        color = new Vector4f();
        texCoords = new Vector2f();
    }
}
