package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertexUV;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

/**
 * 3D vertex with a UV
 *
 * @author maeda6uiui
 */
public class VkMttVertexUV {
    public static VkVertexInputBindingDescription.Buffer getBindingDescription() {
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1);
        bindingDescription.binding(0);
        bindingDescription.stride(MttVertexUV.SIZEOF);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescription;
    }

    public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(4);

        //Position
        VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
        posDescription.binding(0);
        posDescription.location(0);
        posDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        posDescription.offset(MttVertexUV.OFFSETOF_POS);

        //Color
        VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
        colorDescription.binding(0);
        colorDescription.location(1);
        colorDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
        colorDescription.offset(MttVertexUV.OFFSETOF_COLOR);

        //Texture coordinates
        VkVertexInputAttributeDescription texCoordsDescription = attributeDescriptions.get(2);
        texCoordsDescription.binding(0);
        texCoordsDescription.location(2);
        texCoordsDescription.format(VK_FORMAT_R32G32_SFLOAT);
        texCoordsDescription.offset(MttVertexUV.OFFSETOF_TEXCOORDS);

        //Normal
        VkVertexInputAttributeDescription normalDescription = attributeDescriptions.get(3);
        normalDescription.binding(0);
        normalDescription.location(3);
        normalDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        normalDescription.offset(MttVertexUV.OFFSETOF_NORMAL);

        return attributeDescriptions;
    }
}