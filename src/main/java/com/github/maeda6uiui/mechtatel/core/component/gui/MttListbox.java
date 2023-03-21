package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttFont;
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
        private MttFont selectedFont;

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
                String selectedFontName,
                int selectedFontStyle,
                int selectedFontSize,
                Color selectedFontColor) {
            super(vulkanInstance, x, y, width, height, text, fontName, fontStyle, fontSize, fontColor);

            selectedFont = new MttFont(
                    vulkanInstance,
                    new Font(selectedFontName, selectedFontStyle, selectedFontSize),
                    true,
                    selectedFontColor,
                    text
            );
            selectedFont.prepare(text, new Vector2f(x, y));
            selectedFont.createBuffers();
            selectedFont.setVisible(false);
        }

        public void changeToSelectedFont() {
            this.getFont().setVisible(false);
            selectedFont.setVisible(true);
        }

        public void changeToNonSelectedFont() {
            this.getFont().setVisible(true);
            selectedFont.setVisible(false);
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            selectedFont.setVisible(visible);
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() || selectedFont.isVisible();
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
            String selectedFontName,
            int selectedFontStyle,
            int selectedFontSize,
            Color selectedFontColor) {
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
                    itemTexts.get(i), fontName, fontStyle, fontSize, fontColor,
                    selectedFontName, selectedFontStyle, selectedFontSize, selectedFontColor);
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
            if (item.isVisible()) {
                if (item.isCursorOn()) {
                    item.changeToSelectedFont();
                } else {
                    item.changeToNonSelectedFont();
                }
            } else {

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
