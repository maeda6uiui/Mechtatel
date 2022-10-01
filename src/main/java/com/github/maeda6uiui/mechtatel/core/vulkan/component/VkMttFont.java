package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.component.Vertex2DUV;
import com.github.maeda6uiui.mechtatel.core.text.Glyph;
import com.github.maeda6uiui.mechtatel.core.text.TextUtil;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.Texture;
import org.joml.Vector2f;
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

        var topLeft = new Vertex2DUV(new Vector2f(-1.0f, -1.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 0.0f));
        var bottomRight = new Vertex2DUV(new Vector2f(1.0f, 1.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        vkQuadSet.add(topLeft, bottomRight, 0.0f);
        vkQuadSet.createBuffers();

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

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        vkQuadSet.draw(commandBuffer, pipelineLayout);
    }

    @Override
    public void transfer(VkCommandBuffer commandBuffer) {
        vkQuadSet.transfer(commandBuffer);
    }
}
