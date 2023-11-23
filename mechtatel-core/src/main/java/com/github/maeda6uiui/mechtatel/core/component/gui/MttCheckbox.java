package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.component.MttLine2DSet;
import com.github.maeda6uiui.mechtatel.core.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector2f;

import java.awt.*;
import java.util.Map;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Checkbox
 *
 * @author maeda6uiui
 */
public class MttCheckbox extends MttGuiComponent {
    public static class MttCheckboxCreateInfo {
        public float x;
        public float y;
        public float width;
        public float height;
        public float boxX;
        public float boxY;
        public float boxWidth;
        public float boxHeight;
        public float textX;
        public float textY;
        public String text;
        public String fontName;
        public int fontStyle;
        public int fontSize;
        public Color fontColor;
        public Color checkboxColor;

        public MttCheckboxCreateInfo setX(float x) {
            this.x = x;
            return this;
        }

        public MttCheckboxCreateInfo setY(float y) {
            this.y = y;
            return this;
        }

        public MttCheckboxCreateInfo setWidth(float width) {
            this.width = width;
            return this;
        }

        public MttCheckboxCreateInfo setHeight(float height) {
            this.height = height;
            return this;
        }

        public MttCheckboxCreateInfo setBoxX(float boxX) {
            this.boxX = boxX;
            return this;
        }

        public MttCheckboxCreateInfo setBoxY(float boxY) {
            this.boxY = boxY;
            return this;
        }

        public MttCheckboxCreateInfo setBoxWidth(float boxWidth) {
            this.boxWidth = boxWidth;
            return this;
        }

        public MttCheckboxCreateInfo setBoxHeight(float boxHeight) {
            this.boxHeight = boxHeight;
            return this;
        }

        public MttCheckboxCreateInfo setTextX(float textX) {
            this.textX = textX;
            return this;
        }

        public MttCheckboxCreateInfo setTextY(float textY) {
            this.textY = textY;
            return this;
        }

        public MttCheckboxCreateInfo setText(String text) {
            this.text = text;
            return this;
        }

        public MttCheckboxCreateInfo setFontName(String fontName) {
            this.fontName = fontName;
            return this;
        }

        public MttCheckboxCreateInfo setFontStyle(int fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }

        public MttCheckboxCreateInfo setFontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public MttCheckboxCreateInfo setFontColor(Color fontColor) {
            this.fontColor = fontColor;
            return this;
        }

        public MttCheckboxCreateInfo setCheckboxColor(Color checkboxColor) {
            this.checkboxColor = checkboxColor;
            return this;
        }
    }

    private MttQuad2D checkboxFrame;
    private MttLine2DSet checkboxCross;
    private MttFont font;

    private Vector2f checkboxTopLeft;
    private Vector2f checkboxBottomRight;

    private boolean selected;

    public MttCheckbox(MttVulkanInstance vulkanInstance, MttCheckboxCreateInfo createInfo) {
        super(vulkanInstance, createInfo.x, createInfo.y, createInfo.width, createInfo.height);

        checkboxTopLeft = new Vector2f(createInfo.boxX, createInfo.boxY);
        checkboxBottomRight = new Vector2f(
                createInfo.boxX + createInfo.boxWidth, createInfo.boxY + createInfo.boxHeight);

        checkboxFrame = new MttQuad2D(
                vulkanInstance,
                checkboxTopLeft,
                checkboxBottomRight,
                0.0f,
                false,
                convertJavaColorToJOMLVector4f(createInfo.checkboxColor)
        );
        checkboxCross = new MttLine2DSet(vulkanInstance);
        checkboxCross.add(
                checkboxTopLeft,
                checkboxBottomRight,
                convertJavaColorToJOMLVector4f(createInfo.checkboxColor),
                0.0f
        );
        checkboxCross.add(
                new Vector2f(checkboxTopLeft.x, checkboxBottomRight.y),
                new Vector2f(checkboxBottomRight.x, checkboxTopLeft.y),
                convertJavaColorToJOMLVector4f(createInfo.checkboxColor),
                0.0f
        );
        checkboxCross.createBuffer();

        font = new MttFont(
                vulkanInstance,
                "default",
                new Font(createInfo.fontName, createInfo.fontStyle, createInfo.fontSize),
                true,
                createInfo.fontColor,
                createInfo.text
        );
        font.prepare(createInfo.text, new Vector2f(createInfo.textX, createInfo.textY));
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
            int rButtonPressingCount,
            Map<String, Integer> keyboardPressingCounts) {
        super.update(
                cursorX,
                cursorY,
                windowWidth,
                windowHeight,
                lButtonPressingCount,
                mButtonPressingCount,
                rButtonPressingCount,
                keyboardPressingCounts);

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
