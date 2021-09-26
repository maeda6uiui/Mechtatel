package com.github.maeda6uiui.mechtatel.core.vulkan.nabor;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Set of necessary objects for rendering with Vulkan
 *
 * @author maeda
 */
public class Nabor {
    private VkDevice device;

    private int msaaSamples;
    private VkExtent2D extent;

    private List<Long> textureSamplers;

    private List<Long> uniformBuffers;
    private List<Long> uniformBufferMemories;

    private long renderPass;
    private List<Long> descriptorSetLayouts;
    private List<Long> descriptorPools;
    private List<Long> descriptorSets;
    private List<Long> vertShaderModules;
    private List<Long> fragShaderModules;
    private List<Long> pipelineLayouts;
    private List<Long> graphicsPipelines;

    private List<Long> images;
    private List<Long> imageMemories;
    private List<Long> imageViews;
    private List<Long> framebuffers;

    private int setCount;

    public Nabor(VkDevice device, int msaaSamples) {
        this.device = device;

        this.msaaSamples = msaaSamples;

        textureSamplers = new ArrayList<>();

        uniformBuffers = new ArrayList<>();
        uniformBufferMemories = new ArrayList<>();

        descriptorSetLayouts = new ArrayList<>();
        descriptorPools = new ArrayList<>();
        descriptorSets = new ArrayList<>();
        vertShaderModules = new ArrayList<>();
        fragShaderModules = new ArrayList<>();
        pipelineLayouts = new ArrayList<>();
        graphicsPipelines = new ArrayList<>();

        images = new ArrayList<>();
        imageMemories = new ArrayList<>();
        imageViews = new ArrayList<>();
        framebuffers = new ArrayList<>();
    }

    protected void createTextureSamplers() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.callocStack(stack);
            samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerInfo.magFilter(VK_FILTER_LINEAR);
            samplerInfo.minFilter(VK_FILTER_LINEAR);
            samplerInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.anisotropyEnable(true);
            samplerInfo.maxAnisotropy(16.0f);
            samplerInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerInfo.unnormalizedCoordinates(false);
            samplerInfo.compareEnable(false);
            samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
            samplerInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerInfo.minLod(0.0f);
            samplerInfo.maxLod(8.0f);
            samplerInfo.mipLodBias(0.0f);

