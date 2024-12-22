package com.github.maeda6uiui.mechtatel.core.vulkan.shader;

import static org.lwjgl.util.shaderc.Shaderc.*;

/**
 * Kind of shader
 *
 * @author maeda6uiui
 */
public enum ShaderKind {
    VERTEX(shaderc_glsl_vertex_shader),
    GEOMETRY(shaderc_glsl_geometry_shader),
    FRAGMENT(shaderc_glsl_fragment_shader);

    public final int shaderc;

    ShaderKind(int shaderc) {
        this.shaderc = shaderc;
    }
}
