package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttFont;

import java.awt.*;

/**
 * Font
 *
 * @author maeda
 */
public class MttFont extends Component {
    private VkMttFont vkMttFont;

    public MttFont(
            MttVulkanInstance vulkanInstance,
            Font font,
            boolean antiAlias,
            Color color) {
        super(vulkanInstance);

        vkMttFont = vulkanInstance.createMttFont(font, antiAlias, color);
        this.associateVulkanComponent(vkMttFont);
    }
}
