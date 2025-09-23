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
interface IMttSlangc extends Library {
    IMttSlangc INSTANCE = MttSlangcLoader.load();

    void mttSlangcAddModuleSource(String moduleName, String source);

    int mttSlangcCompileIntoSpirv(
            String entryPointName,
            PointerByReference outSpirv,
            LongByReference outSize,
            PointerByReference outErrorMsg);

    void mttSlangcFreeUint8t(Pointer p);

    void mttSlangcFreeStr(Pointer p);
}