            LongBuffer pTextureSampler = stack.mallocLong(1);
            if (vkCreateSampler(device, samplerInfo, null, pTextureSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a texture sampler");
            }

            long textureSampler = pTextureSampler.get(0);
            textureSamplers.add(textureSampler);
        }
    }

    protected void createUniformBuffers(int descriptorCount) {

    }

    protected void createRenderPass(int colorImageFormat) {

    }

    protected void createDescriptorSetLayouts() {

    }

    protected void createDescriptorPools(int descriptorCount) {

    }

    protected void createDescriptorSets(int descriptorCount, long commandPool, VkQueue graphicsQueue) {

    }

    protected void createGraphicsPipelines() {

    }

    protected void createImages(
            long commandPool,
            VkQueue graphicsQueue,
            int colorImageFormat) {

    }

    protected void createFramebuffers() {

    }

    public void compile(
            int colorImageFormat,
            VkExtent2D extent,
            long commandPool,
            VkQueue graphicsQueue,
            int descriptorCount) {
        this.extent = extent;

        this.createTextureSamplers();
        this.createUniformBuffers(descriptorCount);
        this.createRenderPass(colorImageFormat);
        this.createDescriptorSetLayouts();
        this.createDescriptorPools(descriptorCount);
        this.createDescriptorSets(descriptorCount, commandPool, graphicsQueue);
        this.createGraphicsPipelines();
        this.createImages(commandPool, graphicsQueue, colorImageFormat);
        this.createFramebuffers();
    }

    public void cleanup(boolean reserveForRecreation) {
        graphicsPipelines.forEach(graphicsPipeline -> vkDestroyPipeline(device, graphicsPipeline, null));
        pipelineLayouts.forEach(pipelineLayout -> vkDestroyPipelineLayout(device, pipelineLayout, null));
        graphicsPipelines.clear();
        pipelineLayouts.clear();

        images.forEach(image -> vkDestroyImage(device, image, null));
        imageMemories.forEach(imageMemory -> vkFreeMemory(device, imageMemory, null));
        imageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));
        framebuffers.forEach(framebuffer -> vkDestroyFramebuffer(device, framebuffer, null));
        images.clear();
        imageMemories.clear();
        imageViews.clear();
        framebuffers.clear();

        if (!reserveForRecreation) {
            vertShaderModules.forEach(vertShaderModule -> vkDestroyShaderModule(device, vertShaderModule, null));
            fragShaderModules.forEach(fragShaderModule -> vkDestroyShaderModule(device, fragShaderModule, null));
            vertShaderModules.clear();
            fragShaderModules.clear();

            textureSamplers.forEach(textureSampler -> vkDestroySampler(device, textureSampler, null));
            textureSamplers.clear();

            uniformBuffers.forEach(uniformBuffer -> vkDestroyBuffer(device, uniformBuffer, null));
            uniformBufferMemories.forEach(uniformBufferMemory -> vkFreeMemory(device, uniformBufferMemory, null));
            uniformBuffers.clear();
            uniformBufferMemories.clear();

            descriptorSetLayouts.forEach(
                    descriptorSetLayout -> vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null));
            descriptorSetLayouts.clear();

            descriptorPools.forEach(descriptorPool -> vkDestroyDescriptorPool(device, descriptorPool, null));
            descriptorPools.clear();

            descriptorSets.clear();
        }

        vkDestroyRenderPass(device, renderPass, null);
    }

    public void recreate(
            int colorImageFormat,
            VkExtent2D extent,
            long commandPool,
            VkQueue graphicsQueue) {
        this.extent = extent;

        this.cleanup(true);

        this.createRenderPass(colorImageFormat);
        this.createGraphicsPipelines();
        this.createImages(commandPool, graphicsQueue, colorImageFormat);
        this.createFramebuffers();
    }

    protected VkDevice getDevice() {
        return device;
    }

    public int getMsaaSamples() {
        return msaaSamples;
    }

    public VkExtent2D getExtent() {
        return extent;
    }

    public long getTextureSampler(int index) {
        return textureSamplers.get(index);
    }

    protected List<Long> getTextureSamplers() {
        return textureSamplers;
    }

    public long getUniformBuffer(int index) {
        return uniformBuffers.get(index);
    }

    protected List<Long> getUniformBuffers() {
        return uniformBuffers;
    }

    public long getUniformBufferMemory(int index) {
        return uniformBufferMemories.get(index);
    }

    protected List<Long> getUniformBufferMemories() {
        return uniformBufferMemories;
    }

    public long getRenderPass() {
        return renderPass;
    }

    protected void setRenderPass(long renderPass) {
        this.renderPass = renderPass;
    }

    public long getDescriptorSetLayout(int index) {
        return descriptorSetLayouts.get(index);
    }

    protected List<Long> getDescriptorSetLayouts() {
        return descriptorSetLayouts;
    }

    protected LongBuffer pDescriptorSetLayouts() {
        LongBuffer pLayouts = MemoryStack.stackGet().mallocLong(descriptorSetLayouts.size());
        for (int i = 0; i < descriptorSetLayouts.size(); i++) {
            pLayouts.put(i, descriptorSetLayouts.get(i));
        }

        return pLayouts;
    }

    public long getDescriptorPool(int index) {
        return descriptorPools.get(index);
    }

    protected List<Long> getDescriptorPools() {
        return descriptorPools;
    }

    public int getNumDescriptorSets() {
        return descriptorSets.size();
    }

    public long getDescriptorSet(int index) {
        return descriptorSets.get(index);
    }

    protected List<Long> getDescriptorSets() {
        return descriptorSets;
    }

    public LongBuffer pDescriptorSets() {
        LongBuffer pSets = MemoryStack.stackGet().mallocLong(descriptorSets.size());
        for (int i = 0; i < descriptorSets.size(); i++) {
            pSets.put(i, descriptorSets.get(i));
        }

        return pSets;
    }

    public long getVertShaderModule(int index) {
        return vertShaderModules.get(index);
    }

    protected List<Long> getVertShaderModules() {
        return vertShaderModules;
    }

    public long getFragShaderModule(int index) {
        return fragShaderModules.get(index);
    }

    protected List<Long> getFragShaderModules() {
        return fragShaderModules;
    }

    public long getPipelineLayout(int index) {
        return pipelineLayouts.get(index);
    }

    protected List<Long> getPipelineLayouts() {
        return pipelineLayouts;
    }

    public long getGraphicsPipeline(int index) {
        return graphicsPipelines.get(index);
    }

    protected List<Long> getGraphicsPipelines() {
        return graphicsPipelines;
    }

    public long getImage(int index) {
        return images.get(index);
    }

    protected List<Long> getImages() {
        return images;
    }

    public long getImageMemory(int index) {
        return imageMemories.get(index);
    }

    protected List<Long> getImageMemories() {
        return imageMemories;
    }

    public long getImageView(int index) {
        return imageViews.get(index);
    }

    protected List<Long> getImageViews() {
        return imageViews;
    }

    public int getNumFramebuffers() {
        return framebuffers.size();
    }

    public long getFramebuffer(int index) {
        return framebuffers.get(index);
    }

    protected List<Long> getFramebuffers() {
        return framebuffers;
    }

    public int getSetCount() {
        return setCount;
    }

    protected void setSetCount(int setCount) {
        this.setCount = setCount;
    }

    protected long createShaderModule(VkDevice device, ByteBuffer spirvCode) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.callocStack(stack);
            createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            createInfo.pCode(spirvCode);

            LongBuffer pShaderModule = stack.mallocLong(1);
            if (vkCreateShaderModule(device, createInfo, null, pShaderModule) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a shader module");
            }

            return pShaderModule.get(0);
        }
    }
}
