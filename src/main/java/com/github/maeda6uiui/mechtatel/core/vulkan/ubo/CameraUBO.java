package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.AlignmentUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for a camera
 *
 * @author maeda
 */
public class CameraUBO {
    public static final int SIZEOF = (2 * 16 + 2 * 3) * Float.BYTES;

    public Matrix4f view;
    public Matrix4f proj;

    public Vector3f eye;
    public Vector3f center;

    public CameraUBO() {
        view = new Matrix4f();
        proj = new Matrix4f();
        
        eye = new Vector3f();
        center = new Vector3f();
    }

    public CameraUBO(Camera camera) {
        Vector3fc eye = camera.getEye();
        Vector3fc center = camera.getCenter();
        Vector3fc up = camera.getUp();
        float fovY = camera.getFovY();
        float aspect = camera.getAspect();
        float zNear = camera.getZNear();
        float zFar = camera.getZFar();

        view = new Matrix4f().lookAt(
                eye.x(), eye.y(), eye.z(),
                center.x(), center.y(), center.z(),
                up.x(), up.y(), up.z());
        proj = new Matrix4f().perspective(fovY, aspect, zNear, zFar);
        proj.m11(proj.m11() * (-1.0f));

        this.eye = new Vector3f(eye);
        this.center = new Vector3f(center);
    }

    private void memcpy(ByteBuffer buffer) {
        final int mat4Size = 16 * Float.BYTES;
        final int vec3Size = 3 * Float.BYTES;

        view.get(0, buffer);
        proj.get(AlignmentUtils.alignas(mat4Size, AlignmentUtils.alignof(proj)), buffer);
        eye.get(AlignmentUtils.alignas(mat4Size * 2, AlignmentUtils.alignof(eye)), buffer);
        center.get(AlignmentUtils.alignas(mat4Size * 2 + vec3Size, AlignmentUtils.alignof(center)), buffer);

        buffer.rewind();
    }

    public void update(
            VkDevice device,
            List<Long> uniformBufferMemories) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            for (var uniformBufferMemory : uniformBufferMemories) {
                PointerBuffer data = stack.mallocPointer(1);
                vkMapMemory(device, uniformBufferMemory, 0, CameraUBO.SIZEOF, 0, data);
                {
                    this.memcpy(data.getByteBuffer(0, CameraUBO.SIZEOF));
                }
                vkUnmapMemory(device, uniformBufferMemory);
            }
        }
    }
}
