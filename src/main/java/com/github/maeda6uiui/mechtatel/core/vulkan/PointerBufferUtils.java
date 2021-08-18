package com.github.maeda6uiui.mechtatel.core.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.util.Collection;

/**
 * Provides utility methods relating to PointerBuffer
 *
 * @author maeda
 */
class PointerBufferUtils {
    public static PointerBuffer asPointerBuffer(Collection<String> collection) {
        MemoryStack stack = MemoryStack.stackGet();
        PointerBuffer buffer = stack.mallocPointer(collection.size());
        collection.stream().map(stack::UTF8).forEach(buffer::put);

        return buffer.rewind();
    }
}
