package com.github.maeda6uiui.mechtatel.core.vulkan.shader.slang;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Slang compiler
 *
 * @author maeda6uiui
 */
public class MttSlangc {
    enum DataType {
        BYTE_ARRAY,
        STRING
    }

    private static final Logger logger = LoggerFactory.getLogger(MttSlangc.class);

    private byte[] spirvCode;
    private String errorMsg;

    /**
     * Creates a new instance.
     * Module sources stored in the underlying native library are cleared upon new creation of this instance.
     */
    public MttSlangc() {
        IMttSlangc.INSTANCE.mttSlangcClearModuleSources();
    }

    private void freeMemory(PointerByReference pRef, DataType dType) {
        Pointer p = pRef.getValue();
        switch (dType) {
            case BYTE_ARRAY -> IMttSlangc.INSTANCE.mttSlangcFreeUint8t(p);
            case STRING -> IMttSlangc.INSTANCE.mttSlangcFreeStr(p);
        }
    }

    /**
     * Adds a module source to the underlying native library.
     *
     * @param moduleName Module name
     * @param source     Source code of the shader module
     */
    public void pushSource(String moduleName, String source) {
        IMttSlangc.INSTANCE.mttSlangcAddModuleSource(moduleName, source);
    }

    /**
     * Compiles module sources stored in the underlying native library.
     * Module sources must be added via {@link #pushSource(String, String)} before calling this method.
     *
     * @param entryPointName Name of the entry point (e.g. main)
     * @return Non-zero value on error, zero on success
     */
    public int compile(String entryPointName) {
        var outSpirv = new PointerByReference();
        var outSize = new LongByReference();
        var outErrorMsg = new PointerByReference();

        int result = IMttSlangc.INSTANCE.mttSlangcCompileIntoSpirv(
                entryPointName,
                outSpirv,
                outSize,
                outErrorMsg
        );
        if (result != 0) {
            Pointer pErrorMsg = outErrorMsg.getValue();
            errorMsg = pErrorMsg == null ? "Unknown error" : pErrorMsg.getString(0);
            logger.error(errorMsg);
        } else {
            Pointer pSpirv = outSpirv.getValue();
            long size = outSize.getValue();
            spirvCode = pSpirv.getByteArray(0, (int) size);
        }

        this.freeMemory(outSpirv, DataType.BYTE_ARRAY);
        this.freeMemory(outErrorMsg, DataType.STRING);

        return result;
    }

    public byte[] getSpirvCode() {
        return spirvCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
