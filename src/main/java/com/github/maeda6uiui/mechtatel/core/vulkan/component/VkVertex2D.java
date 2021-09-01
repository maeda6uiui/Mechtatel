package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Vertex2D
 *
 * @author maeda
 */
public class VkVertex2D {
    public static final int SIZEOF = (2 + 3) * Float.BYTES;
    public static final int OFFSETOF_POS = 0;
    public static final int OFFSETOF_COLOR = 2 * Float.BYTES;

    public Vector2fc pos;
    public Vector3fc color;

    public VkVertex2D(Vector2fc pos, Vector3fc color) {
        this.pos = pos;
        this.color = color;
    }

    public static VkVertexInputBindingDescription.Buffer getBindingDescription() {
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.callocStack(1);
        bindingDescription.binding(0);
        bindingDescription.stride(SIZEOF);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescription;
    }

    public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.callocStack(2);

        //Position
        VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
        posDescription.binding(0);
        posDescription.location(0);
        posDescription.format(VK_FORMAT_R32G32_SFLOAT);
        posDescription.offset(OFFSETOF_POS);

        //Color
        VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
        colorDescription.binding(0);
        colorDescription.location(1);
        colorDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        colorDescription.offset(OFFSETOF_COLOR);

        return attributeDescriptions;
    }
}
