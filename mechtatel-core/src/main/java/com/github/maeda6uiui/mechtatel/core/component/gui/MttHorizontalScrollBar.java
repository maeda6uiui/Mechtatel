package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.*;
import java.util.Map;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Horizontal scroll bar
 *
 * @author maeda6uiui
 */
public class MttHorizontalScrollBar extends MttGuiComponent {
    public static class MttHorizontalScrollBarCreateInfo {
        public float x;
        public float y;
        public float width;
        public float height;
        public float grabWidth;
        public Color frameColor;
        public Color grabFrameColor;

        public MttHorizontalScrollBarCreateInfo setX(float x) {
            this.x = x;
            return this;
        }

        public MttHorizontalScrollBarCreateInfo setY(float y) {
            this.y = y;
            return this;
        }

        public MttHorizontalScrollBarCreateInfo setWidth(float width) {
            this.width = width;
            return this;
        }

        public MttHorizontalScrollBarCreateInfo setHeight(float height) {
            this.height = height;
            return this;
        }

        public MttHorizontalScrollBarCreateInfo setGrabWidth(float grabWidth) {
            this.grabWidth = grabWidth;
            return this;
        }

        public MttHorizontalScrollBarCreateInfo setFrameColor(Color frameColor) {
            this.frameColor = frameColor;
            return this;
        }

        public MttHorizontalScrollBarCreateInfo setGrabFrameColor(Color grabFrameColor) {
            this.grabFrameColor = grabFrameColor;
            return this;
        }
    }

    private MttQuad2D frame;
    private MttQuad2D grabFrame;

    private Vector2f grabTopLeft;
    private Vector2f grabBottomRight;
    private float prevFCursorX;
    private boolean grabbed;

    public MttHorizontalScrollBar(MttVulkanImpl vulkanImpl, MttScreen screen, MttHorizontalScrollBarCreateInfo createInfo) {
        super(createInfo.x, createInfo.y, createInfo.width, createInfo.height);

        frame = new MttQuad2D(
                vulkanImpl,
                new Vector2f(createInfo.x, createInfo.y),
                new Vector2f(createInfo.x + createInfo.width, createInfo.y + createInfo.height),
                0.0f,
                false,
                convertJavaColorToJOMLVector4f(createInfo.frameColor)
        );
        grabFrame = new MttQuad2D(
                vulkanImpl,
                new Vector2f(createInfo.x, createInfo.y),
                new Vector2f(createInfo.x + createInfo.grabWidth, createInfo.y + createInfo.height),
                0.01f,
                true,
                convertJavaColorToJOMLVector4f(createInfo.grabFrameColor)
        );

        frame.setDrawOrder(1);

        grabTopLeft = new Vector2f(createInfo.x, createInfo.y);
        grabBottomRight = new Vector2f(createInfo.x + createInfo.grabWidth, createInfo.y + createInfo.height);

        prevFCursorX = 0.0f;
        grabbed = false;
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

        float fCursorX = (float) cursorX / (float) windowWidth * 2.0f - 1.0f;
        float fCursorY = (float) cursorY / (float) windowHeight * 2.0f - 1.0f;

        if ((grabTopLeft.x < fCursorX && fCursorX < grabBottomRight.x)
                && (grabTopLeft.y < fCursorY && fCursorY < grabBottomRight.y)) {
            if (lButtonPressingCount > 0) {
                grabbed = true;
            }
        }
        if (lButtonPressingCount == 0) {
            grabbed = false;
        }

        if (grabbed) {
            float diffFCursorX = fCursorX - prevFCursorX;

            float grabMoveAmount;
            float frameX = this.getX();
            float frameWidth = this.getWidth();
            if (grabTopLeft.x + diffFCursorX < frameX) {
                grabMoveAmount = frameX - grabTopLeft.x;
            } else if (grabBottomRight.x + diffFCursorX > frameX + frameWidth) {
                grabMoveAmount = frameX + frameWidth - grabBottomRight.x;
            } else {
                grabMoveAmount = diffFCursorX;
            }

            grabTopLeft.x += grabMoveAmount;
            grabBottomRight.x += grabMoveAmount;

            grabFrame.applyMat(new Matrix4f().translate(grabMoveAmount, 0.0f, 0.0f));
        }

        prevFCursorX = fCursorX;
    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
        grabFrame.setVisible(visible);
    }

    public float getScrollAmount() {
        float grabWidth = grabBottomRight.x - grabTopLeft.x;
        float grabWholeAmount = this.getWidth() - grabWidth;
        float scrollAmount = (grabTopLeft.x - this.getX()) / grabWholeAmount;

        return scrollAmount;
    }

    public void setScrollAmount(float scrollAmount) {
        if (scrollAmount < 0.0f) {
            scrollAmount = 0.0f;
        } else if (scrollAmount > 1.0f) {
            scrollAmount = 1.0f;
        }

        float grabWidth = grabBottomRight.x - grabTopLeft.x;
        float grabWholeAmount = this.getWidth() - grabWidth;
        float grabMoveAmount = scrollAmount * grabWholeAmount;

        grabFrame.applyMat(new Matrix4f().translate(grabMoveAmount, 0.0f, 0.0f));

        grabTopLeft.x = grabMoveAmount + this.getX();
        grabBottomRight.x = grabTopLeft.x + grabWidth;
    }
}
