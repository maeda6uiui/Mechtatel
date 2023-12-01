package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.util.UniversalCounter;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplForComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttComponent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

/**
 * Component
 *
 * @author maeda6uiui
 */
public class MttComponent implements IMttComponentForVkMttComponent {
    private String tag;
    private Matrix4f mat;
    private boolean visible;
    private boolean twoDComponent;
    private boolean castShadow;
    private int drawOrder;

    private IMttVulkanImplForComponent vulkanImpl;
    private VkMttComponent vkComponent;

    private void initialize() {
        tag = "Component_" + UniversalCounter.get();
        mat = new Matrix4f().identity();
        visible = true;
        twoDComponent = false;
        castShadow = false;
        drawOrder = 0;
    }

    public MttComponent(IMttVulkanImplForComponent vulkanImpl) {
        this.initialize();
        this.vulkanImpl = vulkanImpl;
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
        return mat;
    }

    public void setMat(Matrix4f mat) {
        this.mat = mat;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getDrawOrder() {
        return drawOrder;
    }

    public void setDrawOrder(int drawOrder) {
        this.drawOrder = drawOrder;
    }

    public boolean isTwoDComponent() {
        return twoDComponent;
    }

    public void applyMat(Matrix4fc right) {
        mat = mat.mul(right);
    }

    public void reset() {
        mat = new Matrix4f().identity();
    }

    public boolean shouldCastShadow() {
        return castShadow;
    }

    public void setCastShadow(boolean castShadow) {
        this.castShadow = castShadow;
    }

    public MttComponent translate(Vector3fc v) {
        mat = mat.translate(v);
        return this;
    }

    public MttComponent rotX(float ang) {
        mat = mat.rotateX(ang);
        return this;
    }

    public MttComponent rotY(float ang) {
        mat = mat.rotateY(ang);
        return this;
    }

    public MttComponent rotZ(float ang) {
        mat = mat.rotateZ(ang);
        return this;
    }

    public MttComponent rot(float ang, Vector3fc axis) {
        mat = mat.rotate(ang, axis);
        return this;
    }

    public MttComponent rescale(Vector3fc scale) {
        mat = mat.scale(scale);
        return this;
    }

    public void cleanup() {
        vulkanImpl.removeComponent(vkComponent);
    }
}
