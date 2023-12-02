package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.component.MttLine2D;
import com.github.maeda6uiui.mechtatel.core.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.component.MttVertex2D;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter.KeyInterpreter;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.text.Glyph;
import com.github.maeda6uiui.mechtatel.core.text.TextUtil;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

/**
 * Text area
 *
 * @author maeda6uiui
 */
public class MttTextArea extends MttGuiComponent {
    public static final String DEFAULT_SUPPORTED_CHARACTERS
            = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~ ";
    private static final List<String> SPECIAL_KEYS;

    static {
        SPECIAL_KEYS = new ArrayList<>();
        SPECIAL_KEYS.add("BACKSPACE");
        SPECIAL_KEYS.add("DELETE");
        SPECIAL_KEYS.add("RIGHT");
        SPECIAL_KEYS.add("LEFT");
        SPECIAL_KEYS.add("UP");
        SPECIAL_KEYS.add("DOWN");
        SPECIAL_KEYS.add("ENTER");
    }

    public static class MttTextAreaCreateInfo {
        public float x;
        public float y;
        public float width;
        public float height;
        public float caretLength;
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

        public MttTextAreaCreateInfo setX(float x) {
            this.x = x;
            return this;
        }

        public MttTextAreaCreateInfo setY(float y) {
            this.y = y;
            return this;
        }

        public MttTextAreaCreateInfo setWidth(float width) {
            this.width = width;
            return this;
        }

        public MttTextAreaCreateInfo setHeight(float height) {
            this.height = height;
            return this;
        }

        public MttTextAreaCreateInfo setCaretLength(float caretLength) {
            this.caretLength = caretLength;
            return this;
        }

        public MttTextAreaCreateInfo setCaretMarginX(float caretMarginX) {
            this.caretMarginX = caretMarginX;
            return this;
        }

        public MttTextAreaCreateInfo setCaretMarginY(float caretMarginY) {
            this.caretMarginY = caretMarginY;
            return this;
        }

        public MttTextAreaCreateInfo setFontName(String fontName) {
            this.fontName = fontName;
            return this;
        }

        public MttTextAreaCreateInfo setFontStyle(int fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }

        public MttTextAreaCreateInfo setFontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public MttTextAreaCreateInfo setFontColor(Color fontColor) {
            this.fontColor = fontColor;
            return this;
        }

        public MttTextAreaCreateInfo setFrameColor(Color frameColor) {
            this.frameColor = frameColor;
            return this;
        }

        public MttTextAreaCreateInfo setCaretColor(Color caretColor) {
            this.caretColor = caretColor;
            return this;
        }

        public MttTextAreaCreateInfo setCaretBlinkInterval(float caretBlinkInterval) {
            this.caretBlinkInterval = caretBlinkInterval;
            return this;
        }

        public MttTextAreaCreateInfo setSecondsPerFrame(float secondsPerFrame) {
            this.secondsPerFrame = secondsPerFrame;
            return this;
        }

        public MttTextAreaCreateInfo setRepeatDelay(float repeatDelay) {
            this.repeatDelay = repeatDelay;
            return this;
        }

        public MttTextAreaCreateInfo setKeyInterpreter(KeyInterpreter keyInterpreter) {
            this.keyInterpreter = keyInterpreter;
            return this;
        }

        public MttTextAreaCreateInfo setSupportedCharacters(String supportedCharacters) {
            this.supportedCharacters = supportedCharacters;
            return this;
        }
    }

    private MttQuad2D frame;
    private MttLine2D caret;
    private MttFont font;

    private Map<Character, Glyph> glyphs;

    private List<String> lines;
    private List<String> prevLines;
    private int caretRow;
    private int caretColumn;

    private KeyInterpreter keyInterpreter;

    private float caretBlinkInterval;
    private float lastTime;
    private int repeatDelayFrames;

    private boolean visible;

