package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.shadow;

import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PostProcessingNabor;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkQueue;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;

/**
 * Nabor for shadow mapping
 *
 * @author maeda
 */
public class ShadowMappingNabor extends PostProcessingNabor {
    public static final int MAX_NUM_SHADOW_MAPS = Pass2Nabor.MAX_NUM_SHADOW_MAPS;

    private Pass1Nabor pass1;
    private Pass2Nabor pass2;

    public ShadowMappingNabor(VkDevice device, int shadowCoordsImageFormat) {
        super(device, VK_SAMPLE_COUNT_1_BIT, true);

        pass1 = new Pass1Nabor(device, shadowCoordsImageFormat);
        pass2 = new Pass2Nabor(device);
    }

    @Override
    public void transitionColorImage(long commandPool, VkQueue graphicsQueue) {
        pass2.transitionColorImage(commandPool, graphicsQueue);
    }

    @Override
    public long getColorImageView() {
        return pass2.getColorImageView();
    }

    @Override
    public void compile(
            int colorImageFormat,
            VkExtent2D extent,
            long commandPool,
            VkQueue graphicsQueue,
            int descriptorCount) {
        super.compile(colorImageFormat, extent, commandPool, graphicsQueue, descriptorCount);

        pass1.compile(colorImageFormat, extent, commandPool, graphicsQueue, descriptorCount);
        pass2.compile(colorImageFormat, extent, commandPool, graphicsQueue, descriptorCount);
    }

    @Override
    public void recreate(
            int colorImageFormat,
            VkExtent2D extent,
            long commandPool,
            VkQueue graphicsQueue) {
        super.recreate(colorImageFormat, extent, commandPool, graphicsQueue);

        pass1.recreate(colorImageFormat, extent, commandPool, graphicsQueue);
        pass2.recreate(colorImageFormat, extent, commandPool, graphicsQueue);
    }

    @Override
    public void cleanup(boolean reserveForRecreation) {
        super.cleanup(reserveForRecreation);

        pass1.cleanup(reserveForRecreation);
        pass2.cleanup(reserveForRecreation);
    }

    @Override
    public long getTextureSampler(int passIndex) {
        long textureSampler;

        switch (passIndex) {
            case 0:
                textureSampler = pass1.getTextureSampler(0);
                break;
            case 1:
                textureSampler = pass2.getTextureSampler(0);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return textureSampler;
    }

    @Override
    public long getRenderPass(int passIndex) {
        long renderPass;

        switch (passIndex) {
            case 0:
                renderPass = pass1.getRenderPass();
                break;
            case 1:
                renderPass = pass2.getRenderPass();
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return renderPass;
    }

    @Override
    public LongBuffer pDescriptorSets(int passIndex) {
        LongBuffer pSets;

        switch (passIndex) {
            case 0:
                pSets = pass1.pDescriptorSets();
                break;
            case 1:
                pSets = pass2.pDescriptorSets();
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return pSets;
    }

    @Override
    public long getPipelineLayout(int passIndex) {
        long pipelineLayout;

        switch (passIndex) {
            case 0:
                pipelineLayout = pass1.getPipelineLayout(0);
                break;
            case 1:
                pipelineLayout = pass2.getPipelineLayout(0);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return pipelineLayout;
    }

    @Override
    public long getGraphicsPipeline(int passIndex) {
        long graphicsPipeline;

        switch (passIndex) {
            case 0:
                graphicsPipeline = pass1.getGraphicsPipeline(0);
                break;
            case 1:
                graphicsPipeline = pass2.getGraphicsPipeline(1);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return graphicsPipeline;
    }

    @Override
    public long getFramebuffer(int passIndex) {
        long framebuffer;

        switch (passIndex) {
            case 0:
                framebuffer = pass1.getFramebuffer(0);
                break;
            case 1:
                framebuffer = pass2.getFramebuffer(0);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return framebuffer;
    }

    @Override
    public void bindImages(
            VkCommandBuffer commandBuffer,
            long colorImageView,
            long depthImageView,
            long positionImageView,
            long normalImageView) {
        this.bindImages(
                commandBuffer,
                pass2,
                colorImageView,
                depthImageView,
                positionImageView,
                normalImageView);
    }
}
