package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanInstanceForComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttComponent3D;
import org.joml.Vector3fc;

/**
 * Component 3D
 *
 * @author maeda6uiui
 */
public class MttComponent3D extends MttComponent {
    public MttComponent3D(IMttVulkanInstanceForComponent vulkanInstance) {
        super(vulkanInstance);
    }

    public boolean isCastShadow() {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        return vkComponent.isCastShadow();
    }

    public void setCastShadow(boolean castShadow) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.setCastShadow(castShadow);
    }

    public void translate(Vector3fc v) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.translate(v);
    }

    public void rotX(float ang) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.rotX(ang);
    }

    public void rotY(float ang) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.rotY(ang);
    }

    public void rotZ(float ang) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.rotZ(ang);
    }

    public void rot(float ang, Vector3fc axis) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.rot(ang, axis);
    }

    public void rescale(Vector3fc scale) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.rescale(scale);
    }
}
