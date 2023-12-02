package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.component.IMttComponentForVkMttComponent;
import com.github.maeda6uiui.mechtatel.core.component.MttVertex2DUV;
import com.github.maeda6uiui.mechtatel.core.text.Glyph;
import com.github.maeda6uiui.mechtatel.core.text.TextUtil;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkMttTexture;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Font
 *
 * @author maeda6uiui
 */
public class VkMttFont extends VkMttComponent {
    private VkDevice device;

    private Map<Character, Glyph> glyphs;
    private VkMttTexture texture;
    private VkMttTexturedQuadSingleTextureSet vkQuadSet;
    private int imageWidth;
    private int imageHeight;

    public VkMttFont(
            IMttComponentForVkMttComponent mttComponent,
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttScreen screen,
            Font font,
            boolean antiAlias,
            Color fontColor,
            String requiredChars) {
        super(mttComponent, screen, "gbuffer");

        this.device = device;

        texture = this.createFontTexture(
                commandPool,
                graphicsQueue,
                screen,
                font,
                antiAlias,
                fontColor,
                requiredChars
        );
        vkQuadSet = new VkMttTexturedQuadSingleTextureSet(
                mttComponent,
                device,
                commandPool,
                graphicsQueue,
                texture
        );
    }

    private VkMttTexture createFontTexture(
            long commandPool,
            VkQueue graphicsQueue,
            VkMttScreen screen,
            Font font,
            boolean antiAlias,
            Color fontColor,
            String requiredChars) {
        TextUtil.FontImageInfo fontImageInfo = TextUtil.createFontImage(
                font, antiAlias, fontColor, requiredChars);
        this.glyphs = fontImageInfo.glyphs;
        this.imageWidth = fontImageInfo.imageWidth;
        this.imageHeight = fontImageInfo.imageHeight;

        texture = new VkMttTexture(
                device,
                commandPool,
                graphicsQueue,
                screen,
                fontImageInfo.buffer,
                fontImageInfo.imageWidth,
                fontImageInfo.imageHeight,
                false
        );

        MemoryUtil.memFree(fontImageInfo.buffer);

        return texture;
    }

    @Override
    public void cleanup() {
        texture.cleanup();
        vkQuadSet.cleanup();
    }

    public void clear() {
        vkQuadSet.clear();
    }

    public Map<Character, Glyph> getGlyphs() {
        return new HashMap<>(glyphs);
    }

    public void prepare(
            List<String> lines,
            Vector2fc pTopLeft,
            float z,
            float glyphWidthScale,
            float lineHeightScale,
            float vOffset) {
        this.clear();

        float drawX = pTopLeft.x();
        float drawY = pTopLeft.y();

        for (var text : lines) {
            int lineHeight = TextUtil.getHeight(text, glyphs);
            float scaledLineHeight = lineHeight * lineHeightScale;

            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (ch == '\n' || ch == '\r') {
                    continue;
                }

                Glyph glyph = glyphs.get(ch);
                float uLeft = glyph.x / (float) imageWidth;
                float uRight = (glyph.x + glyph.width) / (float) imageWidth;
                float vTop = glyph.y / (float) imageHeight;
                float vBottom = (glyph.y + glyph.height) / (float) imageHeight;
                float scaledGlyphWidth = glyph.width * glyphWidthScale;

                var topLeft = new MttVertex2DUV(
                        new Vector2f(drawX, drawY),
                        new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                        new Vector2f(uLeft, vTop + vOffset));
                var bottomRight = new MttVertex2DUV(
                        new Vector2f(
                                drawX + scaledGlyphWidth,
                                drawY + scaledLineHeight),
                        new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                        new Vector2f(uRight, vBottom - vOffset));
                vkQuadSet.add(topLeft, bottomRight, z);

                drawX += scaledGlyphWidth;
            }

            drawY += scaledLineHeight;
            drawX = pTopLeft.x();
        }
    }

    public void createBuffers() {
        vkQuadSet.cleanup();
        vkQuadSet.createBuffers();
    }

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        if (!this.isVisible()) {
            return;
        }

        vkQuadSet.draw(commandBuffer, pipelineLayout);
    }

    @Override
    public void transfer(VkCommandBuffer commandBuffer) {
        if (!this.isVisible()) {
            return;
        }

        vkQuadSet.transfer(commandBuffer);
    }
}
