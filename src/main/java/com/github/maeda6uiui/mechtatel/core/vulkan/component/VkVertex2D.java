package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.component.Vertex2D;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static com.github.maeda6uiui.mechtatel.core.component.Vertex2D.OFFSETOF_COLOR;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Vertex2D
 *
 * @author maeda
 */
public class VkVertex2D {
    public static VkVertexInputBindingDescription.Buffer getBindingDescription() {
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1);
        bindingDescription.binding(0);
        bindingDescription.stride(Vertex2D.SIZEOF);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescription;
    }

    public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(2);

        //Position
        VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
        posDescription.binding(0);
        posDescription.location(0);
        posDescription.format(VK_FORMAT_R32G32_SFLOAT);
        posDescription.offset(Vertex2D.OFFSETOF_POS);

        //Color
        VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
        colorDescription.binding(0);
        colorDescription.location(1);
        colorDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
        colorDescription.offset(OFFSETOF_COLOR);

        return attributeDescriptions;
    }
}
