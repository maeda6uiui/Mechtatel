package com.github.maeda6uiui.mechtatel.core.vulkan.util;

import org.lwjgl.system.NativeResource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;

/**
 * Provides utility methods for Shaderc
 *
 * @author maeda
 */
public class ShaderSPIRVUtils {
    public enum ShaderKind {
        VERTEX_SHADER(shaderc_glsl_vertex_shader),
        GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
        FRAGMENT_SHADER(shaderc_glsl_fragment_shader);

        private final int kind;

        ShaderKind(int kind) {
            this.kind = kind;
        }
    }

    public static final class SPIRV implements NativeResource {
        private final long handle;
        private ByteBuffer bytecode;

        public SPIRV(long handle, ByteBuffer bytecode) {
            this.handle = handle;
            this.bytecode = bytecode;
        }

        public ByteBuffer bytecode() {
            return bytecode;
        }

        @Override
        public void free() {
            shaderc_result_release(handle);
            bytecode = null;
        }
    }

    public static SPIRV compileShader(String filepath, String source, ShaderKind shaderKind) {
        long compiler = shaderc_compiler_initialize();
        if (compiler == NULL) {
            throw new RuntimeException("Failed to create a shader compiler");
        }

        long result = shaderc_compile_into_spv(compiler, source, shaderKind.kind, filepath, "main", NULL);
        if (result == NULL) {
            String errorMessage = String.format("Failed to compile a shader %s into SPIR-V", filepath);
            throw new RuntimeException(errorMessage);
        }

        if (shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
            String errorMessage = String.format(
                    "Failed to compile a shader %s into SPIR-V\n%s",
                    filepath, shaderc_result_get_error_message(result));
            throw new RuntimeException(errorMessage);
        }

        shaderc_compiler_release(compiler);

        return new SPIRV(result, shaderc_result_get_bytes(result));
    }

    public static SPIRV compileShaderFile(String filepath, ShaderKind shaderKind) throws IOException {
        String source = new String(Files.readAllBytes(Paths.get(filepath)));
        return compileShader(filepath, source, shaderKind);
    }
}
