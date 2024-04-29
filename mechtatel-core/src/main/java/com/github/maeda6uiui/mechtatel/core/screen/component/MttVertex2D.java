package com.github.maeda6uiui.mechtatel.core.screen.component;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;

/**
 * 2D vertex
 *
 * @author maeda6uiui
 */
public class MttVertex2D {
    public static final int OFFSETOF_POS = 0;
    public static final int OFFSETOF_COLOR = OFFSETOF_POS + 2 * Float.BYTES;
    public static final int OFFSETOF_TEXCOORDS = OFFSETOF_COLOR + 4 * Float.BYTES;
    public static final int SIZEOF = OFFSETOF_TEXCOORDS + 2 * Float.BYTES;

    public Vector2fc pos;
    public Vector4fc color;
    public Vector2fc texCoords;

    public void putToByteBuffer(ByteBuffer buffer) {
        for (int i = 0; i < 2; i++) {
            buffer.putFloat(pos.get(i));
        }
        for (int i = 0; i < 4; i++) {
            buffer.putFloat(color.get(i));
        }
        for (int i = 0; i < 2; i++) {
            buffer.putFloat(texCoords.get(i));
        }
    }

    public MttVertex2D(Vector2fc pos, Vector4fc color, Vector2fc texCoords) {
        this.pos = pos;
        this.color = color;
        this.texCoords = texCoords;
    }

    public MttVertex2D(Vector2fc pos, Vector4fc color) {
        this(pos, color, new Vector2f());
    }

    public MttVertex2D() {
        this(new Vector2f(), new Vector4f(), new Vector2f());
    }
}
