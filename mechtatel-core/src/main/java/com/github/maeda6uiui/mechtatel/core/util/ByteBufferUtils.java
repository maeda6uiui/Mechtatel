package com.github.maeda6uiui.mechtatel.core.util;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttPrimitiveVertex;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex2D;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex2DUV;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertexUV;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utility methods for ByteBuffer
 *
 * @author maeda6uiui
 */
public class ByteBufferUtils {
    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);

        return newBuffer;
    }

    public static ByteBuffer ioResourceToByteBuffer(URI resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
                while (fc.read(buffer) != -1) {
                }
            }
        } else {
            try (InputStream source = resource.toURL().openStream();
                 ReadableByteChannel rbc = Channels.newChannel(source)) {
                buffer = BufferUtils.createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2);
                    }
                }
            }
        }

        buffer.flip();
        return MemoryUtil.memSlice(buffer);
    }

    private static ByteBuffer malloc(int bufferSize, MemoryStack stack) {
        if (stack != null) {
            return stack.malloc(bufferSize);
        } else {
            return MemoryUtil.memAlloc(bufferSize);
        }
    }

    /**
     * Converts a list of {@link MttVertex2D} to a byte buffer.
     * Buffer is created from a stack memory if stack is provided by the argument.
     * Otherwise, it is created from a heap memory allocated by {@code malloc()},
     * and the memory must be freed afterward.
     *
     * @param vertices List of vertices
     * @param stack    Stack
     * @return Byte buffer
     */
    public static ByteBuffer vertices2DToByteBuffer(List<MttVertex2D> vertices, MemoryStack stack) {
        int bufferSize = MttVertex2D.SIZEOF * vertices.size();
        ByteBuffer buffer = malloc(bufferSize, stack);
        vertices.forEach(v -> v.putToByteBuffer(buffer));
        buffer.flip();

        return buffer;
    }

    /**
     * Converts a list of {@link MttVertex2DUV} to a byte buffer.
     * Buffer is created from a stack memory if stack is provided by the argument.
     * Otherwise, it is created from a heap memory allocated by {@code malloc()},
     * and the memory must be freed afterward.
     *
     * @param vertices List of vertices
     * @param stack    Stack
     * @return Byte buffer
     */
    public static ByteBuffer vertices2DUVToByteBuffer(List<MttVertex2DUV> vertices, MemoryStack stack) {
        int bufferSize = MttVertex2DUV.SIZEOF * vertices.size();
        ByteBuffer buffer = malloc(bufferSize, stack);
        vertices.forEach(v -> v.putToByteBuffer(buffer));
        buffer.flip();

        return buffer;
    }

    /**
     * Converts a list of {@link MttPrimitiveVertex} to a byte buffer.
     * Buffer is created from a stack memory if stack is provided by the argument.
     * Otherwise, it is created from a heap memory allocated by {@code malloc()},
     * and the memory must be freed afterward.
     *
     * @param vertices List of vertices
     * @param stack    Stack
     * @return Byte buffer
     */
    public static ByteBuffer verticesToByteBuffer(List<MttPrimitiveVertex> vertices, MemoryStack stack) {
        int bufferSize = MttPrimitiveVertex.SIZEOF * vertices.size();
        ByteBuffer buffer = malloc(bufferSize, stack);
        vertices.forEach(v -> v.putToByteBuffer(buffer));
        buffer.flip();

        return buffer;
    }

    /**
     * Converts a list of {@link MttVertexUV} to a byte buffer.
     * Buffer is created from a stack memory if stack is provided by the argument.
     * Otherwise, it is created from a heap memory allocated by {@code malloc()},
     * and the memory must be freed afterward.
     *
     * @param vertices List of vertices
     * @param stack    Stack
     * @return Byte buffer
     */
    public static ByteBuffer verticesUVToByteBuffer(List<MttVertexUV> vertices, MemoryStack stack) {
        int bufferSize = MttVertexUV.SIZEOF * vertices.size();
        ByteBuffer buffer = malloc(bufferSize, stack);
        vertices.forEach(v -> v.putToByteBuffer(buffer));
        buffer.flip();

        return buffer;
    }

    /**
     * Converts a list of indices to a byte buffer.
     * Buffer is created from a stack memory if stack is provided by the argument.
     * Otherwise, it is created from a heap memory allocated by {@code malloc()},
     * and the memory must be freed afterward.
     *
     * @param indices List of indices
     * @param stack   Stack
     * @return Byte buffer
     */
    public static ByteBuffer indicesToByteBuffer(List<Integer> indices, MemoryStack stack) {
        int bufferSize = Integer.BYTES * indices.size();
        ByteBuffer buffer = malloc(bufferSize, stack);
        indices.forEach(buffer::putInt);
        buffer.flip();

        return buffer;
    }
}
