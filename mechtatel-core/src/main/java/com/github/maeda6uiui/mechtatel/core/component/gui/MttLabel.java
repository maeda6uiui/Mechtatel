package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector2f;

import java.awt.*;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Label
 *
 * @author maeda6uiui
 */
public class MttLabel extends MttGuiComponent {
    public static class MttLabelCreateInfo {
        public float x;
        public float y;
        public float width;
        public float height;
        public String requiredChars;
        public String fontName;
        public int fontStyle;
        public int fontSize;
        public Color fontColor;
        public Color frameColor;

        public MttLabelCreateInfo setX(float x) {
            this.x = x;
            return this;
        }

        public MttLabelCreateInfo setY(float y) {
            this.y = y;
            return this;
        }

        public MttLabelCreateInfo setWidth(float width) {
            this.width = width;
            return this;
        }

        public MttLabelCreateInfo setHeight(float height) {
            this.height = height;
            return this;
        }

        public MttLabelCreateInfo setRequiredChars(String requiredChars) {
            this.requiredChars = requiredChars;
            return this;
        }

        public MttLabelCreateInfo setFontName(String fontName) {
            this.fontName = fontName;
            return this;
        }

        public MttLabelCreateInfo setFontStyle(int fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }

        public MttLabelCreateInfo setFontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public MttLabelCreateInfo setFontColor(Color fontColor) {
            this.fontColor = fontColor;
            return this;
        }

        public MttLabelCreateInfo setFrameColor(Color frameColor) {
            this.frameColor = frameColor;
            return this;
        }
    }

    private MttQuad2D frame;
    private MttFont font;

    public MttLabel(MttVulkanInstance vulkanInstance, MttLabelCreateInfo createInfo) {
        super(vulkanInstance, createInfo.x, createInfo.y, createInfo.width, createInfo.height);

        frame = new MttQuad2D(
                vulkanInstance,
                new Vector2f(createInfo.x, createInfo.y),
                new Vector2f(createInfo.x + createInfo.width, createInfo.y + createInfo.height),
                0.0f,
                false,
                convertJavaColorToJOMLVector4f(createInfo.frameColor)
        );

        font = new MttFont(
                vulkanInstance,
                "default",
                new Font(createInfo.fontName, createInfo.fontStyle, createInfo.fontSize),
                true,
                createInfo.fontColor,
                createInfo.requiredChars
        );
    }

    public void prepare(String text) {
        font.prepare(text, new Vector2f(this.getX(), this.getY()));
        font.createBuffers();
    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
        font.setVisible(visible);
    }

    public void setFrameVisible(boolean visible) {
        frame.setVisible(visible);
    }
}
