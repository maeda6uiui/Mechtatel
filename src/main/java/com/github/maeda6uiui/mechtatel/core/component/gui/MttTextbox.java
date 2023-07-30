package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.component.MttLine2D;
import com.github.maeda6uiui.mechtatel.core.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.component.MttVertex2D;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter.KeyInterpreter;
import com.github.maeda6uiui.mechtatel.core.text.Glyph;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

/**
 * Textbox
 *
 * @author maeda6uiui
 */
public class MttTextbox extends MttGuiComponent {
    public static final String DEFAULT_SUPPORTED_CHARACTERS
            = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~ ";
    private static final List<String> SPECIAL_KEYS;

    static {
        SPECIAL_KEYS = new ArrayList<>();
        SPECIAL_KEYS.add("BACKSPACE");
        SPECIAL_KEYS.add("DELETE");
        SPECIAL_KEYS.add("RIGHT");
        SPECIAL_KEYS.add("LEFT");
    }

    private MttQuad2D frame;
    private MttLine2D caret;
    private MttFont font;

    private Map<Character, Glyph> glyphs;

    private String text;
    private String prevText;
    private int caretColumn;

    private KeyInterpreter keyInterpreter;

    private float caretBlinkInterval;
    private float lastTime;
    private int repeatDelayFrames;

    private boolean visible;

    public MttTextbox(
            MttVulkanInstance vulkanInstance,
            float x,
            float y,
            float width,
            float height,
            float caretMarginX,
            float caretMarginY,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color frameColor,
            Color caretColor,
            float caretBlinkInterval,
            float secondsPerFrame,
            float repeatDelay,
            KeyInterpreter keyInterpreter,
            String supportedCharacters) {
        super(vulkanInstance, x, y, width, height);

        frame = new MttQuad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + height),
                0.0f,
                false,
                convertJavaColorToJOMLVector4f(frameColor)
        );

        caret = new MttLine2D(
                vulkanInstance,
                new MttVertex2D(
                        new Vector2f(x + caretMarginX, y + caretMarginY), convertJavaColorToJOMLVector4f(caretColor)
                ),
                new MttVertex2D(
                        new Vector2f(x + caretMarginX, y + height - caretMarginY), convertJavaColorToJOMLVector4f(caretColor)
                ),
                0.0f
        );

        font = new MttFont(
                vulkanInstance,
                "default",
                new Font(fontName, fontStyle, fontSize),
                true,
                fontColor,
                supportedCharacters);

        glyphs = font.getGlyphs();

        text = "";
        prevText = "";
        caretColumn = 0;

        this.keyInterpreter = keyInterpreter;

        this.caretBlinkInterval = caretBlinkInterval;
        this.lastTime = (float) glfwGetTime();

        repeatDelayFrames = Math.round(repeatDelay / secondsPerFrame);

        visible = true;
    }

    public String getText() {
        return text;
    }

    public void clear() {
        text = "";
        caretColumn = 0;

        font.clear();
        caret.setMat(new Matrix4f().identity());
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

        boolean focused = this.isFocused();

        float currentTime = (float) glfwGetTime();
        float elapsedTime = currentTime - lastTime;
        if (elapsedTime >= caretBlinkInterval) {
            if (this.visible && focused) {
                boolean caretVisible = caret.isVisible();
                caret.setVisible(!caretVisible);
            }

            if (!focused) {
                caret.setVisible(false);
            }

            lastTime = currentTime;
        }

        if (!focused) {
            return;
        }

        String inputKey = keyInterpreter.getInputLetter(keyboardPressingCounts, SPECIAL_KEYS, repeatDelayFrames);
        if (inputKey.equals("")) {
            return;
        }

        String caretPrecedingText = "";
        String caretSucceedingText = "";
        if (!text.equals("")) {
            caretPrecedingText = text.substring(0, caretColumn);
            caretSucceedingText = text.substring(caretColumn, text.length());
        }

        if (inputKey.equals("BACKSPACE")) {
            if (!caretPrecedingText.equals("")) {
                caretPrecedingText = caretPrecedingText.substring(0, caretPrecedingText.length() - 1);
                caretColumn--;
            }
        } else if (inputKey.equals("DELETE")) {
            if (!caretSucceedingText.equals("")) {
                caretSucceedingText = caretSucceedingText.substring(1, caretSucceedingText.length());
            }
        } else if (inputKey.equals("RIGHT")) {
            if (caretColumn < text.length()) {
                caretColumn++;
            }
        } else if (inputKey.equals("LEFT")) {
            if (caretColumn > 0) {
                caretColumn--;
            }
        } else {
            caretPrecedingText += inputKey;
            caretColumn++;
        }

        text = caretPrecedingText + caretSucceedingText;

        float caretTranslateX = 0.0f;
        for (int i = 0; i < caretColumn; i++) {
            char c = text.charAt(i);
            Glyph glyph = glyphs.get(c);

            caretTranslateX += glyph.width * MttFont.DEFAULT_GLYPH_WIDTH_SCALE;
        }

        caret.setMat(new Matrix4f().translate(caretTranslateX, 0.0f, 0.0f));

        if (!text.equals(prevText)) {
            if (text.equals("")) {
                font.clear();
            } else {
                font.prepare(text, new Vector2f(this.getX(), this.getY()));
                font.createBuffers();
            }

            prevText = text;
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
