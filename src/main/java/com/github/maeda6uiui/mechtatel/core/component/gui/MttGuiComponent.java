package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.Component;
import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Matrix4f;
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

    private MttGuiComponentCallbacks callbacks;
    private boolean cursorOn;

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

        callbacks = new MttGuiComponentCallbacks();
        cursorOn = false;
    }

    public void setCallbacks(MttGuiComponentCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void setVisible(boolean visible) {
        font.setVisible(visible);
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

    protected MttFont getFont() {
        return font;
    }

    public void translate(float diffX, float diffY) {
        x += diffX;
        y += diffY;
        font.applyMat(new Matrix4f().translate(diffX, diffY, 0.0f));
    }

    public boolean isCursorOn() {
        return cursorOn;
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
    }
}
