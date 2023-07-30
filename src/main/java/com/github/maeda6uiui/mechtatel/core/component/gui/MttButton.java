package com.github.maeda6uiui.mechtatel.core.component.gui;


import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector2f;

import java.awt.*;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Button
 *
 * @author maeda6uiui
 */
public class MttButton extends MttGuiComponent {
    private MttQuad2D frame;
    private MttFont font;

    public MttButton(
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
            Color frameColor) {
        super(vulkanInstance, x, y, width, height);

        frame = new MttQuad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + height),
                0.0f,
                false,
                convertJavaColorToJOMLVector4f(frameColor)
        );

        font = new MttFont(vulkanInstance, "default", new Font(
                fontName, fontStyle, fontSize), true, fontColor, text);
        font.prepare(text, new Vector2f(x, y));
        font.createBuffers();
    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
        font.setVisible(visible);
    }
}
