package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_MAT4;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for a camera
 *
 * @author maeda6uiui
 */
public class CameraUBO {
    public static final int SIZEOF = 2 * SIZEOF_MAT4 + 2 * SIZEOF_VEC4;

    private Matrix4f view;
    private Matrix4f proj;

    private Vector3f eye;
    private Vector3f center;

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
        proj = new Matrix4f().perspective(fovY, aspect, zNear, zFar, true);
        proj.m11(proj.m11() * (-1.0f));

        this.eye = new Vector3f(eye);
        this.center = new Vector3f(center);
    }

    private void memcpy(ByteBuffer buffer) {
        view.get(0, buffer);
        proj.get(SIZEOF_MAT4, buffer);
        eye.get(SIZEOF_MAT4 * 2, buffer);
        center.get(SIZEOF_MAT4 * 2 + SIZEOF_VEC4, buffer);

        buffer.rewind();
    }

    public void update(VkDevice device, long uniformBufferMemory) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(device, uniformBufferMemory, 0, SIZEOF, 0, data);
            {
                this.memcpy(data.getByteBuffer(0, SIZEOF));
            }
            vkUnmapMemory(device, uniformBufferMemory);
        }
    }
}
