package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.util.UniversalCounter;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanInstanceForComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttComponent;
import org.joml.Matrix4fc;

/**
 * Component
 *
 * @author maeda6uiui
 */
public class MttComponent {
    private String tag;

    private IMttVulkanInstanceForComponent vulkanInstance;
    private VkMttComponent vkComponent;

    private void setDefaultValues() {
        tag = "Component_" + UniversalCounter.get();
    }

    //Vulkan
    public MttComponent(IMttVulkanInstanceForComponent vulkanInstance) {
        this.setDefaultValues();
        this.vulkanInstance = vulkanInstance;
    }

    protected void associateVulkanComponent(VkMttComponent vkComponent) {
        this.vkComponent = vkComponent;
    }

    protected VkMttComponent getVulkanComponent() {
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

    public void setVisible(boolean visible) {
        vkComponent.setVisible(visible);
    }

    public boolean isVisible() {
        return vkComponent.isVisible();
    }

    public int getDrawOrder() {
        return vkComponent.getDrawOrder();
    }

    public void setDrawOrder(int drawOrder) {
        vkComponent.setDrawOrder(drawOrder);
    }

    public boolean isTwoDComponent() {
        return vkComponent.isTwoDComponent();
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