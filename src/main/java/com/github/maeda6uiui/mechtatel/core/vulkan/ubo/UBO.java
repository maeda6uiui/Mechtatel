package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Base class for uniform buffer object
 *
 * @author maeda6uiui
 */
public abstract class UBO {
    public UBO() {

    }

    protected abstract void memcpy(ByteBuffer buffer);

    protected abstract int getSize();

    public void update(VkDevice device, long uniformBufferMemory) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int size = this.getSize();

            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(device, uniformBufferMemory, 0, size, 0, data);
            {
                this.memcpy(data.getByteBuffer(0, size));
            }
            vkUnmapMemory(device, uniformBufferMemory);
        }
    }

    public void update(VkDevice device, long uniformBufferMemory, int index) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int size = this.getSize();

            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(device, uniformBufferMemory, index * size, size, 0, data);
            {
                this.memcpy(data.getByteBuffer(0, size));
            }
            vkUnmapMemory(device, uniformBufferMemory);
        }
    }
}
