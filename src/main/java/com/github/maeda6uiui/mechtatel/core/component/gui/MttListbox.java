package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.FilledQuad2D;
import com.github.maeda6uiui.mechtatel.core.component.Quad2D;
import com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Listbox
 *
 * @author maeda6uiui
 */
public class MttListbox extends MttGuiComponent {
    public static class MttListboxItem extends MttGuiComponent {
        private FilledQuad2D backgroundQuad;

        public MttListboxItem(
                MttVulkanInstance vulkanInstance,
                float x,
                float y,
                float width,
                float height,
                String text,
                String fontName,
                int fontStyle,
                int fontSize,
                Color fontColor,
                Color backgroundColor,
                float backgroundZ) {
            super(vulkanInstance, x, y, width, height, text, fontName, fontStyle, fontSize, fontColor, backgroundColor);

            backgroundQuad = new FilledQuad2D(
                    vulkanInstance,
                    new Vector2f(x, y),
                    new Vector2f(x + width, y + height),
                    backgroundZ,
                    ClassConversionUtils.convertJavaColorToJOMLVector4f(backgroundColor)
            );
            backgroundQuad.setVisible(false);
        }

        public void setBackgroundVisible(boolean visible) {
            backgroundQuad.setVisible(visible);
        }
    }

    private Quad2D frame;
    private List<MttListboxItem> items;

    public MttListbox(
            MttVulkanInstance vulkanInstance,
            float x,
            float y,
            float width,
            float height,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color frameColor,
            List<String> itemTexts,
            float itemHeight,
            Color backgroundColor) {
        super(vulkanInstance, x, y, width, height, "Listbox", Font.SERIF, Font.PLAIN, 50, Color.WHITE);

        this.getFont().setVisible(false);

        frame = new Quad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + height),
                0.0f,
                ClassConversionUtils.convertJavaColorToJOMLVector4f(frameColor)
        );

        items = new ArrayList<>();
        for (int i = 0; i < itemTexts.size(); i++) {
            var item = new MttListboxItem(
                    vulkanInstance, x, y + itemHeight * i, width, itemHeight,
                    itemTexts.get(i), fontName, fontStyle, fontSize, fontColor, backgroundColor, 0.01f);
            items.add(item);

            if (y + itemHeight * (i + 1) > y + height) {
                item.setVisible(false);
            }
        }
    }

    @Override
    public void update(
            int cursorX,
            int cursorY,
            int windowWidth,
            int windowHeight,
            int lButtonPressingCount,
            int mButtonPressingCount,
            int rButtonPressingCount) {
        super.update(
                cursorX, cursorY, windowWidth, windowHeight,
                lButtonPressingCount, mButtonPressingCount, rButtonPressingCount);

        items.forEach(item -> {
            item.update(
                    cursorX, cursorY, windowWidth, windowHeight,
                    lButtonPressingCount, mButtonPressingCount, rButtonPressingCount);
            if (item.isCursorOn()) {
                float bottomY = item.getY() + item.getHeight();
                float frameBottomY = this.getY() + this.getHeight();
                if (bottomY < frameBottomY) {
                    item.setBackgroundVisible(true);
                }
            } else {
                item.setBackgroundVisible(false);
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        frame.setVisible(visible);
        items.forEach(item -> item.setVisible(visible));
    }
}
