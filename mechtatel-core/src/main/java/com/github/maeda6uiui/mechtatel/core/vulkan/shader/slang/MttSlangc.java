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

    public MttSlangc() {

    }

    private void freeMemory(PointerByReference pRef, DataType dType) {
        Pointer p = pRef.getValue();
        switch (dType) {
            case BYTE_ARRAY -> IMttSlangc.INSTANCE.mttSlangcFreeUint8t(p);
            case STRING -> IMttSlangc.INSTANCE.mttSlangcFreeStr(p);
        }
    }

    /**
     * Compiles module sources stored on the disk.
     * Compilation output (SPIR-V) can be obtained with {@link #getSpirvCode()} after this method succeeds.
     *
     * @param mainModuleFilepath Filepath of the shader file that has the entrypoint in it
     * @return Non-zero value on error, zero on success
     */
    public int compile(String mainModuleFilepath) {
        var outSpirv = new PointerByReference();
        var outSize = new LongByReference();
        var outErrorMsg = new PointerByReference();

        int result = IMttSlangc.INSTANCE.mttSlangcCompileIntoSpirv(
                mainModuleFilepath,
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
