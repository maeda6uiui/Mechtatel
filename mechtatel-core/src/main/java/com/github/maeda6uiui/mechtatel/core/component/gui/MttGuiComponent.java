package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttComponent;
import com.github.maeda6uiui.mechtatel.core.util.UniversalCounter;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;

import java.util.Map;

/**
 * Base class for GUI components
 *
 * @author maeda6uiui
 */
public class MttGuiComponent extends MttComponent {
    private static int focusedGUIComponentID = -1;

    private int guiComponentID;

    private float x;
    private float y;
    private float width;
    private float height;

    private MttGuiComponentCallbacks callbacks;
    private boolean cursorOn;

    public MttGuiComponent(
            MttVulkanImpl vulkanImpl,
            float x,
            float y,
            float width,
            float height) {
        super(vulkanImpl);

        guiComponentID = UniversalCounter.get();
        focusedGUIComponentID = guiComponentID;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        callbacks = new MttGuiComponentCallbacks();
        cursorOn = false;
    }

    public void setCallbacks(MttGuiComponentCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    protected float getX() {
        return x;
    }

    protected float getY() {
        return y;
    }

    protected float getWidth() {
        return width;
    }

    protected float getHeight() {
        return height;
    }

    public boolean isCursorOn() {
        return cursorOn;
    }

    public boolean isFocused() {
        return focusedGUIComponentID == guiComponentID;
    }

    public void update(
            int cursorX,
            int cursorY,
            int windowWidth,
            int windowHeight,
            int lButtonPressingCount,
            int mButtonPressingCount,
            int rButtonPressingCount,
            Map<String, Integer> keyboardPressingCounts) {
        float fCursorX = (float) cursorX / (float) windowWidth * 2.0f - 1.0f;
        float fCursorY = (float) cursorY / (float) windowHeight * 2.0f - 1.0f;

        if ((x < fCursorX && fCursorX < x + width)
                && (y < fCursorY && fCursorY < y + height)) {
            cursorOn = true;

            if (lButtonPressingCount == 1) {
                callbacks.onLButtonDown();
            }
            if (mButtonPressingCount == 1) {
                callbacks.onMButtonDown();
            }
            if (rButtonPressingCount == 1) {
                callbacks.onRButtonDown();
            }
        } else {
            cursorOn = false;
        }

        if (cursorOn && lButtonPressingCount == 1) {
            focusedGUIComponentID = guiComponentID;
        }
        if (focusedGUIComponentID == guiComponentID) {
            if (keyboardPressingCounts.get("ESCAPE") == 1) {
                focusedGUIComponentID = -1;
            }
        }
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
