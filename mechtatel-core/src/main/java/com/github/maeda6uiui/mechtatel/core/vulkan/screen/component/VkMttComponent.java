package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.component.IMttComponentForVkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.IVkMttScreenForVkMttComponent;
import org.joml.Matrix4fc;
import org.lwjgl.vulkan.VkCommandBuffer;

/**
 * Vulkan implementation of component
 *
 * @author maeda6uiui
 */
public class VkMttComponent {
    private IMttComponentForVkMttComponent mttComponent;
    private IVkMttScreenForVkMttComponent screen;
    private String naborName;

    public VkMttComponent(
            IMttComponentForVkMttComponent mttComponent,
            IVkMttScreenForVkMttComponent screen,
            String naborName) {
        this.mttComponent = mttComponent;
        this.screen = screen;
        this.naborName = naborName;
    }

    public IVkMttScreenForVkMttComponent getScreen() {
        return screen;
    }

    public String getNaborName() {
        return naborName;
    }

    public Matrix4fc getMat() {
        return mttComponent.getMat();
    }

    public boolean isVisible() {
        return mttComponent.isVisible();
    }

    public int getDrawOrder() {
        return mttComponent.getDrawOrder();
    }

    public boolean isTwoDComponent() {
        return mttComponent.isTwoDComponent();
    }

    public boolean shouldCastShadow() {
        return mttComponent.shouldCastShadow();
    }

    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {

    }

    public void transfer(VkCommandBuffer commandBuffer) {

    }

    public void cleanup() {

    }
}
