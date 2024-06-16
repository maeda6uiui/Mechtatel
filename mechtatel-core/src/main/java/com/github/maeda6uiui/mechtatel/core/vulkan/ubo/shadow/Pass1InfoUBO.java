package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.shadow;

import com.github.maeda6uiui.mechtatel.core.shadow.Pass1Info;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_MAT4;

/**
 * Uniform buffer object for pass 1 info
 *
 * @author maeda6uiui
 */
public class Pass1InfoUBO extends UBO {
    public static final int SIZEOF = SIZEOF_MAT4 * 2;

    private Matrix4f lightView;
    private Matrix4f lightProj;

    public Pass1InfoUBO(Pass1Info info) {
        lightView = info.getLightView();
        lightProj = info.getLightProj();
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        lightView.get(0, buffer);
        lightProj.get(1 * SIZEOF_MAT4, buffer);

        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
