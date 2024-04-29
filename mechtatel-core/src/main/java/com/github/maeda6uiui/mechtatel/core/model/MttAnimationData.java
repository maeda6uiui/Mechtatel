package com.github.maeda6uiui.mechtatel.core.model;

import org.joml.Matrix4f;

import java.util.Arrays;

/**
 * Animation data
 *
 * @author maeda6uiui
 */
public class MttAnimationData {
    public static final Matrix4f[] DEFAULT_BONES_MATRICES = new Matrix4f[AssimpModelLoader.MAX_NUM_BONES];

    static {
        var zeroMatrix = new Matrix4f().zero();
        Arrays.fill(DEFAULT_BONES_MATRICES, zeroMatrix);
    }

    private MttModelData.Animation currentAnimation;
    private int currentFrameIdx;

    private int startFrameOffset;
    private int endFrameOffset;

    public MttAnimationData(
            MttModelData.Animation currentAnimation,
            int startFrameOffset,
            int endFrameOffset) {
        this.currentAnimation = currentAnimation;
        currentFrameIdx = startFrameOffset;

        this.startFrameOffset = startFrameOffset;
        this.endFrameOffset = endFrameOffset;
    }

    public MttModelData.Animation getCurrentAnimation() {
        return currentAnimation;
    }

    public MttModelData.AnimatedFrame getCurrentFrame() {
        return currentAnimation.frames().get(currentFrameIdx);
    }

    public int getCurrentFrameIdx() {
        return currentFrameIdx;
    }

    public void nextFrame() {
        int nextFrame = currentFrameIdx + 1;
        int endFrameIdx = currentAnimation.frames().size() - 1 + endFrameOffset;
        if (nextFrame > endFrameIdx) {
            currentFrameIdx = startFrameOffset;
        } else {
            currentFrameIdx = nextFrame;
        }
    }

    public void setCurrentAnimation(MttModelData.Animation currentAnimation) {
        this.currentAnimation = currentAnimation;
        currentFrameIdx = startFrameOffset;
    }
}
