#include <array>
#include <cstring>
#include <iostream>
#include <string>
#include <vector>
#include "slang.h"
#include "slang-com-ptr.h"
#include "slang-com-helper.h"
#include "mtt-main.h"

MttSlangc::MttSlangc()
{
    slang::createGlobalSession(this->globalSession.writeRef());
}

template <class T>
T *MttSlangc::leak(Slang::ComPtr<slang::IBlob> blob)
{
    auto ptr = (T *)blob->getBufferPointer();
    size_t len = blob->getBufferSize();

    auto duplicatedPtr = new T[len];
    for (int i = 0; i < len; i++)
    {
        duplicatedPtr[i] = ptr[i];
    }

    return duplicatedPtr;
}

char *MttSlangc::leak_from_str(const char *src)
{
    char *dest = new char[strlen(src) + 1];
    strncpy(dest, src, strlen(src) + 1);
    return dest;
}

template <class T>
void MttSlangc::free(const T *p)
{
    delete[] p;
}

void MttSlangc::addModuleSource(const char *moduleName, const char *source)
{
    auto moduleSource = MttSlangModuleSource{};
    if (moduleName == nullptr)
    {
        moduleSource.moduleName = std::string("");
    }
    else
    {
        moduleSource.moduleName = std::string(moduleName);
    }
    moduleSource.source = std::string(source);
    this->moduleSources.push_back(moduleSource);
}

int MttSlangc::compile(
    const char *entryPointName,
    uint8_t **outSpirv,
    size_t *outSize,
    char **outErrorMsg)
{
    // Set up a session
    slang::SessionDesc sessionDesc = {};

    slang::TargetDesc targetDesc = {};
    targetDesc.format = SLANG_SPIRV;
    targetDesc.profile = this->globalSession->findProfile("spirv_1_5");

    sessionDesc.targets = &targetDesc;
    sessionDesc.targetCount = 1;

    std::array<slang::CompilerOptionEntry, 1> options = {
        {slang::CompilerOptionName::EmitSpirvDirectly,
         {slang::CompilerOptionValueKind::Int, 1, 0, nullptr, nullptr}}};
    sessionDesc.compilerOptionEntries = options.data();
    sessionDesc.compilerOptionEntryCount = options.size();

    Slang::ComPtr<slang::ISession> session;
    globalSession->createSession(sessionDesc, session.writeRef());

    // Load modules
    std::vector<Slang::ComPtr<slang::IModule>> slangModules;
    {
        for (const MttSlangModuleSource &moduleSource : this->moduleSources)
        {
            Slang::ComPtr<slang::IModule> slangModule;
            Slang::ComPtr<slang::IBlob> diagnosticsBlob;

            slangModule = session->loadModuleFromSourceString(
                moduleSource.moduleName.c_str(),
                nullptr,
                moduleSource.source.c_str(),
                diagnosticsBlob.writeRef());
            if (!slangModule)
            {
                if (diagnosticsBlob != nullptr)
                {
                    *outErrorMsg = this->leak<char>(diagnosticsBlob);
                }
                return -1;
            }
        }
    }

    // Query entry points
    Slang::ComPtr<slang::IEntryPoint> entryPoint;
    {
        for (auto slangModule : slangModules)
        {
            slangModule->findEntryPointByName(entryPointName, entryPoint.writeRef());
            if (!entryPoint)
            {
                break;
            }
        }
    }
    if (!entryPoint)
    {
        auto errorMsg = std::string("Error getting entry point: ");
        errorMsg += std::string(entryPointName);
        *outErrorMsg = this->leak_from_str(errorMsg.c_str());

        return -1;
    }

    // Compose modules and entry points
    std::vector<slang::IComponentType *> componentTypes;
    for (auto slangModule : slangModules)
    {
        componentTypes.push_back(slangModule);
    }
    componentTypes.push_back(entryPoint);

    Slang::ComPtr<slang::IComponentType> composedProgram;
    {
        Slang::ComPtr<slang::IBlob> diagnosticsBlob;
        SlangResult result = session->createCompositeComponentType(
            componentTypes.data(),
            componentTypes.size(),
            composedProgram.writeRef(),
            diagnosticsBlob.writeRef());
        if (diagnosticsBlob != nullptr)
        {
            *outErrorMsg = this->leak<char>(diagnosticsBlob);
        }
        SLANG_RETURN_ON_FAIL(result);
    }

    // Link
    Slang::ComPtr<slang::IComponentType> linkedProgram;
    {
        Slang::ComPtr<slang::IBlob> diagnosticsBlob;
        SlangResult result = composedProgram->link(linkedProgram.writeRef(), diagnosticsBlob.writeRef());
        if (diagnosticsBlob != nullptr)
        {
            *outErrorMsg = this->leak<char>(diagnosticsBlob);
        }
        SLANG_RETURN_ON_FAIL(result);
    }

    // Get target kernel code
    Slang::ComPtr<slang::IBlob> spirvCode;
    {
        Slang::ComPtr<slang::IBlob> diagnosticsBlob;
        SlangResult result = linkedProgram->getEntryPointCode(0, 0, spirvCode.writeRef(), diagnosticsBlob.writeRef());
        if (diagnosticsBlob != nullptr)
        {
            *outErrorMsg = this->leak<char>(diagnosticsBlob);
        }
        SLANG_RETURN_ON_FAIL(result);
    }
    *outSpirv = this->leak<uint8_t>(spirvCode);
    *outSize = spirvCode->getBufferSize();

    return 0;
}

//===== C interface =====
const static auto slangcInstance = new MttSlangc();

void mttSlangcAddModuleSource(const char *moduleName, const char *source)
{
    slangcInstance->addModuleSource(moduleName, source);
}

int mttSlangcCompileIntoSpirv(
    const char *entryPointName,
    uint8_t **outSpirv,
    size_t *outSize,
    char **outErrorMsg)
{
    return slangcInstance->compile(
        entryPointName, outSpirv, outSize, outErrorMsg);
}

void mttSlangcFreeUint8t(const uint8_t *p)
{
    slangcInstance->free(p);
}

void mttSlangcFreeStr(const char *p)
{
    slangcInstance->free(p);
}
