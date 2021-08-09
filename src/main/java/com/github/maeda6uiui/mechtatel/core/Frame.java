package com.github.maeda6uiui.mechtatel.core;

import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;

/**
 * Frame
 *
 * @author maeda
 */
class Frame {
    private final long imageAvailableSemaphore;
    private final long renderFinishedSemaphore;
    private final long fence;

    public Frame(long imageAvailableSemaphore, long renderFinishedSemaphore, long fence) {
        this.imageAvailableSemaphore = imageAvailableSemaphore;
        this.renderFinishedSemaphore = renderFinishedSemaphore;
        this.fence = fence;
    }

    public long imageAvailableSemaphore() {
        return imageAvailableSemaphore;
    }

    public LongBuffer pImageAvailableSemaphore() {
        return MemoryStack.stackGet().longs(imageAvailableSemaphore);
    }

    public long renderFinishedSemaphore() {
        return renderFinishedSemaphore;
    }

    public LongBuffer pRenderFinishedSemaphore() {
        return MemoryStack.stackGet().longs(renderFinishedSemaphore);
    }

    public long fence() {
        return fence;
    }

    public LongBuffer pFence() {
        return MemoryStack.stackGet().longs(fence);
    }
}
