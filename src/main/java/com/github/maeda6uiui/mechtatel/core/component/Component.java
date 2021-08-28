package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.util.UniversalCounter;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanInstanceForComponent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/**
 * Component
 *
 * @author maeda
 */
public class Component {
    private String tag;
    private Matrix4f mat;
    private boolean visible;

    private IMttVulkanInstanceForComponent vulkanInstance;

    private void setDefaultValues() {
        tag = "Component_" + UniversalCounter.get();
        mat = new Matrix4f().identity();
        visible = true;
    }

    //Vulkan
    public Component(IMttVulkanInstanceForComponent vulkanInstance) {
        this.setDefaultValues();
        this.vulkanInstance = vulkanInstance;
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

    protected Matrix4f getMatRef() {
        return mat;
    }

    public void setMat(Matrix4fc mat) {
        this.mat = new Matrix4f(mat);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return visible;
    }

    public void applyMat(Matrix4fc right) {
        this.mat.mul(right);
    }

    public void reset() {
        this.mat.invert();
    }

    protected IMttVulkanInstanceForComponent getVulkanInstance() {
        return vulkanInstance;
    }

    public void cleanup() {

    }
}
