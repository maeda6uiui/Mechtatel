package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.component.MttLine2D;
import com.github.maeda6uiui.mechtatel.core.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.component.MttVertex2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector2f;

import java.awt.*;
import java.util.Map;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

/**
 * Textbox
 *
 * @author maeda6uiui
 */
public class MttTextbox extends MttGuiComponent {
    public static final String SUPPORTED_CHARACTERS
            = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~ ";

    private MttQuad2D frame;
    private MttLine2D caret;
    private MttFont font;

    private boolean allowLines;

    private float caretBlinkInterval;
    private float lastTime;

    private float secondsPerFrame;
    private float repeatDelay;

    private boolean visible;

    public MttTextbox(
            MttVulkanInstance vulkanInstance,
            float x,
            float y,
            float width,
            float height,
            float caretMarginX,
            float caretMarginY,
            float lineHeight,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color frameColor,
            Color caretColor,
            boolean allowLines,
            float caretBlinkInterval,
            float secondsPerFrame,
            float repeatDelay) {
        super(vulkanInstance, x, y, width, height);

        frame = new MttQuad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + height),
                0.0f,
                convertJavaColorToJOMLVector4f(frameColor)
        );

        caret = new MttLine2D(
                vulkanInstance,
                new MttVertex2D(
                        new Vector2f(x + caretMarginX, y + caretMarginY), convertJavaColorToJOMLVector4f(caretColor)
                ),
                new MttVertex2D(
                        new Vector2f(x + caretMarginX, y + lineHeight - caretMarginY), convertJavaColorToJOMLVector4f(caretColor)
                ),
                0.0f
        );

        font = new MttFont(
                vulkanInstance,
                "default",
                new Font(fontName, fontStyle, fontSize),
                true,
                fontColor,
                SUPPORTED_CHARACTERS);

        this.allowLines = allowLines;

        this.caretBlinkInterval = caretBlinkInterval;
        this.lastTime = (float) glfwGetTime();

        this.secondsPerFrame = secondsPerFrame;
        this.repeatDelay = repeatDelay;

        visible = true;
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

        float currentTime = (float) glfwGetTime();
        float elapsedTime = currentTime - lastTime;
        if (elapsedTime >= caretBlinkInterval) {
            if (this.visible) {
                boolean caretVisible = caret.isVisible();
                caret.setVisible(!caretVisible);
            }

            lastTime = currentTime;
        }


    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
        caret.setVisible(visible);
        font.setVisible(visible);

        this.visible = visible;
    }
}
