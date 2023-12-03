package com.github.maeda6uiui.mechtatel.core.screen.component.gui;


import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import org.joml.Vector2f;

import java.awt.*;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Button
 *
 * @author maeda6uiui
 */
public class MttButton extends MttGuiComponent {
    public static class MttButtonCreateInfo {
        public float x;
        public float y;
        public float width;
        public float height;
        public String text;
        public String fontName;
        public int fontStyle;
        public int fontSize;
        public Color fontColor;
        public Color frameColor;

        public MttButtonCreateInfo setX(float x) {
            this.x = x;
            return this;
        }

        public MttButtonCreateInfo setY(float y) {
            this.y = y;
            return this;
        }

        public MttButtonCreateInfo setWidth(float width) {
            this.width = width;
            return this;
        }

        public MttButtonCreateInfo setHeight(float height) {
            this.height = height;
            return this;
        }

        public MttButtonCreateInfo setText(String text) {
            this.text = text;
            return this;
        }

        public MttButtonCreateInfo setFontName(String fontName) {
            this.fontName = fontName;
            return this;
        }

        public MttButtonCreateInfo setFontStyle(int fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }

        public MttButtonCreateInfo setFontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public MttButtonCreateInfo setFontColor(Color fontColor) {
            this.fontColor = fontColor;
            return this;
        }

        public MttButtonCreateInfo setFrameColor(Color frameColor) {
            this.frameColor = frameColor;
            return this;
        }
    }

    private MttQuad2D frame;
    private MttFont font;

    public MttButton(MttVulkanImpl vulkanImpl, IMttScreenForMttComponent screen, MttButtonCreateInfo createInfo) {
        super(screen, createInfo.x, createInfo.y, createInfo.width, createInfo.height);

        frame = new MttQuad2D(
                vulkanImpl,
                screen,
                new Vector2f(createInfo.x, createInfo.y),
                new Vector2f(createInfo.x + createInfo.width, createInfo.y + createInfo.height),
                0.0f,
                false,
                convertJavaColorToJOMLVector4f(createInfo.frameColor)
        );

        font = new MttFont(
                vulkanImpl,
                screen,
                new Font(createInfo.fontName, createInfo.fontStyle, createInfo.fontSize),
                true,
                createInfo.fontColor,
                createInfo.text
        );
        font.prepare(createInfo.text, new Vector2f(createInfo.x, createInfo.y));
        font.createBuffers();
    }

    @Override
    public void cleanup() {
        super.cleanup();

        frame.cleanup();
        font.cleanup();
    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
        font.setVisible(visible);
    }
}
