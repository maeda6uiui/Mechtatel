package com.github.maeda6uiui.mechtatel.core.component.gui;


import com.github.maeda6uiui.mechtatel.core.component.Quad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.awt.*;

/**
 * Button
 *
 * @author maeda6uiui
 */
public class MttButton extends MttGuiComponent {
    private Quad2D frame;

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
        super(vulkanInstance, x, y, width, height, text, fontName, fontStyle, fontSize, fontColor);

        float fFrameColorR = frameColor.getRed() / 255.0f;
        float fFrameColorG = frameColor.getGreen() / 255.0f;
        float fFrameColorB = frameColor.getBlue() / 255.0f;
        frame = new Quad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + height),
                0.0f,
                new Vector4f(fFrameColorR, fFrameColorG, fFrameColorB, 1.0f)
        );
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        frame.setVisible(visible);
    }

    public void setFrameVisible(boolean visible) {
        frame.setVisible(visible);
    }

    @Override
    public void translate(float diffX, float diffY) {
        super.translate(diffX, diffY);
        frame.setMat(new Matrix4f().translate(diffX, diffY, 0.0f));
    }
}
