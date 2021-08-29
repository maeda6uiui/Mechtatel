package com.github.maeda6uiui.mechtatel.core.vulkan.util;

import com.github.maeda6uiui.mechtatel.core.camera.CameraUBO;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Utility methods for uniform buffer objects
 *
 * @author maeda
 */
public class UBOUtils {
    private static void memcpyCameraUBO(ByteBuffer buffer, CameraUBO ubo) {
        final int mat4size = 16 * Float.BYTES;

        ubo.view.get(0, buffer);
        ubo.proj.get(AlignmentUtils.alignas(mat4size, AlignmentUtils.alignof(ubo.proj)), buffer);

        buffer.rewind();
    }

    public static void updateCameraUBO(
            VkDevice device,
            List<Long> UBMemories,
            CameraUBO ubo) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            for (var UBMemory : UBMemories) {
                PointerBuffer data = stack.mallocPointer(1);
                vkMapMemory(device, UBMemory, 0, CameraUBO.SIZEOF, 0, data);
                {
                    memcpyCameraUBO(data.getByteBuffer(0, CameraUBO.SIZEOF), ubo);
                }
                vkUnmapMemory(device, UBMemory);
            }
        }
    }
}
