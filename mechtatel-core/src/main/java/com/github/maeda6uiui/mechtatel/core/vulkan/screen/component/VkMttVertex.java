package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

/**
 * 3D vertex
 *
 * @author maeda6uiui
 */
public class VkMttVertex {
    public static VkVertexInputBindingDescription.Buffer getBindingDescription() {
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1);
        bindingDescription.binding(0);
        bindingDescription.stride(MttVertex.SIZEOF);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescription;
    }

    public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(6);

        //Position
        VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
        posDescription.binding(0);
        posDescription.location(0);
        posDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        posDescription.offset(MttVertex.OFFSETOF_POS);

        //Color
        VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
        colorDescription.binding(0);
        colorDescription.location(1);
        colorDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
        colorDescription.offset(MttVertex.OFFSETOF_COLOR);

        //Texture coordinates
        VkVertexInputAttributeDescription texCoordsDescription = attributeDescriptions.get(2);
        texCoordsDescription.binding(0);
        texCoordsDescription.location(2);
        texCoordsDescription.format(VK_FORMAT_R32G32_SFLOAT);
        texCoordsDescription.offset(MttVertex.OFFSETOF_TEXCOORDS);

        //Normal
        VkVertexInputAttributeDescription normalDescription = attributeDescriptions.get(3);
        normalDescription.binding(0);
        normalDescription.location(3);
        normalDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        normalDescription.offset(MttVertex.OFFSETOF_NORMAL);

        //Bone weights
        VkVertexInputAttributeDescription boneWeightsDescription = attributeDescriptions.get(4);
        boneWeightsDescription.binding(0);
        boneWeightsDescription.location(4);
        boneWeightsDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
        boneWeightsDescription.offset(MttVertex.OFFSETOF_BONE_WEIGHTS);

        //Bone indices
        VkVertexInputAttributeDescription boneIndicesDescription = attributeDescriptions.get(5);
        boneIndicesDescription.binding(0);
        boneIndicesDescription.location(5);
        boneIndicesDescription.format(VK_FORMAT_R32G32B32A32_SINT);
        boneIndicesDescription.offset(MttVertex.OFFSETOF_BONE_INDICES);

        return attributeDescriptions;
    }
}
