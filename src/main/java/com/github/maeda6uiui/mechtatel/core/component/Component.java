package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.util.UniversalCounter;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanInstanceForComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkComponent;
import org.joml.Matrix4fc;

/**
 * Component
 *
 * @author maeda
 */
public class Component {
    private String tag;

    private IMttVulkanInstanceForComponent vulkanInstance;
    private VkComponent vkComponent;

    private void setDefaultValues() {
        tag = "Component_" + UniversalCounter.get();
    }

    //Vulkan
    public Component(IMttVulkanInstanceForComponent vulkanInstance) {
        this.setDefaultValues();
        this.vulkanInstance = vulkanInstance;
    }

    protected void associateVulkanComponent(VkComponent vkComponent) {
        this.vkComponent = vkComponent;
    }

    protected VkComponent getVulkanComponent() {
        return vkComponent;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Matrix4fc getMat() {
        return vkComponent.getMat();
    }

    public void setMat(Matrix4fc mat) {
        vkComponent.setMat(mat);
    }

    public void applyMat(Matrix4fc right) {
        vkComponent.applyMat(right);
    }

    public void reset() {
        vkComponent.reset();
    }

    protected IMttVulkanInstanceForComponent getVulkanInstance() {
        return vulkanInstance;
    }

    public void cleanup() {
        vulkanInstance.removeComponent(vkComponent);
    }
}
