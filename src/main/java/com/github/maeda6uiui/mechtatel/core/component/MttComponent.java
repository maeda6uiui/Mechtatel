package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.util.UniversalCounter;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanInstanceForComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttComponent;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Component
 *
 * @author maeda6uiui
 */
public class MttComponent {
    private String tag;

    private IMttVulkanInstanceForComponent vulkanInstance;
    private VkMttComponent vkComponent;

    private Vector3f scale;

    private void setDefaultValues() {
        tag = "Component_" + UniversalCounter.get();
    }

    //Vulkan
    public MttComponent(IMttVulkanInstanceForComponent vulkanInstance) {
        this.setDefaultValues();
        this.vulkanInstance = vulkanInstance;

        scale = new Vector3f(1.0f, 1.0f, 1.0f);
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

    public boolean shouldCastShadow() {
        return vkComponent.shouldCastShadow();
    }

    public void setCastShadow(boolean castShadow) {
        vkComponent.setCastShadow(castShadow);
    }

    public MttComponent translate(Vector3fc v) {
        vkComponent.translate(v);
        return this;
    }

    public MttComponent rotX(float ang) {
        vkComponent.rotX(ang);
        return this;
    }

    public MttComponent rotY(float ang) {
        vkComponent.rotY(ang);
        return this;
    }

    public MttComponent rotZ(float ang) {
        vkComponent.rotZ(ang);
        return this;
    }

    public MttComponent rot(float ang, Vector3fc axis) {
        vkComponent.rot(ang, axis);
        return this;
    }

    public MttComponent rescale(Vector3fc scale) {
        vkComponent.rescale(scale);
        this.scale.mul(scale);

        return this;
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

    protected IMttVulkanInstanceForComponent getVulkanInstance() {
        return vulkanInstance;
    }

    public void cleanup() {
        vulkanInstance.removeComponent(vkComponent);
    }
}
