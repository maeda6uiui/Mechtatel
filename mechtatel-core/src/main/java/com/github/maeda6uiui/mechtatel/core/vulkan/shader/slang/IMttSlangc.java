package com.github.maeda6uiui.mechtatel.core.vulkan.shader.slang;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Interface to the Slang compiler
 *
 * @author maeda6uiui
 */
public interface IMttSlangc extends Library {
    IMttSlangc INSTANCE = MttSlangcLoader.load();

    int mttSlangcCompileIntoSpirv(
            String moduleName,
            String modulePath,
            String source,
            String entryPointName,
            PointerByReference outSpirv,
            LongByReference outSize,
            PointerByReference outErrorMsg);

    void mttSlangcFreeUint8t(Pointer p);

    void mttSlangcFreeStr(Pointer p);
}
