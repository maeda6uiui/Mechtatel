package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanInstanceForComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttComponent3D;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Component 3D
 *
 * @author maeda6uiui
 */
public class MttComponent3D extends MttComponent {
    private Vector3f position;
    private Vector3f scale;

    public MttComponent3D(IMttVulkanInstanceForComponent vulkanInstance) {
        super(vulkanInstance);

        position = new Vector3f(0.0f, 0.0f, 0.0f);
        scale = new Vector3f(1.0f, 1.0f, 1.0f);
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

        position.add(v);
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

        this.scale.mul(scale);
    }

    /**
     * Returns the model position.
     * Returned position is only valid if all translation is done via {@link #translate(Vector3fc)}.
     *
     * @return Position of the model
     */
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    /**
     * Returns the model scale.
     * Returned scale is only valid if all rescaling is done via {@link #rescale(Vector3fc)}.
     *
     * @return Scale of the model
     */
    public Vector3f getScale() {
        return new Vector3f(scale);
    }
}
