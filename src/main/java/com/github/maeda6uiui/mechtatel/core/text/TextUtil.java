package com.github.maeda6uiui.mechtatel.core.text;

import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for text rendering
 *
 * @author maeda
 */
public class TextUtil {
    public static class FontImageInfo {
        public ByteBuffer buffer;   //Must be manually freed with MemoryUtil.memFree()
        public Map<Character, Glyph> glyphs;
        public int imageWidth;
        public int imageHeight;
    }

    private static BufferedImage createCharImage(Font font, char c, boolean antiAlias, Color color) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        g.dispose();

        int charWidth = metrics.charWidth(c);
        int charHeight = metrics.getHeight();
        if (charWidth == 0) {
            return null;
        }

        image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        g.setPaint(color);
        g.drawString(String.valueOf(c), 0, metrics.getAscent());
        g.dispose();

        return image;
    }

    public static FontImageInfo createFontImage(Font font, boolean antiAlias, Color color, String requiredChars) {
        var fontImageInfo = new FontImageInfo();
        fontImageInfo.glyphs = new HashMap<>();

        int lineWidth = 0;
        int lineHeight = 0;
        var lineHeights = new ArrayList<Integer>();

        int totalImageWidth = 0;
        int totalImageHeight = 0;

        int numRequiredChars = requiredChars.length();
        final int NUM_CHARS_PER_LINE = 32;

        for (int i = 0; i < numRequiredChars; i++) {
            char c = requiredChars.charAt(i);
            BufferedImage ch = createCharImage(font, c, antiAlias, color);
            if (ch == null) {
                continue;
            }

            lineWidth += ch.getWidth();
            lineHeight = Math.max(lineHeight, ch.getHeight());

            if ((i + 1) % NUM_CHARS_PER_LINE == 0) {
                totalImageWidth = Math.max(lineWidth, totalImageWidth);
                totalImageHeight += lineHeight;
                lineHeights.add(lineHeight);
                lineWidth = 0;
                lineHeight = 0;
            }
        }
        if (totalImageWidth == 0) {
            totalImageWidth = lineWidth;
        }
        totalImageHeight += lineHeight;

        fontImageInfo.imageWidth = totalImageWidth;
        fontImageInfo.imageHeight = totalImageHeight;

        BufferedImage image = new BufferedImage(totalImageWidth, totalImageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        int x = 0;
        int y = 0;
        int lineCount = 0;
        for (int i = 0; i < numRequiredChars; i++) {
            char c = requiredChars.charAt(i);
            BufferedImage charImage = createCharImage(font, c, antiAlias, color);
            if (charImage == null) {
                continue;
            }

            int charWidth = charImage.getWidth();
            int charHeight = charImage.getHeight();

            Glyph ch = new Glyph(charWidth, charHeight, x, y, 0.0f);
            g.drawImage(charImage, x, y, null);
            x += ch.width;
            fontImageInfo.glyphs.put(c, ch);

            if ((i + 1) % NUM_CHARS_PER_LINE == 0) {
                x = 0;
                y += lineHeights.get(lineCount);
                lineCount++;
            }
        }

        try {
            ImageIO.write(image, "png", new File("font.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int width = image.getWidth();
        int height = image.getHeight();

        var pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = pixels[i * width + j];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();
        fontImageInfo.buffer = buffer;

        return fontImageInfo;
    }

    public static int getWidth(CharSequence text, Map<Character, Glyph> glyphs) {
        int width = 0;
        int lineWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                width = Math.max(width, lineWidth);
                lineWidth = 0;

                continue;
            }
            if (c == '\r') {
                continue;
            }

            Glyph g = glyphs.get(c);
            lineWidth += g.width;
        }
        width = Math.max(width, lineWidth);
        return width;
    }

    public static int getHeight(CharSequence text, Map<Character, Glyph> glyphs) {
        int height = 0;
        int lineHeight = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                height += lineHeight;
                lineHeight = 0;

                continue;
            }
            if (c == '\r') {
                continue;
            }

            Glyph g = glyphs.get(c);
            lineHeight = Math.max(lineHeight, g.height);
        }
        height += lineHeight;
        return height;
    }
}
