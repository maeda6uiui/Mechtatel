package com.github.maeda6uiui.mechtatel.core.screen.component.gui;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.*;
import java.util.Map;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Vertical scroll bar
 *
 * @author maeda6uiui
 */
public class MttVerticalScrollBar extends MttGuiComponent {
    public static class MttVerticalScrollBarCreateInfo {
        public float x;
        public float y;
        public float width;
        public float height;
        public float grabHeight;
        public Color frameColor;
        public Color grabFrameColor;

        public MttVerticalScrollBarCreateInfo setX(float x) {
            this.x = x;
            return this;
        }

        public MttVerticalScrollBarCreateInfo setY(float y) {
            this.y = y;
            return this;
        }

        public MttVerticalScrollBarCreateInfo setWidth(float width) {
            this.width = width;
            return this;
        }

        public MttVerticalScrollBarCreateInfo setHeight(float height) {
            this.height = height;
            return this;
        }

        public MttVerticalScrollBarCreateInfo setGrabHeight(float grabHeight) {
            this.grabHeight = grabHeight;
            return this;
        }

        public MttVerticalScrollBarCreateInfo setFrameColor(Color frameColor) {
            this.frameColor = frameColor;
            return this;
        }

        public MttVerticalScrollBarCreateInfo setGrabFrameColor(Color grabFrameColor) {
            this.grabFrameColor = grabFrameColor;
            return this;
        }
    }

    private MttQuad2D frame;
    private MttQuad2D grabFrame;

    private Vector2f grabTopLeft;
    private Vector2f grabBottomRight;
    private float prevFCursorY;
    private boolean grabbed;

    public MttVerticalScrollBar(
            MttVulkanImpl vulkanImpl, IMttScreenForMttComponent screen, MttVerticalScrollBarCreateInfo createInfo) {
        super(screen, createInfo.x, createInfo.y, createInfo.width, createInfo.height);

        frame = new MttQuad2D(
                vulkanImpl,
                screen,
                new Vector2f(createInfo.x, createInfo.y),
                new Vector2f(createInfo.x + createInfo.width, createInfo.y + createInfo.height),
                0.0f,
                false,
                convertJavaColorToJOMLVector4f(createInfo.frameColor)
        );
        grabFrame = new MttQuad2D(
                vulkanImpl,
                screen,
                new Vector2f(createInfo.x, createInfo.y),
                new Vector2f(createInfo.x + createInfo.width, createInfo.y + createInfo.grabHeight),
                0.01f,
                true,
                convertJavaColorToJOMLVector4f(createInfo.grabFrameColor)
        );

        frame.setDrawOrder(1);

        grabTopLeft = new Vector2f(createInfo.x, createInfo.y);
        grabBottomRight = new Vector2f(createInfo.x + createInfo.width, createInfo.y + createInfo.grabHeight);

        prevFCursorY = 0.0f;
        grabbed = false;
    }

    @Override
    public void cleanup() {
        super.cleanup();

        frame.cleanup();
        grabFrame.cleanup();
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
            float diffFCursorY = fCursorY - prevFCursorY;

            float grabMoveAmount;
            float frameY = this.getY();
            float frameHeight = this.getHeight();
            if (grabTopLeft.y + diffFCursorY < frameY) {
                grabMoveAmount = frameY - grabTopLeft.y;
            } else if (grabBottomRight.y + diffFCursorY > frameY + frameHeight) {
                grabMoveAmount = frameY + frameHeight - grabBottomRight.y;
            } else {
                grabMoveAmount = diffFCursorY;
            }

            grabTopLeft.y += grabMoveAmount;
            grabBottomRight.y += grabMoveAmount;

            grabFrame.applyMat(new Matrix4f().translate(0.0f, grabMoveAmount, 0.0f));
        }

        prevFCursorY = fCursorY;
    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
        grabFrame.setVisible(visible);
    }

    public float getScrollAmount() {
        float grabHeight = grabBottomRight.y - grabTopLeft.y;
        float grabWholeAmount = this.getHeight() - grabHeight;
        float scrollAmount = (grabTopLeft.y - this.getY()) / grabWholeAmount;

        return scrollAmount;
    }

    public void setScrollAmount(float scrollAmount) {
        if (scrollAmount < 0.0f) {
            scrollAmount = 0.0f;
        } else if (scrollAmount > 1.0f) {
            scrollAmount = 1.0f;
        }

        float grabHeight = grabBottomRight.y - grabTopLeft.y;
        float grabWholeAmount = this.getHeight() - grabHeight;
        float grabMoveAmount = scrollAmount * grabWholeAmount;

        grabFrame.applyMat(new Matrix4f().translate(0.0f, grabMoveAmount, 0.0f));

        grabTopLeft.y = grabMoveAmount + this.getY();
        grabBottomRight.y = grabTopLeft.y + grabHeight;
    }
}
