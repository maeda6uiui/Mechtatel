package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.shadow;

import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PostProcessingNabor;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkQueue;

import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;

/**
 * Nabor for shadow mapping
 *
 * @author maeda6uiui
 */
public class ShadowMappingNabor extends PostProcessingNabor {
    public static final int MAX_NUM_SHADOW_MAPS = Pass2Nabor.MAX_NUM_SHADOW_MAPS;

    private int depthImageWidth;
    private int depthImageHeight;

    private Pass1Nabor pass1;
    private Pass2Nabor pass2;

    public ShadowMappingNabor(VkDevice device, int depthImageFormat, int depthImageWidth, int depthImageHeight) {
        super(device, VK_SAMPLE_COUNT_1_BIT, true);

        this.depthImageWidth = depthImageWidth;
        this.depthImageHeight = depthImageHeight;

        pass1 = new Pass1Nabor(device, depthImageFormat);
        pass2 = new Pass2Nabor(device);
    }

    @Override
    public void compile(
            int colorImageFormat,
            VkExtent2D extent,
            long commandPool,
            VkQueue graphicsQueue,
            int descriptorCount) {
        super.compile(colorImageFormat, extent, commandPool, graphicsQueue, descriptorCount);

        VkExtent2D depthExtent = VkExtent2D.create().set(depthImageWidth, depthImageHeight);
        pass1.compile(colorImageFormat, depthExtent, commandPool, graphicsQueue, descriptorCount);

        pass2.compile(colorImageFormat, extent, commandPool, graphicsQueue, descriptorCount);
    }

    @Override
    public void recreate(
            int colorImageFormat,
            VkExtent2D extent) {
        super.recreate(colorImageFormat, extent);

        //pass1.recreate(colorImageFormat, extent);
        pass2.recreate(colorImageFormat, extent);
    }

    @Override
    public void cleanup(boolean reserveForRecreation) {
        super.cleanup(reserveForRecreation);

        pass1.cleanup(reserveForRecreation);
        pass2.cleanup(reserveForRecreation);
    }

    @Override
    public void transitionColorImageLayout(long commandPool, VkQueue graphicsQueue) {
        pass2.transitionColorImageLayout(commandPool, graphicsQueue);
    }

    @Override
    public long getColorImageView() {
        return pass2.getColorImageView();
    }

    @Override
    public VkExtent2D getExtent(int naborIndex) {
        VkExtent2D extent;

        switch (naborIndex) {
            case 0:
                extent = pass1.getExtent();
                break;
            case 1:
                extent = pass2.getExtent();
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return extent;
    }

    @Override
    public long getTextureSampler(int naborIndex, int arrayIndex) {
        long textureSampler;

        switch (naborIndex) {
            case 0:
                textureSampler = pass1.getTextureSampler(arrayIndex);
                break;
            case 1:
                textureSampler = pass2.getTextureSampler(arrayIndex);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return textureSampler;
    }

    @Override
    public long getUniformBufferMemory(int naborIndex, int arrayIndex) {
        long uniformBufferMemory;

        switch (naborIndex) {
            case 0:
                uniformBufferMemory = pass1.getUniformBufferMemory(arrayIndex);
                break;
            case 1:
                uniformBufferMemory = pass2.getUniformBufferMemory(arrayIndex);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return uniformBufferMemory;
    }

    @Override
    public long getRenderPass(int naborIndex) {
        long renderPass;

        switch (naborIndex) {
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
    public LongBuffer pDescriptorSets(int naborIndex) {
        LongBuffer pSets;

        switch (naborIndex) {
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
    public long getPipelineLayout(int naborIndex, int arrayIndex) {
        long pipelineLayout;

        switch (naborIndex) {
            case 0:
                pipelineLayout = pass1.getPipelineLayout(arrayIndex);
                break;
            case 1:
                pipelineLayout = pass2.getPipelineLayout(arrayIndex);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return pipelineLayout;
    }

    @Override
    public long getGraphicsPipeline(int naborIndex, int arrayIndex) {
        long graphicsPipeline;

        switch (naborIndex) {
            case 0:
                graphicsPipeline = pass1.getGraphicsPipeline(arrayIndex);
                break;
            case 1:
                graphicsPipeline = pass2.getGraphicsPipeline(arrayIndex);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return graphicsPipeline;
    }

    @Override
    public long getImage(int naborIndex, int arrayIndex) {
        long image;

        switch (naborIndex) {
            case 0:
                image = pass1.getImage(arrayIndex);
                break;
            case 1:
                image = pass2.getImage(arrayIndex);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return image;
    }

    @Override
    public long getImageView(int naborIndex, int arrayIndex) {
        long imageView;

        switch (naborIndex) {
            case 0:
                imageView = pass1.getImageView(arrayIndex);
                break;
            case 1:
                imageView = pass2.getImageView(arrayIndex);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return imageView;
    }

    @Override
    public long getFramebuffer(int naborIndex, int arrayIndex) {
        long framebuffer;

        switch (naborIndex) {
            case 0:
                framebuffer = pass1.getFramebuffer(arrayIndex);
                break;
            case 1:
                framebuffer = pass2.getFramebuffer(arrayIndex);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return framebuffer;
    }

    @Override
    public void bindImages(
            VkCommandBuffer commandBuffer,
            int naborIndex,
            int dstSet,
            int dstBinding,
            List<Long> imageViews) {
        if (naborIndex == 1) {
            pass2.bindImages(commandBuffer, dstSet, dstBinding, imageViews);
        } else {
            throw new RuntimeException("Unsupported operation");
        }
    }

    @Override
    public void bindImages(
            VkCommandBuffer commandBuffer,
            int naborIndex,
            int dstBinding,
            long albedoImageView,
            long depthImageView,
            long positionImageView,
            long normalImageView) {
        if (naborIndex == 1) {
            pass2.bindImages(
                    commandBuffer,
                    dstBinding,
                    albedoImageView,
                    depthImageView,
                    positionImageView,
                    normalImageView);
        } else {
            throw new RuntimeException("Unsupported operation");
        }
    }
}
