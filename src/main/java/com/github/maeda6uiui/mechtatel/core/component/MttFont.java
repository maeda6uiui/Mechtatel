package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttFont;
import org.joml.Vector2fc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Font
 *
 * @author maeda
 */
public class MttFont extends Component {
    private VkMttFont vkMttFont;

    public MttFont(
            MttVulkanInstance vulkanInstance,
            Font font,
            boolean antiAlias,
            Color color,
            String requiredChars) {
        super(vulkanInstance);

        vkMttFont = vulkanInstance.createMttFont(font, antiAlias, color, requiredChars);
        this.associateVulkanComponent(vkMttFont);
    }

    public void clear() {
        vkMttFont.clear();
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

        vkMttFont.prepare(lines, pTopLeft, z, glyphWidthScale, lineHeightScale, vOffset);
    }

    public void createBuffers() {
        vkMttFont.createBuffers();
    }
}
