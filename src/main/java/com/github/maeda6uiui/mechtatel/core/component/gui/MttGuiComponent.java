package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.Component;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;

/**
 * Base class for GUI components
 *
 * @author maeda6uiui
 */
public class MttGuiComponent extends Component {
    private float cursorX;
    private float cursorY;

    public MttGuiComponent(MttVulkanInstance vulkanInstance) {
        super(vulkanInstance);

        cursorX = 0.0f;
        cursorY = 0.0f;
    }

    public void updateCursorPos(int x, int y, int width, int height) {
        cursorX = ((float) x / (float) width - 0.5f) * 2.0f;
        cursorY = ((float) y / (float) height - 0.5f) * 2.0f;
    }

    protected float getCursorPosX() {
        return cursorX;
    }

    protected float getCursorPosY() {
        return cursorY;
    }

    public void updateState() {

    }
}
