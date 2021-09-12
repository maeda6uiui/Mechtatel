package com.github.maeda6uiui.mechtatel.core.vulkan.nabor;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

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

    private int texDstBinding;

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

    public Nabor(VkDevice device) {
        this.device = device;

        texDstBinding = 1;

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

    protected void createUniformBuffers(int descriptorCount) {

    }

    protected void createRenderPass(int imageFormat, int msaaSamples) {

    }

    protected void createDescriptorSetLayouts() {

    }

    protected void createDescriptorPools(int descriptorCount) {

    }

    protected void createDescriptorSets(int descriptorCount) {

    }

    protected void createGraphicsPipelines(VkExtent2D extent, int msaaSamples) {

    }

    protected void createImages(
            long commandPool,
            VkQueue graphicsQueue,
            VkExtent2D extent,
            int msaaSamples,
            int imageFormat) {

    }

    protected void createFramebuffers(VkExtent2D extent) {

    }

    public void compile(
            int imageFormat,
            int msaaSamples,
            VkExtent2D extent,
            long commandPool,
            VkQueue graphicsQueue) {
        this.createUniformBuffers(1);
        this.createRenderPass(imageFormat, msaaSamples);
        this.createDescriptorSetLayouts();
        this.createDescriptorPools(1);
        this.createDescriptorSets(1);
        this.createGraphicsPipelines(extent, msaaSamples);
        this.createImages(commandPool, graphicsQueue, extent, msaaSamples, imageFormat);
        this.createFramebuffers(extent);
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

            uniformBuffers.forEach(uniformBuffer -> vkDestroyBuffer(device, uniformBuffer, null));
            uniformBufferMemories.forEach(uniformBufferMemory -> vkFreeMemory(device, uniformBufferMemory, null));

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
            int imageFormat,
            int msaaSamples,
            VkExtent2D extent,
            long commandPool,
            VkQueue graphicsQueue) {
        this.cleanup(true);

        this.createRenderPass(imageFormat, msaaSamples);
        this.createGraphicsPipelines(extent, msaaSamples);
        this.createGraphicsPipelines(extent, msaaSamples);
        this.createImages(commandPool, graphicsQueue, extent, msaaSamples, imageFormat);
        this.createFramebuffers(extent);
    }

    protected VkDevice getDevice() {
        return device;
    }

    public int getTexDstBinding() {
        return texDstBinding;
    }

    protected void setTexDstBinding(int texDstBinding) {
        this.texDstBinding = texDstBinding;
    }

    protected List<Long> getUniformBuffers() {
        return uniformBuffers;
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

    protected List<Long> getDescriptorPools() {
        return descriptorPools;
    }

    protected List<Long> getDescriptorSets() {
        return descriptorSets;
    }

    protected List<Long> getVertShaderModules() {
        return vertShaderModules;
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

    protected List<Long> getImages() {
        return images;
    }

    protected List<Long> getImageMemories() {
        return imageMemories;
    }

    protected List<Long> getImageViews() {
        return imageViews;
    }

    protected List<Long> getFramebuffers() {
        return framebuffers;
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
