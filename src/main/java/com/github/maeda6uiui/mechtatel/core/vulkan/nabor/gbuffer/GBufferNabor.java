package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.gbuffer;

import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.Nabor;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkQueue;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;

/**
 * Nabor for G-Buffer
 *
 * @author maeda
 */
public class GBufferNabor extends Nabor {
    public static final int MAX_NUM_TEXTURES = AlbedoNabor.MAX_NUM_TEXTURES;

    private AlbedoNabor albedoNabor;
    private PropertiesNabor propertiesNabor;

    public GBufferNabor(
            VkDevice device,
            int albedoMsaaSamples,
            int depthImageFormat,
            int positionImageFormat,
            int normalImageFormat) {
        super(device, VK_SAMPLE_COUNT_1_BIT, true);

        albedoNabor = new AlbedoNabor(device, albedoMsaaSamples, depthImageFormat);
        propertiesNabor = new PropertiesNabor(device, depthImageFormat, positionImageFormat, normalImageFormat);
    }

    @Override
    public void compile(
            int colorImageFormat,
            VkExtent2D extent,
            long commandPool,
            VkQueue graphicsQueue,
            int descriptorCount) {
        super.compile(colorImageFormat, extent, commandPool, graphicsQueue, descriptorCount);

        albedoNabor.compile(colorImageFormat, extent, commandPool, graphicsQueue, descriptorCount);
        propertiesNabor.compile(colorImageFormat, extent, commandPool, graphicsQueue, descriptorCount);
    }

    @Override
    public void recreate(
            int colorImageFormat,
            VkExtent2D extent) {
        super.recreate(colorImageFormat, extent);

        albedoNabor.recreate(colorImageFormat, extent);
        propertiesNabor.recreate(colorImageFormat, extent);
    }

    @Override
    public void cleanup(boolean reserveForRecreation) {
        super.cleanup(reserveForRecreation);

        albedoNabor.cleanup(reserveForRecreation);
        propertiesNabor.cleanup(reserveForRecreation);
    }

    public void transitionAlbedoImage(long commandPool, VkQueue graphicsQueue) {
        albedoNabor.transitionAlbedoResolveImage(commandPool, graphicsQueue);
    }

    public long getAlbedoImageView() {
        return albedoNabor.getAlbedoResolveImageView();
    }

    public void transitionDepthImage(long commandPool, VkQueue graphicsQueue) {
        propertiesNabor.transitionDepthImage(commandPool, graphicsQueue);
    }

    public long getDepthImageView() {
        return propertiesNabor.getDepthImageView();
    }

    public void transitionPositionImage(long commandPool, VkQueue graphicsQueue) {
        propertiesNabor.transitionPositionImage(commandPool, graphicsQueue);
    }

    public long getPositionImageView() {
        return propertiesNabor.getPositionImageView();
    }

    public void transitionNormalImage(long commandPool, VkQueue graphicsQueue) {
        propertiesNabor.transitionNormalImage(commandPool, graphicsQueue);
    }

    public long getNormalImageView() {
        return propertiesNabor.getNormalImageView();
    }

    @Override
    public VkExtent2D getExtent(int naborIndex) {
        VkExtent2D extent;

        switch (naborIndex) {
            case 0:
                extent = albedoNabor.getExtent();
                break;
            case 1:
                extent = propertiesNabor.getExtent();
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
                textureSampler = albedoNabor.getTextureSampler(arrayIndex);
                break;
            case 1:
                textureSampler = propertiesNabor.getTextureSampler(arrayIndex);
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
                uniformBufferMemory = albedoNabor.getUniformBufferMemory(arrayIndex);
                break;
            case 1:
                uniformBufferMemory = propertiesNabor.getUniformBufferMemory(arrayIndex);
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
                renderPass = albedoNabor.getRenderPass();
                break;
            case 1:
                renderPass = propertiesNabor.getRenderPass();
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
                pSets = albedoNabor.pDescriptorSets();
                break;
            case 1:
                pSets = propertiesNabor.pDescriptorSets();
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
                pipelineLayout = albedoNabor.getPipelineLayout(arrayIndex);
                break;
            case 1:
                pipelineLayout = propertiesNabor.getPipelineLayout(arrayIndex);
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
                graphicsPipeline = albedoNabor.getGraphicsPipeline(arrayIndex);
                break;
            case 1:
                graphicsPipeline = propertiesNabor.getGraphicsPipeline(arrayIndex);
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
                image = albedoNabor.getImage(arrayIndex);
                break;
            case 1:
                image = propertiesNabor.getImage(arrayIndex);
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
                imageView = albedoNabor.getImageView(arrayIndex);
                break;
            case 1:
                imageView = propertiesNabor.getImageView(arrayIndex);
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
                framebuffer = albedoNabor.getFramebuffer(arrayIndex);
                break;
            case 1:
                framebuffer = propertiesNabor.getFramebuffer(arrayIndex);
                break;
            default:
                throw new RuntimeException("Index out of bounds");
        }

        return framebuffer;
    }
}
