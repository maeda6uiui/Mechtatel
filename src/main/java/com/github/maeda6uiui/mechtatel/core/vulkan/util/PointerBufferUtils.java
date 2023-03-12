package com.github.maeda6uiui.mechtatel.core.vulkan.util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;

import java.util.Collection;
import java.util.List;

/**
 * Provides utility methods relating to PointerBuffer
 *
 * @author maeda6uiui
 */
public class PointerBufferUtils {
    public static PointerBuffer asPointerBuffer(Collection<String> collection) {
        MemoryStack stack = MemoryStack.stackGet();
        PointerBuffer buffer = stack.mallocPointer(collection.size());
        collection.stream().map(stack::UTF8).forEach(buffer::put);

        return buffer.rewind();
    }

    public static PointerBuffer asPointerBuffer(List<? extends Pointer> list) {
        MemoryStack stack = MemoryStack.stackGet();
        PointerBuffer buffer = stack.mallocPointer(list.size());
        list.forEach(buffer::put);

        return buffer.rewind();
    }
}
