package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector2f;

import java.awt.*;

/**
 * Label
 *
 * @author maeda6uiui
 */
public class MttLabel extends MttGuiComponent {
    private MttQuad2D frame;
    private MttFont font;

    public MttLabel(
            MttVulkanInstance vulkanInstance,
            float x,
            float y,
            float width,
            float height,
            String requiredChars,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color frameColor) {
        super(vulkanInstance, x, y, width, height);

        frame = new MttQuad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + height),
                0.0f,
                ClassConversionUtils.convertJavaColorToJOMLVector4f(frameColor)
        );

        font = new MttFont(vulkanInstance, "default",
                new Font(fontName, fontStyle, fontSize), true, fontColor, requiredChars);
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
