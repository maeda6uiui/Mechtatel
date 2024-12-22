package com.github.maeda6uiui.mechtatel.core.vulkan.shader;

import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;

/**
 * Provides utility methods for SPIR-V
 *
 * @author maeda6uiui
 */
public class SPIRVUtils {
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

        public byte[] bytearray() {
            bytecode.rewind();
            var bin = new byte[bytecode.remaining()];
            bytecode.get(bin);

            return bin;
        }

        @Override
        public void free() {
            shaderc_result_release(handle);
            bytecode = null;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SPIRVUtils.class);

    public static SPIRV compileShader(String source, ShaderKind kind) {
        long compiler = shaderc_compiler_initialize();
        if (compiler == NULL) {
            throw new RuntimeException("Failed to create a shader compiler");
        }

        long result = shaderc_compile_into_spv(compiler, source, kind.shaderc, "", "main", NULL);
        if (result == NULL) {
            throw new RuntimeException("Failed to compile a shader into SPIR-V");
        }

        if (shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
            String errorMessage = String.format(
                    "Failed to compile a shader into SPIR-V\n%s",
                    shaderc_result_get_error_message(result)
            );
            throw new RuntimeException(errorMessage);
        }

        shaderc_compiler_release(compiler);

        return new SPIRV(result, shaderc_result_get_bytes(result));
    }

    public static SPIRV compileShader(byte[] bin, ShaderKind kind) {
        String source = new String(bin);
        return compileShader(source, kind);
    }

    public static SPIRV compileShaderFile(URL shaderResource, ShaderKind kind) throws IOException {
        logger.debug("Compiling shader: {}", shaderResource.getPath());

        String source;
        try (var bis = new BufferedInputStream(shaderResource.openStream())) {
            byte[] bs = bis.readAllBytes();
            source = new String(bs);
        }

        return compileShader(source, kind);
    }
}
