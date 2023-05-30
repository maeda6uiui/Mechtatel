package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.component.MttLine2DSet;
import com.github.maeda6uiui.mechtatel.core.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector2f;

import java.awt.*;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Checkbox
 *
 * @author maeda6uiui
 */
public class MttCheckbox extends MttGuiComponent {
    private static final float CHECKBOX_MARGIN_X_HEIGHT_RATIO = 0.3f;
    private static final float CHECKBOX_MARGIN_Y_HEIGHT_RATIO = 0.25f;

    private MttQuad2D checkboxFrame;
    private MttLine2DSet checkboxCross;
    private MttFont font;

    private Vector2f checkboxTopLeft;
    private Vector2f checkboxBottomRight;

    private boolean selected;

    public MttCheckbox(
            MttVulkanInstance vulkanInstance,
            float x,
            float y,
            float width,
            float height,
            String text,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color checkboxColor) {
        super(vulkanInstance, x + height, y, width - height, height);

        checkboxTopLeft = new Vector2f(x + height * CHECKBOX_MARGIN_X_HEIGHT_RATIO, y + height * CHECKBOX_MARGIN_Y_HEIGHT_RATIO);
        checkboxBottomRight = new Vector2f(x + height * (1.0f - CHECKBOX_MARGIN_X_HEIGHT_RATIO), y + height * (1.0f - CHECKBOX_MARGIN_Y_HEIGHT_RATIO));

        checkboxFrame = new MttQuad2D(
                vulkanInstance,
                checkboxTopLeft,
                checkboxBottomRight,
                0.0f,
                convertJavaColorToJOMLVector4f(checkboxColor)
        );
        checkboxCross = new MttLine2DSet(vulkanInstance);
        checkboxCross.add(
                checkboxTopLeft,
                checkboxBottomRight,
                convertJavaColorToJOMLVector4f(checkboxColor),
                0.0f
        );
        checkboxCross.add(
                new Vector2f(checkboxTopLeft.x, checkboxBottomRight.y),
                new Vector2f(checkboxBottomRight.x, checkboxTopLeft.y),
                convertJavaColorToJOMLVector4f(checkboxColor),
                0.0f
        );
        checkboxCross.createBuffer();

        font = new MttFont(vulkanInstance, "default", new Font(
                fontName, fontStyle, fontSize), true, fontColor, text);
        font.prepare(text, new Vector2f(x, y));
        font.createBuffers();

        selected = false;
        checkboxCross.setVisible(false);
    }

    @Override
    public void update(
            int cursorX,
            int cursorY,
            int windowWidth,
            int windowHeight,
            int lButtonPressingCount,
            int mButtonPressingCount,
            int rButtonPressingCount) {
        super.update(
                cursorX,
                cursorY,
                windowWidth,
                windowHeight,
                lButtonPressingCount,
                mButtonPressingCount,
                rButtonPressingCount);

        float fCursorX = (float) cursorX / (float) windowWidth * 2.0f - 1.0f;
        float fCursorY = (float) cursorY / (float) windowHeight * 2.0f - 1.0f;

        if ((checkboxTopLeft.x < fCursorX && fCursorX < checkboxBottomRight.x)
                && (checkboxTopLeft.y < fCursorY && fCursorY < checkboxBottomRight.y)) {
            if (lButtonPressingCount == 1) {
                this.setSelected(!selected);
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        checkboxFrame.setVisible(visible);
        checkboxCross.setVisible(visible);
        font.setVisible(visible);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        checkboxCross.setVisible(selected);
    }

    public boolean isSelected() {
        return selected;
    }
}
