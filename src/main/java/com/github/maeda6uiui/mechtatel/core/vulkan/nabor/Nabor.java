package com.github.maeda6uiui.mechtatel.core.vulkan.nabor;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
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

    private long renderPass;
    private long descriptorSetLayout;
    private List<Long> vertShaderModules;
    private List<Long> fragShaderModules;
    private long pipelineLayout;
    private long graphicsPipeline;

    protected void createRenderPass(int imageFormat, int msaaSamples) {

    }

    protected void createDescriptorSetLayout() {

    }

    protected void createGraphicsPipeline(int width, int height, int msaaSamples) {

    }

    public Nabor(VkDevice device, int imageFormat, int msaaSamples, int width, int height) {
        this.device = device;

        vertShaderModules = new ArrayList<>();
        fragShaderModules = new ArrayList<>();

        this.createRenderPass(imageFormat, msaaSamples);
        this.createDescriptorSetLayout();
        this.createGraphicsPipeline(width, height, msaaSamples);
    }

    public void cleanup(boolean reserveForRecreation) {
        vkDestroyPipeline(device, graphicsPipeline, null);
        vkDestroyPipelineLayout(device, pipelineLayout, null);

        if (!reserveForRecreation) {
            vertShaderModules.forEach(vertShaderModule -> vkDestroyShaderModule(device, vertShaderModule, null));
            fragShaderModules.forEach(fragShaderModule -> vkDestroyShaderModule(device, fragShaderModule, null));

            vertShaderModules.clear();
            fragShaderModules.clear();

            vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null);
        }

        vkDestroyRenderPass(device, renderPass, null);
    }

    public void recreate(int imageFormat, int msaaSamples, int width, int height) {
        this.cleanup(true);

        this.createRenderPass(imageFormat, msaaSamples);
        this.createGraphicsPipeline(width, height, msaaSamples);
    }

    protected VkDevice getDevice() {
        return device;
    }

    public long getRenderPass() {
        return renderPass;
    }

    protected void setRenderPass(long renderPass) {
        this.renderPass = renderPass;
    }

    public long getDescriptorSetLayout() {
        return descriptorSetLayout;
    }

    protected void setDescriptorSetLayout(long descriptorSetLayout) {
        this.descriptorSetLayout = descriptorSetLayout;
    }

    protected List<Long> getVertShaderModules() {
        return vertShaderModules;
    }

    protected List<Long> getFragShaderModules() {
        return fragShaderModules;
    }

    protected void setPipelineLayout(long pipelineLayout) {
        this.pipelineLayout = pipelineLayout;
    }

    public long getPipelineLayout() {
        return pipelineLayout;
    }

    public long getGraphicsPipeline() {
        return graphicsPipeline;
    }

    protected void setGraphicsPipeline(long graphicsPipeline) {
        this.graphicsPipeline = graphicsPipeline;
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
