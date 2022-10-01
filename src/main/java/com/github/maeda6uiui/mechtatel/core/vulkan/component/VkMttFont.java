package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.component.Vertex2DUV;
import com.github.maeda6uiui.mechtatel.core.text.Glyph;
import com.github.maeda6uiui.mechtatel.core.text.TextUtil;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.Texture;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkCommandBuffer;
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
public class VkMttFont extends VkComponent {
    private VkDevice device;

    private Map<Character, Glyph> glyphs;
    private Texture texture;
    private VkTexturedQuad2DSingleTextureSet vkQuadSet;
    private int imageWidth;
    private int imageHeight;

    public VkMttFont(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<Long> descriptorSets,
            int setCount,
            Font font,
            boolean antiAlias,
            Color color) {
        this.device = device;

        texture = this.createFontTexture(
                commandPool,
                graphicsQueue,
                descriptorSets,
                setCount,
                font,
                antiAlias,
                color
        );
        vkQuadSet = new VkTexturedQuad2DSingleTextureSet(
                device,
                commandPool,
                graphicsQueue,
                texture
        );

        this.setComponentType("gbuffer");
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
        this.imageWidth = fontImageInfo.imageWidth;
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

    @Override
    public void cleanup() {
        texture.cleanup();
        vkQuadSet.cleanup();
    }

    public void clear() {
        vkQuadSet.clear(false);
    }

    public void prepare(List<String> lines, Vector2fc pTopLeft, float z) {
        float drawX = pTopLeft.x();
        float drawY = pTopLeft.y();

        for (var line : lines) {
            CharSequence text = line;

            int textHeight = TextUtil.getHeight(text, glyphs);
            float normalizedTextHeight = textHeight / (float) imageHeight / 2.0f;

            int textWidth = TextUtil.getWidth(text, glyphs);

            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (ch == '\n' || ch == '\r') {
                    continue;
                }

                Glyph glyph = glyphs.get(ch);
                float uLeft = glyph.x / (float) imageWidth;
                float uRight = (glyph.x + glyph.width) / (float) imageWidth;
                float normalizedGlyphWidth = glyph.width / (float) textWidth / 2.0f;

                var topLeft = new Vertex2DUV(
                        new Vector2f(drawX, drawY), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(uLeft, 0.0f));
                var bottomRight = new Vertex2DUV(
                        new Vector2f(drawX + normalizedGlyphWidth, drawY + normalizedTextHeight), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(uRight, -1.0f));
                vkQuadSet.add(topLeft, bottomRight, z);

                drawX += normalizedGlyphWidth;
            }

            drawY += normalizedTextHeight;
            drawX = pTopLeft.x();
        }
    }

    public void createBuffers() {
        vkQuadSet.createBuffers();
    }

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        vkQuadSet.draw(commandBuffer, pipelineLayout);
    }

    @Override
    public void transfer(VkCommandBuffer commandBuffer) {
        vkQuadSet.transfer(commandBuffer);
    }
}
