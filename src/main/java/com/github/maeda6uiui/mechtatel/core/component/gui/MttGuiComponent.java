package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.Component;
import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector2f;

import java.awt.*;

/**
 * Base class for GUI components
 *
 * @author maeda6uiui
 */
public class MttGuiComponent extends Component {
    private float x;
    private float y;
    private float width;
    private float height;
    private MttFont font;

    public MttGuiComponent(
            MttVulkanInstance vulkanInstance,
            float x,
            float y,
            float width,
            float height,
            String text,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor) {
        super(vulkanInstance);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        font = new MttFont(vulkanInstance, new Font(fontName, fontStyle, fontSize), true, fontColor, text);
        font.prepare(text, new Vector2f(x, y));
        font.createBuffers();
    }

    public void update(
            int cursorX,
            int cursorY,
            int windowWidth,
            int windowHeight,
            int lButtonPressingCount,
            int mButtonPressingCount,
            int rButtonPressingCount) {
        float fCursorX = (float) cursorX / (float) windowWidth * 2.0f - 1.0f;
        float fCursorY = (float) cursorY / (float) windowHeight * 2.0f - 1.0f;

        if ((x < fCursorX && fCursorX < x + width)
                && (y < fCursorY && fCursorY < y + height)) {
            this.onCursorOnComponent();

            if (lButtonPressingCount == 1) {
                this.onLButtonDown();
            }
            if (mButtonPressingCount == 1) {
                this.onMButtonDown();
            }
            if (rButtonPressingCount == 1) {
                this.onRButtonDown();
            }
        }
    }

    public void onCursorOnComponent() {

    }

    public void onLButtonDown() {

    }

    public void onMButtonDown() {

    }

    public void onRButtonDown() {

    }
}