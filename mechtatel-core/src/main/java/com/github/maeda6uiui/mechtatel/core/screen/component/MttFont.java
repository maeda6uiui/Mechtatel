package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.text.Glyph;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttFont;
import org.joml.Vector2fc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Font
 *
 * @author maeda6uiui
 */
public class MttFont extends MttComponent {
    public static final float DEFAULT_GLYPH_WIDTH_SCALE = 0.001f;
    public static final float DEFAULT_LINE_HEIGHT_SCALE = 0.002f;
    public static final float DEFAULT_V_OFFSET = 0.01f;

    private VkMttFont vkMttFont;

    public MttFont(
            MttVulkanImpl vulkanImpl,
            IMttScreenForMttComponent screen,
            Font font,
            boolean antiAlias,
            Color fontColor,
            String requiredChars) {
        super(
                screen,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(true)
                        .setCastShadow(false)
                        .setDrawOrder(0)
        );

        var dq = vulkanImpl.getDeviceAndQueues();
        vkMttFont = new VkMttFont(
                this,
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                font,
                antiAlias,
                fontColor,
                requiredChars
        );
        this.associateVulkanComponent(vkMttFont);
    }

    public void clear() {
        vkMttFont.clear();
    }

    public Map<Character, Glyph> getGlyphs() {
        return vkMttFont.getGlyphs();
    }

    public void prepare(
            List<String> lines,
            Vector2fc pTopLeft,
            float z,
            float glyphWidthScale,
            float lineHeightScale,
            float vOffset) {
        vkMttFont.prepare(lines, pTopLeft, z, glyphWidthScale, lineHeightScale, vOffset);
    }

    public void prepare(
            String text,
            Vector2fc pTopLeft,
            float z,
            float glyphWidthScale,
            float lineHeightScale,
            float vOffset) {
        var lines = new ArrayList<String>();
        if (text.contains("\n")) {
            String[] splits = text.split("\n");
            for (var split : splits) {
                lines.add(split);
            }
        } else {
            lines.add(text);
        }

        this.prepare(lines, pTopLeft, z, glyphWidthScale, lineHeightScale, vOffset);
    }

    public void prepare(String text, Vector2fc pTopLeft) {
        this.prepare(
                text,
                pTopLeft,
                0.0f,
                DEFAULT_GLYPH_WIDTH_SCALE,
                DEFAULT_LINE_HEIGHT_SCALE,
                DEFAULT_V_OFFSET);
    }

    public void createBuffers() {
        vkMttFont.createBuffers();
    }
}