    public MttTextArea(MttVulkanImpl vulkanImpl, MttScreen screen, MttTextAreaCreateInfo createInfo) {
        super(createInfo.x, createInfo.y, createInfo.width, createInfo.height);

        frame = new MttQuad2D(
                vulkanImpl,
                new Vector2f(createInfo.x, createInfo.y),
                new Vector2f(createInfo.x + createInfo.width, createInfo.y + createInfo.height),
                0.0f,
                false,
                convertJavaColorToJOMLVector4f(createInfo.frameColor)
        );

        caret = new MttLine2D(
                vulkanImpl,
                new MttVertex2D(
                        new Vector2f(createInfo.x + createInfo.caretMarginX, createInfo.y + createInfo.caretMarginY),
                        convertJavaColorToJOMLVector4f(createInfo.caretColor)
                ),
                new MttVertex2D(
                        new Vector2f(
                                createInfo.x + createInfo.caretMarginX,
                                createInfo.y + createInfo.caretLength - createInfo.caretMarginY
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
                createInfo.supportedCharacters
        );

        glyphs = font.getGlyphs();

        lines = new ArrayList<>();
        lines.add("");
        prevLines = new ArrayList<>();
        prevLines.add("");

        caretRow = 0;
        caretColumn = 0;

        this.keyInterpreter = createInfo.keyInterpreter;

        this.caretBlinkInterval = createInfo.caretBlinkInterval;
        this.lastTime = (float) glfwGetTime();

        repeatDelayFrames = Math.round(createInfo.repeatDelay / createInfo.secondsPerFrame);

        visible = true;
    }

    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    public String getText() {
        var sb = new StringBuilder();

        for (var line : lines) {
            sb.append(line);
            sb.append("\n");
        }

        return sb.toString();
    }

    public void clear() {
        lines.clear();
        lines.add("");

        prevLines.clear();
        prevLines.add("");

        caretRow = 0;
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

        String caretLine = lines.get(caretRow);

        String caretPrecedingText = "";
        String caretSucceedingText = "";
        if (!caretLine.equals("")) {
            caretPrecedingText = caretLine.substring(0, caretColumn);
            caretSucceedingText = caretLine.substring(caretColumn, caretLine.length());
        }

        String precedingLine = "";
        String succeedingLine = "";
        if (caretRow != 0) {
            precedingLine = lines.get(caretRow - 1);
        }
        if (caretRow != lines.size() - 1) {
            succeedingLine = lines.get(caretRow + 1);
        }

        if (inputKey.equals("ENTER")) {
            lines.set(caretRow, caretPrecedingText);
            lines.add(caretRow + 1, caretSucceedingText);

            caretRow++;
            caretColumn = 0;
        } else if (inputKey.equals("BACKSPACE")) {
            if (caretPrecedingText.equals("")) {
                if (caretRow != 0) {
                    String newPreceedingLine = precedingLine + caretSucceedingText;
                    lines.set(caretRow - 1, newPreceedingLine);

                    caretColumn = precedingLine.length();
                    lines.remove(caretRow);
                    caretRow--;
                }
            } else {
                caretPrecedingText = caretPrecedingText.substring(0, caretPrecedingText.length() - 1);

                String newCaretLine = caretPrecedingText + caretSucceedingText;
                lines.set(caretRow, newCaretLine);

                caretColumn--;
            }
        } else if (inputKey.equals("DELETE")) {
            if (caretSucceedingText.equals("")) {
                if (caretRow != lines.size() - 1) {
                    String newCaretLine = caretPrecedingText + succeedingLine;
                    lines.set(caretRow, newCaretLine);

                    lines.remove(caretRow + 1);
                }
            } else {
                caretSucceedingText = caretSucceedingText.substring(1, caretSucceedingText.length());

                String newCaretLine = caretPrecedingText + caretSucceedingText;
                lines.set(caretRow, newCaretLine);
            }
        } else if (inputKey.equals("RIGHT")) {
            if (caretColumn == caretLine.length()) {
                if (caretRow != lines.size() - 1) {
                    caretRow++;
                    caretColumn = 0;
                }
            } else {
                caretColumn++;
            }
        } else if (inputKey.equals("LEFT")) {
            if (caretColumn == 0) {
                if (caretRow != 0) {
                    caretColumn = lines.get(caretRow - 1).length();
                    caretRow--;
                }
            } else {
                caretColumn--;
            }
        } else if (inputKey.equals("UP")) {
            if (caretRow != 0) {
                if (caretColumn > precedingLine.length()) {
                    caretColumn = precedingLine.length();
                }

                caretRow--;
            }
        } else if (inputKey.equals("DOWN")) {
            if (caretRow != lines.size() - 1) {
                if (caretColumn > succeedingLine.length()) {
                    caretColumn = succeedingLine.length();
                }

                caretRow++;
            }
        } else {
            caretPrecedingText += inputKey;
            String newCaretLine = caretPrecedingText + caretSucceedingText;
            lines.set(caretRow, newCaretLine);

            caretColumn++;
        }

        String newCaretLine = lines.get(caretRow);
        float caretTranslateX = 0.0f;
        for (int i = 0; i < caretColumn; i++) {
            char c = newCaretLine.charAt(i);
            Glyph glyph = glyphs.get(c);

            caretTranslateX += glyph.width * MttFont.DEFAULT_GLYPH_WIDTH_SCALE;
        }

        float caretTranslateY = 0.0f;
        for (int i = 0; i < caretRow; i++) {
            String line = lines.get(i);

            int lineHeight;
            if (line.equals("")) {
                lineHeight = glyphs.get(' ').height;
            } else {
                lineHeight = TextUtil.getHeight(line, glyphs);
            }

            caretTranslateY += lineHeight * MttFont.DEFAULT_LINE_HEIGHT_SCALE;
        }

        caret.setMat(new Matrix4f().translate(caretTranslateX, caretTranslateY, 0.0f));

        boolean textChanged = false;
        if (prevLines.size() != lines.size()) {
            textChanged = true;
        } else {
            for (int i = 0; i < lines.size(); i++) {
                String prevLine = prevLines.get(i);
                String line = lines.get(i);

                if (!line.equals(prevLine)) {
                    textChanged = true;
                    break;
                }
            }
        }

        boolean allLinesEmpty = true;
        boolean onlyChangeLines = true;
        for (var line : lines) {
            if (!line.isEmpty()) {
                allLinesEmpty = false;
            }
            if (!line.equals("\n")) {
                onlyChangeLines = false;
            }

            if (!allLinesEmpty && !onlyChangeLines) {
                break;
            }
        }

        if (textChanged) {
            if (allLinesEmpty || onlyChangeLines) {
                font.clear();
            } else {
                var sb = new StringBuilder();
                for (var line : lines) {
                    if (line.equals("")) {
                        sb.append(" ");
                    } else {
                        sb.append(line);
                    }
                    sb.append("\n");
                }

                font.prepare(sb.toString(), new Vector2f(this.getX(), this.getY()));
                font.createBuffers();
            }

            prevLines = new ArrayList<>(lines);
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
