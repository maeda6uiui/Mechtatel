package com.github.maeda6uiui.mechtatel.core.screen.component.gui;

import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttLine2DSet;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import org.joml.Vector2f;

import java.awt.*;
import java.util.Map;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Check box
 *
 * @author maeda6uiui
 */
public class MttCheckBox extends MttGuiComponent {
    public static class MttCheckBoxCreateInfo {
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

        public MttCheckBoxCreateInfo setX(float x) {
            this.x = x;
            return this;
        }

        public MttCheckBoxCreateInfo setY(float y) {
            this.y = y;
            return this;
        }

        public MttCheckBoxCreateInfo setWidth(float width) {
            this.width = width;
            return this;
        }

        public MttCheckBoxCreateInfo setHeight(float height) {
            this.height = height;
            return this;
        }

        public MttCheckBoxCreateInfo setBoxX(float boxX) {
            this.boxX = boxX;
            return this;
        }

        public MttCheckBoxCreateInfo setBoxY(float boxY) {
            this.boxY = boxY;
            return this;
        }

        public MttCheckBoxCreateInfo setBoxWidth(float boxWidth) {
            this.boxWidth = boxWidth;
            return this;
        }

        public MttCheckBoxCreateInfo setBoxHeight(float boxHeight) {
            this.boxHeight = boxHeight;
            return this;
        }

        public MttCheckBoxCreateInfo setTextX(float textX) {
            this.textX = textX;
            return this;
        }

        public MttCheckBoxCreateInfo setTextY(float textY) {
            this.textY = textY;
            return this;
        }

        public MttCheckBoxCreateInfo setText(String text) {
            this.text = text;
            return this;
        }

        public MttCheckBoxCreateInfo setFontName(String fontName) {
            this.fontName = fontName;
            return this;
        }

        public MttCheckBoxCreateInfo setFontStyle(int fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }

        public MttCheckBoxCreateInfo setFontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public MttCheckBoxCreateInfo setFontColor(Color fontColor) {
            this.fontColor = fontColor;
            return this;
        }

        public MttCheckBoxCreateInfo setCheckboxColor(Color checkboxColor) {
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

    public MttCheckBox(MttVulkanImpl vulkanImpl, IMttScreenForMttComponent screen, MttCheckBoxCreateInfo createInfo) {
        super(screen, createInfo.x, createInfo.y, createInfo.width, createInfo.height);

        checkboxTopLeft = new Vector2f(createInfo.boxX, createInfo.boxY);
        checkboxBottomRight = new Vector2f(
                createInfo.boxX + createInfo.boxWidth, createInfo.boxY + createInfo.boxHeight);

        checkboxFrame = new MttQuad2D(
                vulkanImpl,
                screen,
                checkboxTopLeft,
                checkboxBottomRight,
                0.0f,
                false,
                convertJavaColorToJOMLVector4f(createInfo.checkboxColor)
        );
        checkboxCross = new MttLine2DSet(vulkanImpl, screen);
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
                vulkanImpl,
                screen,
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
    public void cleanup() {
        super.cleanup();

        checkboxFrame.cleanup();
        checkboxCross.cleanup();
        font.cleanup();
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
            Map<KeyCode, Integer> keyboardPressingCounts) {
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
