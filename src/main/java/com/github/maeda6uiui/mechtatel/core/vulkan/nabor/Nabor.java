package com.github.maeda6uiui.mechtatel.core.vulkan.nabor;

import com.github.maeda6uiui.mechtatel.core.vulkan.creator.ColorResourceCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.FramebufferCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.DepthResourceUtils;
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

    private long renderPass;
    private List<Long> descriptorSetLayouts;
    private List<Long> vertShaderModules;
    private List<Long> fragShaderModules;
    private List<Long> pipelineLayouts;
    private List<Long> graphicsPipelines;

    private long colorImage;
    private long colorImageMemory;
    private long colorImageView;

    private long depthImage;
    private long depthImageMemory;
    private long depthImageView;

    private long framebuffer;

    public Nabor(VkDevice device) {
        this.device = device;

        texDstBinding = 1;

        descriptorSetLayouts = new ArrayList<>();
        vertShaderModules = new ArrayList<>();
        fragShaderModules = new ArrayList<>();
        pipelineLayouts = new ArrayList<>();
        graphicsPipelines = new ArrayList<>();
    }

    protected void createRenderPass(int imageFormat, int msaaSamples) {

    }

    protected void createDescriptorSetLayouts() {

    }

    protected void createGraphicsPipelines(VkExtent2D extent, int msaaSamples) {

    }

    protected void createColorImageAndImageView(
            long commandPool,
            VkQueue graphicsQueue,
            VkExtent2D extent,
            int msaaSamples,
            int imageFormat) {
        ColorResourceCreator.ColorResources resources = ColorResourceCreator.createColorResources(
                device,
                commandPool,
                graphicsQueue,
                extent,
                msaaSamples,
                imageFormat);
        colorImage = resources.colorImage;
        colorImageMemory = resources.colorImageMemory;
        colorImageView = resources.colorImageView;
    }

    protected void createDepthImageAndImageViews(
            long commandPool,
            VkQueue graphicsQueue,
            VkExtent2D extent,
            int msaaSamples) {
        DepthResourceUtils.DepthResources resources = DepthResourceUtils.createDepthResources(
                device,
                commandPool,
                graphicsQueue,
                extent,
                msaaSamples);
        depthImage = resources.depthImage;
        depthImageMemory = resources.depthImageMemory;
        depthImageView = resources.depthImageView;
    }

    protected void createFramebuffer(VkExtent2D extent) {
        framebuffer = FramebufferCreator.createFramebuffer(device, colorImageView, depthImageView, renderPass, extent);
    }

    public void compile(
            long commandPool,
            VkQueue graphicsQueue,
            int imageFormat,
            int msaaSamples,
            VkExtent2D extent) {
        this.createRenderPass(imageFormat, msaaSamples);
        this.createDescriptorSetLayouts();
        this.createGraphicsPipelines(extent, msaaSamples);
        this.createColorImageAndImageView(commandPool, graphicsQueue, extent, msaaSamples, imageFormat);
        this.createDepthImageAndImageViews(commandPool, graphicsQueue, extent, msaaSamples);
        this.createFramebuffer(extent);
    }

    public void cleanup(boolean reserveForRecreation) {
        vkDestroyFramebuffer(device, framebuffer, null);

        vkDestroyImageView(device, depthImageView, null);
        vkDestroyImage(device, depthImage, null);
        vkFreeMemory(device, depthImageMemory, null);

        vkDestroyImageView(device, colorImageView, null);
        vkDestroyImage(device, colorImage, null);
        vkFreeMemory(device, colorImageMemory, null);

        graphicsPipelines.forEach(graphicsPipeline -> vkDestroyPipeline(device, graphicsPipeline, null));
        pipelineLayouts.forEach(pipelineLayout -> vkDestroyPipelineLayout(device, pipelineLayout, null));
        graphicsPipelines.clear();
        pipelineLayouts.clear();

        if (!reserveForRecreation) {
            vertShaderModules.forEach(vertShaderModule -> vkDestroyShaderModule(device, vertShaderModule, null));
            fragShaderModules.forEach(fragShaderModule -> vkDestroyShaderModule(device, fragShaderModule, null));
            vertShaderModules.clear();
            fragShaderModules.clear();

            descriptorSetLayouts.forEach(
                    descriptorSetLayout -> vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null));
            descriptorSetLayouts.clear();
        }

        vkDestroyRenderPass(device, renderPass, null);
    }

    public void recreate(
            long commandPool,
            VkQueue graphicsQueue,
            int imageFormat,
            int msaaSamples,
            VkExtent2D extent) {
        this.cleanup(true);

        this.createRenderPass(imageFormat, msaaSamples);
        this.createGraphicsPipelines(extent, msaaSamples);
        this.createColorImageAndImageView(commandPool, graphicsQueue, extent, msaaSamples, imageFormat);
        this.createDepthImageAndImageViews(commandPool, graphicsQueue, extent, msaaSamples);
        this.createFramebuffer(extent);
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

    protected void setDescriptorSetLayouts(List<Long> descriptorSetLayouts) {
        this.descriptorSetLayouts = descriptorSetLayouts;
    }

    protected List<Long> getVertShaderModules() {
        return vertShaderModules;
    }

    protected void setVertShaderModules(List<Long> vertShaderModules) {
        this.vertShaderModules = vertShaderModules;
    }

    protected List<Long> getFragShaderModules() {
        return fragShaderModules;
    }

    protected void setFragShaderModules(List<Long> fragShaderModules) {
        this.fragShaderModules = fragShaderModules;
    }

    public long getPipelineLayout(int index) {
        return pipelineLayouts.get(index);
    }

    protected List<Long> getPipelineLayouts() {
        return pipelineLayouts;
    }

    protected void setPipelineLayouts(List<Long> pipelineLayouts) {
        this.pipelineLayouts = pipelineLayouts;
    }

    public int getNumPipelines() {
        return graphicsPipelines.size();
    }

    public long getGraphicsPipeline(int index) {
        return graphicsPipelines.get(index);
    }

    protected List<Long> getGraphicsPipelines() {
        return graphicsPipelines;
    }

    protected void setGraphicsPipelines(List<Long> graphicsPipelines) {
        this.graphicsPipelines = graphicsPipelines;
    }

    protected long getFramebuffer() {
        return framebuffer;
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
