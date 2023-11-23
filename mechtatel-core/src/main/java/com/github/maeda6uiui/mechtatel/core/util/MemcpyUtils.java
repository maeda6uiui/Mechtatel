package com.github.maeda6uiui.mechtatel.core.util;

import com.github.maeda6uiui.mechtatel.core.component.MttVertex2D;
import com.github.maeda6uiui.mechtatel.core.component.MttVertex2DUV;
import com.github.maeda6uiui.mechtatel.core.component.MttVertex3D;
import com.github.maeda6uiui.mechtatel.core.component.MttVertex3DUV;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Memcpy methods
 *
 * @author maeda6uiui
 */
public class MemcpyUtils {
    public static void memcpyVertex2D(ByteBuffer buffer, List<MttVertex2D> vertices) {
        for (var vertex : vertices) {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());
            buffer.putFloat(vertex.color.w());
        }

        buffer.rewind();
    }

    public static void memcpyVertex2DUV(ByteBuffer buffer, List<MttVertex2DUV> vertices) {
        for (var vertex : vertices) {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());
            buffer.putFloat(vertex.color.w());

            buffer.putFloat(vertex.texCoords.x());
            buffer.putFloat(vertex.texCoords.y());
        }

        buffer.rewind();
    }

    public static void memcpyVertex3D(ByteBuffer buffer, List<MttVertex3D> vertices) {
        for (var vertex : vertices) {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());
            buffer.putFloat(vertex.pos.z());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());
            buffer.putFloat(vertex.color.w());

            buffer.putFloat(vertex.normal.x());
            buffer.putFloat(vertex.normal.y());
            buffer.putFloat(vertex.normal.z());
        }

        buffer.rewind();
    }

    public static void memcpyVertex3DUV(ByteBuffer buffer, List<MttVertex3DUV> vertices) {
        for (var vertex : vertices) {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());
            buffer.putFloat(vertex.pos.z());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());
            buffer.putFloat(vertex.color.w());

            buffer.putFloat(vertex.texCoords.x());
            buffer.putFloat(vertex.texCoords.y());

            buffer.putFloat(vertex.normal.x());
            buffer.putFloat(vertex.normal.y());
            buffer.putFloat(vertex.normal.z());
        }

        buffer.rewind();
    }

    public static void memcpyIntegers(ByteBuffer buffer, List<Integer> indices) {
        for (var index : indices) {
            buffer.putInt(index);
        }

        buffer.rewind();
    }
}
