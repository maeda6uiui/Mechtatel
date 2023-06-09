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

    public MttComponent3D translate(Vector3fc v) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.translate(v);

        position.add(v);

        return this;
    }

    public MttComponent3D rotX(float ang) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.rotX(ang);

        position.rotateX(ang);

        return this;
    }

    public MttComponent3D rotY(float ang) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.rotY(ang);

        position.rotateY(ang);

        return this;
    }

    public MttComponent3D rotZ(float ang) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.rotZ(ang);

        position.rotateZ(ang);

        return this;
    }

    public MttComponent3D rot(float ang, Vector3fc axis) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.rot(ang, axis);

        position.rotateAxis(ang, axis.x(), axis.y(), axis.z());

        return this;
    }

    public MttComponent3D rescale(Vector3fc scale) {
        VkMttComponent3D vkComponent = (VkMttComponent3D) this.getVulkanComponent();
        vkComponent.rescale(scale);

        this.scale.mul(scale);
        position.mul(scale);

        return this;
    }

    @Override
    public void reset() {
        super.reset();

        position = new Vector3f(0.0f, 0.0f, 0.0f);
        scale = new Vector3f(1.0f, 1.0f, 1.0f);
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
