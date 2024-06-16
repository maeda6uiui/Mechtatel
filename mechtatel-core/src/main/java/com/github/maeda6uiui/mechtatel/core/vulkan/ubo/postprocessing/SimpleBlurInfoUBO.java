package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing;

import com.github.maeda6uiui.mechtatel.core.postprocessing.blur.SimpleBlurInfo;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;
import org.joml.Vector2i;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_INT;

/**
 * Uniform buffer object for simple blur info
 *
 * @author maeda6uiui
 */
public class SimpleBlurInfoUBO extends UBO {
    public static final int SIZEOF = SIZEOF_INT * 4;

    private Vector2i textureSize;
    private int blurSize;
    private int stride;

    public SimpleBlurInfoUBO(SimpleBlurInfo blurInfo) {
        textureSize = blurInfo.getTextureSize();
        blurSize = blurInfo.getBlurSize();
        stride = blurInfo.getStride();
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        textureSize.get(0, buffer);
        buffer.putInt(SIZEOF_INT * 2, blurSize);
        buffer.putInt(SIZEOF_INT * 3, stride);

        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
