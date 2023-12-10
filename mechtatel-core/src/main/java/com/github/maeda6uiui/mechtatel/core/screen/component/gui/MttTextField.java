package com.github.maeda6uiui.mechtatel.core.screen.component.gui;

import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter.KeyInterpreter;
import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttLine2D;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex2D;
import com.github.maeda6uiui.mechtatel.core.text.Glyph;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

/**
 * Text field
 *
 * @author maeda6uiui
 */
public class MttTextField extends MttGuiComponent {
    public static final String DEFAULT_SUPPORTED_CHARACTERS
            = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~ ";
    private static final List<KeyCode> SPECIAL_KEYS;

    static {
        SPECIAL_KEYS = new ArrayList<>();
        SPECIAL_KEYS.addAll(Arrays.asList(
                KeyCode.BACKSPACE,
                KeyCode.DELETE,
                KeyCode.RIGHT,
                KeyCode.LEFT
        ));
    }

    public static class MttTextFieldCreateInfo {
        public float x;
        public float y;
        public float width;
        public float height;
        public float caretMarginX;
        public float caretMarginY;
        public String fontName;
        public int fontStyle;
        public int fontSize;
        public Color fontColor;
        public Color frameColor;
        public Color caretColor;
        public float caretBlinkInterval;
        public float secondsPerFrame;
        public float repeatDelay;
        public KeyInterpreter keyInterpreter;
        public String supportedCharacters;

        public MttTextFieldCreateInfo setX(float x) {
            this.x = x;
            return this;
        }

        public MttTextFieldCreateInfo setY(float y) {
            this.y = y;
            return this;
        }

        public MttTextFieldCreateInfo setWidth(float width) {
            this.width = width;
            return this;
        }

        public MttTextFieldCreateInfo setHeight(float height) {
            this.height = height;
            return this;
        }

        public MttTextFieldCreateInfo setCaretMarginX(float caretMarginX) {
            this.caretMarginX = caretMarginX;
            return this;
        }

        public MttTextFieldCreateInfo setCaretMarginY(float caretMarginY) {
            this.caretMarginY = caretMarginY;
            return this;
        }

        public MttTextFieldCreateInfo setFontName(String fontName) {
            this.fontName = fontName;
            return this;
        }

        public MttTextFieldCreateInfo setFontStyle(int fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }

        public MttTextFieldCreateInfo setFontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public MttTextFieldCreateInfo setFontColor(Color fontColor) {
            this.fontColor = fontColor;
            return this;
        }

        public MttTextFieldCreateInfo setFrameColor(Color frameColor) {
            this.frameColor = frameColor;
            return this;
        }

        public MttTextFieldCreateInfo setCaretColor(Color caretColor) {
            this.caretColor = caretColor;
            return this;
        }

        public MttTextFieldCreateInfo setCaretBlinkInterval(float caretBlinkInterval) {
            this.caretBlinkInterval = caretBlinkInterval;
            return this;
        }

        public MttTextFieldCreateInfo setSecondsPerFrame(float secondsPerFrame) {
            this.secondsPerFrame = secondsPerFrame;
            return this;
        }

        public MttTextFieldCreateInfo setRepeatDelay(float repeatDelay) {
            this.repeatDelay = repeatDelay;
            return this;
        }

        public MttTextFieldCreateInfo setKeyInterpreter(KeyInterpreter keyInterpreter) {
            this.keyInterpreter = keyInterpreter;
            return this;
        }

        public MttTextFieldCreateInfo setSupportedCharacters(String supportedCharacters) {
            this.supportedCharacters = supportedCharacters;
            return this;
        }
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

    public MttTextField(MttVulkanImpl vulkanImpl, IMttScreenForMttComponent screen, MttTextFieldCreateInfo createInfo) {
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

        caret = new MttLine2D(
                vulkanImpl,
                screen,
                new MttVertex2D(
                        new Vector2f(createInfo.x + createInfo.caretMarginX, createInfo.y + createInfo.caretMarginY),
                        convertJavaColorToJOMLVector4f(createInfo.caretColor)
                ),
                new MttVertex2D(
                        new Vector2f(
                                createInfo.x + createInfo.caretMarginX,
                                createInfo.y + createInfo.height - createInfo.caretMarginY
                        ),
                        convertJavaColorToJOMLVector4f(createInfo.caretColor)
                ),
                0.0f
        );

        font = new MttFont(
                vulkanImpl,
                screen,
                new Font(createInfo.fontName, createInfo.fontStyle, createInfo.fontSize),
                true,
                createInfo.fontColor,
                createInfo.supportedCharacters);

        glyphs = font.getGlyphs();

        text = "";
        prevText = "";
        caretColumn = 0;

        this.keyInterpreter = createInfo.keyInterpreter;

        this.caretBlinkInterval = createInfo.caretBlinkInterval;
        this.lastTime = (float) glfwGetTime();

        repeatDelayFrames = Math.round(createInfo.repeatDelay / createInfo.secondsPerFrame);

        visible = true;
    }

    @Override
    public void cleanup() {
        super.cleanup();

        frame.cleanup();
        caret.cleanup();
        font.cleanup();
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
        if (inputKey.isEmpty()) {
            return;
        }

        String caretPrecedingText = "";
        String caretSucceedingText = "";
        if (!text.isEmpty()) {
            caretPrecedingText = text.substring(0, caretColumn);
            caretSucceedingText = text.substring(caretColumn);
        }

        if (inputKey.equals(KeyCode.BACKSPACE.name())) {
            if (!caretPrecedingText.isEmpty()) {
                caretPrecedingText = caretPrecedingText.substring(0, caretPrecedingText.length() - 1);
                caretColumn--;
            }
        } else if (inputKey.equals(KeyCode.DELETE.name())) {
            if (!caretSucceedingText.isEmpty()) {
                caretSucceedingText = caretSucceedingText.substring(1);
            }
        } else if (inputKey.equals(KeyCode.RIGHT.name())) {
            if (caretColumn < text.length()) {
                caretColumn++;
            }
        } else if (inputKey.equals(KeyCode.LEFT.name())) {
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
            if (text.isEmpty()) {
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
