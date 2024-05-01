package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.component.IMttComponentForVkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.IVkMttScreenForVkMttComponent;
import org.joml.Matrix4fc;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;

import java.util.List;

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

    public boolean isValid() {
        return mttComponent.isValid();
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

    /**
     * This method is meant to be used for destroying and freeing one-time resources
     * after commands are submitted to a graphics queue.
     */
    public void cleanupLocally() {

    }

    /**
     * Updates UBOs peculiar to this component.
     *
     * @param device                Logical device
     * @param uniformBufferMemories List of uniform buffer memories
     */
    public void updateUBOs(VkDevice device, List<Long> uniformBufferMemories) {

    }

    public void cleanup() {

    }
}
