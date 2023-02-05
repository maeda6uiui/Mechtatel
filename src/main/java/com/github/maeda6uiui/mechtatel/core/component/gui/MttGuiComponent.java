package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.Component;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;

/**
 * Base class for GUI components
 *
 * @author maeda6uiui
 */
public class MttGuiComponent extends Component {
    public MttGuiComponent(MttVulkanInstance vulkanInstance) {
        super(vulkanInstance);
    }
}
