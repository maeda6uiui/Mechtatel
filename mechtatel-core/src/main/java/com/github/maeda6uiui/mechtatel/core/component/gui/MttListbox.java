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

    public static class MttListboxCreateInfo {
        public float x;
        public float y;
        public float width;
        public float height;
        public float scrollbarWidth;
        public float scrollbarGrabHeight;
        public Color scrollbarFrameColor;
        public Color scrollbarGrabColor;
        public String nonSelectedFontName;
        public int nonSelectedFontStyle;
        public int nonSelectedFontSize;
        public Color nonSelectedFontColor;
        public Color frameColor;
        public List<String> itemTexts;
        public float itemHeight;
        public String selectedFontName;
        public int selectedFontStyle;
        public int selectedFontSize;
        public Color selectedFontColor;

        public MttListboxCreateInfo setX(float x) {
            this.x = x;
            return this;
        }

        public MttListboxCreateInfo setY(float y) {
            this.y = y;
            return this;
        }

        public MttListboxCreateInfo setWidth(float width) {
            this.width = width;
            return this;
        }

        public MttListboxCreateInfo setHeight(float height) {
            this.height = height;
            return this;
        }

        public MttListboxCreateInfo setScrollbarWidth(float scrollbarWidth) {
            this.scrollbarWidth = scrollbarWidth;
            return this;
        }

        public MttListboxCreateInfo setScrollbarGrabHeight(float scrollbarGrabHeight) {
            this.scrollbarGrabHeight = scrollbarGrabHeight;
            return this;
        }

        public MttListboxCreateInfo setScrollbarFrameColor(Color scrollbarFrameColor) {
            this.scrollbarFrameColor = scrollbarFrameColor;
            return this;
        }

        public MttListboxCreateInfo setScrollbarGrabColor(Color scrollbarGrabColor) {
            this.scrollbarGrabColor = scrollbarGrabColor;
            return this;
        }

        public MttListboxCreateInfo setNonSelectedFontName(String nonSelectedFontName) {
            this.nonSelectedFontName = nonSelectedFontName;
            return this;
        }

        public MttListboxCreateInfo setNonSelectedFontStyle(int nonSelectedFontStyle) {
            this.nonSelectedFontStyle = nonSelectedFontStyle;
            return this;
        }

        public MttListboxCreateInfo setNonSelectedFontSize(int nonSelectedFontSize) {
            this.nonSelectedFontSize = nonSelectedFontSize;
            return this;
        }

        public MttListboxCreateInfo setNonSelectedFontColor(Color nonSelectedFontColor) {
            this.nonSelectedFontColor = nonSelectedFontColor;
            return this;
        }

        public MttListboxCreateInfo setFrameColor(Color frameColor) {
            this.frameColor = frameColor;
            return this;
        }

        public MttListboxCreateInfo setItemTexts(List<String> itemTexts) {
            this.itemTexts = itemTexts;
            return this;
        }

        public MttListboxCreateInfo setItemHeight(float itemHeight) {
            this.itemHeight = itemHeight;
            return this;
        }

        public MttListboxCreateInfo setSelectedFontName(String selectedFontName) {
            this.selectedFontName = selectedFontName;
            return this;
        }

        public MttListboxCreateInfo setSelectedFontStyle(int selectedFontStyle) {
            this.selectedFontStyle = selectedFontStyle;
            return this;
        }

        public MttListboxCreateInfo setSelectedFontSize(int selectedFontSize) {
            this.selectedFontSize = selectedFontSize;
            return this;
        }

        public MttListboxCreateInfo setSelectedFontColor(Color selectedFontColor) {
            this.selectedFontColor = selectedFontColor;
            return this;
        }
    }

    private MttQuad2D frame;
    private MttVerticalScrollbar scrollbar;
    private List<MttListboxItem> items;
    private float itemHeight;
    private int numDisplayedItems;
    private float scrollAmountPerItem;

    public MttListbox(MttVulkanInstance vulkanInstance, MttListboxCreateInfo createInfo) {
        super(vulkanInstance, createInfo.x, createInfo.y, createInfo.width, createInfo.height);

        frame = new MttQuad2D(
                vulkanInstance,
                new Vector2f(createInfo.x, createInfo.y),
                new Vector2f(createInfo.x + createInfo.width, createInfo.y + createInfo.height),
                0.0f,
                false,
                convertJavaColorToJOMLVector4f(createInfo.frameColor)
        );

        scrollbar = new MttVerticalScrollbar(
                vulkanInstance,
                new MttVerticalScrollbar.MttVerticalScrollbarCreateInfo()
                        .setX(createInfo.x + createInfo.width - createInfo.scrollbarWidth)
                        .setY(createInfo.y)
                        .setWidth(createInfo.scrollbarWidth)
                        .setHeight(createInfo.height)
                        .setGrabHeight(createInfo.scrollbarGrabHeight)
                        .setFrameColor(createInfo.scrollbarFrameColor)
                        .setGrabFrameColor(createInfo.scrollbarGrabColor)
        );

        items = new ArrayList<>();
        this.itemHeight = createInfo.itemHeight;
        numDisplayedItems = 0;
        for (int i = 0; i < createInfo.itemTexts.size(); i++) {
            var item = new MttListboxItem(
                    vulkanInstance,
                    createInfo.x,
                    createInfo.y + itemHeight * i,
                    createInfo.width - createInfo.scrollbarWidth,
                    itemHeight,
                    createInfo.itemTexts.get(i),
                    createInfo.nonSelectedFontName,
                    createInfo.nonSelectedFontStyle,
                    createInfo.nonSelectedFontSize,
                    createInfo.nonSelectedFontColor,
                    createInfo.selectedFontName,
                    createInfo.selectedFontStyle,
                    createInfo.selectedFontSize,
                    createInfo.selectedFontColor
            );
            items.add(item);

            if (createInfo.y + itemHeight * (i + 1) > createInfo.y + createInfo.height) {
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