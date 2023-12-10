package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Optional;

/**
 * Component
 *
 * @author maeda6uiui
 */
public class MttComponent implements IMttComponentForVkMttComponent, Comparable<MttComponent> {
    public static class MttComponentCreateInfo {
        public boolean visible;
        public boolean twoDComponent;
        public boolean castShadow;
        public int drawOrder;

        public MttComponentCreateInfo() {
            visible = true;
            twoDComponent = false;
            castShadow = false;
            drawOrder = 0;
        }

        public MttComponentCreateInfo setVisible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public MttComponentCreateInfo setTwoDComponent(boolean twoDComponent) {
            this.twoDComponent = twoDComponent;
            return this;
        }

        public MttComponentCreateInfo setCastShadow(boolean castShadow) {
            this.castShadow = castShadow;
            return this;
        }

        public MttComponentCreateInfo setDrawOrder(int drawOrder) {
            this.drawOrder = drawOrder;
            return this;
        }
    }

    private Matrix4f mat;
    private boolean visible;
    private boolean twoDComponent;
    private boolean castShadow;
    private int drawOrder;

    private VkMttComponent vkComponent;

    private boolean valid;

    private void setInitialProperties(MttComponentCreateInfo createInfo) {
        mat = new Matrix4f().identity();
        visible = createInfo.visible;
        twoDComponent = createInfo.twoDComponent;
        castShadow = createInfo.castShadow;
        drawOrder = createInfo.drawOrder;
    }

    public MttComponent(IMttScreenForMttComponent screen, MttComponentCreateInfo createInfo) {
        this.setInitialProperties(createInfo);
        screen.addComponent(this);

        valid = true;
    }

    public void cleanup() {
        if (valid && vkComponent != null) {
            vkComponent.cleanup();
        }
        valid = false;
    }

    @Override
    public int compareTo(MttComponent that) {
        return Integer.compare(this.drawOrder, that.drawOrder);
    }

    protected void associateVulkanComponent(VkMttComponent vkComponent) {
        this.vkComponent = vkComponent;
    }

    public Optional<VkMttComponent> getVulkanComponent() {
        return Optional.ofNullable(vkComponent);
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public Matrix4fc getMat() {
        return mat;
    }

    public void setMat(Matrix4f mat) {
        this.mat = mat;
    }

    public void applyMat(Matrix4fc right) {
        mat = mat.mul(right);
    }

    public void reset() {
        mat = new Matrix4f().identity();
    }

    public Vector3f getMatScale() {
        return mat.getScale(new Vector3f());
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public int getDrawOrder() {
        return drawOrder;
    }

    public void setDrawOrder(int drawOrder) {
        this.drawOrder = drawOrder;
    }

    @Override
    public boolean isTwoDComponent() {
        return twoDComponent;
    }

    @Override
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
}
