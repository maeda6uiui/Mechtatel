package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.MttFilledQuad2D;
import com.github.maeda6uiui.mechtatel.core.component.MttQuad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.*;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Vertical scrollbar
 *
 * @author maeda6uiui
 */
public class MttVerticalScrollbar extends MttGuiComponent {
    private MttQuad2D frame;
    private MttFilledQuad2D grabFrame;

    private Vector2f grabTopLeft;
    private Vector2f grabBottomRight;
    private float prevFCursorY;
    private boolean grabbed;

    public MttVerticalScrollbar(
            MttVulkanInstance vulkanInstance,
            float x,
            float y,
            float width,
            float height,
            float grabHeight,
            Color frameColor,
            Color grabFrameColor) {
        super(vulkanInstance, x, y, width, height);

        frame = new MttQuad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + height),
                0.0f,
                convertJavaColorToJOMLVector4f(frameColor)
        );
        grabFrame = new MttFilledQuad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + grabHeight),
                0.01f,
                convertJavaColorToJOMLVector4f(grabFrameColor)
        );

        frame.setDrawOrder(1);
        vulkanInstance.sortComponents();

        grabTopLeft = new Vector2f(x, y);
        grabBottomRight = new Vector2f(x + width, y + grabHeight);

        prevFCursorY = 0.0f;
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
            int rButtonPressingCount) {
        super.update(
                cursorX, cursorY, windowWidth, windowHeight,
                lButtonPressingCount, mButtonPressingCount, rButtonPressingCount);

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
}
