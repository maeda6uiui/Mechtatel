package com.github.maeda6uiui.mechtatel.core.vulkan.text;

import com.github.maeda6uiui.mechtatel.core.text.Glyph;
import com.github.maeda6uiui.mechtatel.core.text.TextUtil;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.Texture;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Font
 *
 * @author maeda
 */
public class MttFont {
    private VkDevice device;

    private Map<Character, Glyph> glyphs;
    private Texture texture;
    private int imageHeight;

    public MttFont(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<Long> descriptorSets,
            int setCount,
            Font font,
            boolean antiAlias,
            Color color) {
        this.device = device;

        this.texture = this.createFontTexture(
                commandPool,
                graphicsQueue,
                descriptorSets,
                setCount,
                font,
                antiAlias,
                color
        );
    }

    private Texture createFontTexture(
            long commandPool,
            VkQueue graphicsQueue,
            List<Long> descriptorSets,
            int setCount,
            Font font,
            boolean antiAlias,
            Color color) {
        TextUtil.FontImageInfo fontImageInfo = TextUtil.createFontImage(font, antiAlias, color);
        this.glyphs = fontImageInfo.glyphs;
        this.imageHeight = fontImageInfo.imageHeight;

        texture = new Texture(
                device,
                commandPool,
                graphicsQueue,
                descriptorSets,
                setCount,
                fontImageInfo.buffer,
                fontImageInfo.imageWidth,
                fontImageInfo.imageHeight,
                false
        );

        MemoryUtil.memFree(fontImageInfo.buffer);

        return texture;
    }
}
