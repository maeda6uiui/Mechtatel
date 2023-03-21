package com.github.maeda6uiui.mechtatel.core.component.gui;


import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.component.Quad2D;
import com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector2f;

import java.awt.*;

/**
 * Button
 *
 * @author maeda6uiui
 */
public class MttButton extends MttGuiComponent {
    private Quad2D frame;
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

        frame = new Quad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + height),
                0.0f,
                ClassConversionUtils.convertJavaColorToJOMLVector4f(frameColor)
        );

        font = new MttFont(vulkanInstance, new Font(
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
