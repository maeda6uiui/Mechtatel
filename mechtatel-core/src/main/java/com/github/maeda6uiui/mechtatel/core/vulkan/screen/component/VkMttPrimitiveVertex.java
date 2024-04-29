package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttPrimitiveVertex;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

/**
 * 3D vertex for primitive
 *
 * @author maeda6uiui
 */
public class VkMttPrimitiveVertex {
    public static VkVertexInputBindingDescription.Buffer getBindingDescription() {
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1);
        bindingDescription.binding(0);
        bindingDescription.stride(MttPrimitiveVertex.SIZEOF);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescription;
    }

    public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(3);

        //Position
        VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
        posDescription.binding(0);
        posDescription.location(0);
        posDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        posDescription.offset(MttPrimitiveVertex.OFFSETOF_POS);

        //Color
        VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
        colorDescription.binding(0);
        colorDescription.location(1);
        colorDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
        colorDescription.offset(MttPrimitiveVertex.OFFSETOF_COLOR);

        //Normal
        VkVertexInputAttributeDescription normalDescription = attributeDescriptions.get(2);
        normalDescription.binding(0);
        normalDescription.location(2);
        normalDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        normalDescription.offset(MttPrimitiveVertex.OFFSETOF_NORMAL);

        return attributeDescriptions;
    }
}
