package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Listbox
 *
 * @author maeda6uiui
 */
public class MttListbox extends MttGuiComponent {
    public static class MttListboxItem extends MttGuiComponent {
        private MttFont nonSelectedFont;
        private MttFont selectedFont;

        public MttListboxItem(
                MttVulkanInstance vulkanInstance,
                float x,
                float y,
                float width,
                float height,
                String text,
                String nonSelectedFontName,
                int nonSelectedFontStyle,
                int nonSelectedFontSize,
                Color nonSelectedFontColor,
                String selectedFontName,
                int selectedFontStyle,
                int selectedFontSize,
                Color selectedFontColor) {
            super(vulkanInstance, x, y, width, height);

            nonSelectedFont = new MttFont(
                    vulkanInstance, "default",
                    new Font(nonSelectedFontName, nonSelectedFontStyle, nonSelectedFontSize),
                    true,
                    nonSelectedFontColor,
                    text
            );
            nonSelectedFont.prepare(text, new Vector2f(x, y));
            nonSelectedFont.createBuffers();

            selectedFont = new MttFont(
                    vulkanInstance, "default",
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
            nonSelectedFont.setVisible(false);
            selectedFont.setVisible(true);
        }

        public void changeToNonSelectedFont() {
            nonSelectedFont.setVisible(true);
            selectedFont.setVisible(false);
        }

        @Override
        public void setVisible(boolean visible) {
            nonSelectedFont.setVisible(visible);
            selectedFont.setVisible(visible);
        }

        @Override
        public boolean isVisible() {
            return nonSelectedFont.isVisible() || selectedFont.isVisible();
        }

        @Override
        public void setPosition(float x, float y) {
            float diffX = x - this.getX();
            float diffY = y - this.getY();

            nonSelectedFont.applyMat(new Matrix4f().translate(diffX, diffY, 0.0f));
            selectedFont.applyMat(new Matrix4f().translate(diffX, diffY, 0.0f));

            super.setPosition(x, y);
        }
    }

    private MttQuad2D frame;
    private MttVerticalScrollbar scrollbar;
    private List<MttListboxItem> items;
    private float itemHeight;
    private int numDisplayedItems;
    private float scrollAmountPerItem;

    public MttListbox(
            MttVulkanInstance vulkanInstance,
            float x,
            float y,
            float width,
            float height,
            float scrollbarWidth,
            float scrollbarGrabHeight,
            Color scrollbarFrameColor,
            Color scrollbarGrabColor,
            String nonSelectedFontName,
            int nonSelectedFontStyle,
            int nonSelectedFontSize,
            Color nonSelectedFontColor,
            Color frameColor,
            List<String> itemTexts,
            float itemHeight,
            String selectedFontName,
            int selectedFontStyle,
            int selectedFontSize,
            Color selectedFontColor) {
        super(vulkanInstance, x, y, width, height);

        frame = new MttQuad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + height),
                0.0f,
                convertJavaColorToJOMLVector4f(frameColor)
        );

        scrollbar = new MttVerticalScrollbar(
                vulkanInstance, x + width - scrollbarWidth, y, scrollbarWidth, height, scrollbarGrabHeight,
                scrollbarFrameColor, scrollbarGrabColor
        );

        items = new ArrayList<>();
        this.itemHeight = itemHeight;
        numDisplayedItems = 0;
        for (int i = 0; i < itemTexts.size(); i++) {
            var item = new MttListboxItem(
                    vulkanInstance, x, y + itemHeight * i, width - scrollbarWidth, itemHeight, itemTexts.get(i),
                    nonSelectedFontName, nonSelectedFontStyle, nonSelectedFontSize, nonSelectedFontColor,
                    selectedFontName, selectedFontStyle, selectedFontSize, selectedFontColor);
            items.add(item);

            if (y + itemHeight * (i + 1) > y + height) {
                item.setVisible(false);
            } else {
                numDisplayedItems++;
            }

            var callbacks = new MttListboxItemDefaultCallbacks(i);
            item.setCallbacks(callbacks);
        }

        if (numDisplayedItems == items.size()) {
            scrollAmountPerItem = -1.0f;
        } else {
            scrollAmountPerItem = 1.0f / (items.size() - numDisplayedItems);
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
            int rButtonPressingCount,
            Map<String, Integer> keyboardPressingCounts) {
        super.update(
                cursorX, cursorY, windowWidth, windowHeight,
                lButtonPressingCount, mButtonPressingCount, rButtonPressingCount, keyboardPressingCounts);

        scrollbar.update(
                cursorX, cursorY, windowWidth, windowHeight,
                lButtonPressingCount, mButtonPressingCount, rButtonPressingCount, keyboardPressingCounts);

        float scrollAmount = scrollbar.getScrollAmount();
        if (scrollAmountPerItem > 0.0f) {
            int expectedTopItemIndex = Math.round(scrollAmount / scrollAmountPerItem);
            int expectedBottomItemIndex = expectedTopItemIndex + numDisplayedItems - 1;

            for (int i = 0; i < items.size(); i++) {
                var item = items.get(i);

                if (i >= expectedTopItemIndex && i <= expectedBottomItemIndex) {
                    float expectedItemDiffY = itemHeight * (i - expectedTopItemIndex);
                    item.setPosition(this.getX(), this.getY() + expectedItemDiffY);

                    item.setVisible(true);
                } else {
                    item.setVisible(false);
                }
            }
        }

        items.forEach(item -> {
            item.update(
                    cursorX, cursorY, windowWidth, windowHeight,
                    lButtonPressingCount, mButtonPressingCount, rButtonPressingCount, keyboardPressingCounts);
            if (item.isVisible()) {
                if (item.isCursorOn()) {
                    item.changeToSelectedFont();
                } else {
                    item.changeToNonSelectedFont();
                }
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
        items.forEach(item -> item.setVisible(visible));
    }

    public void setScrollbarItemCallbacks(int index, MttGuiComponentCallbacks callbacks) {
        items.get(index).setCallbacks(callbacks);
    }
}
