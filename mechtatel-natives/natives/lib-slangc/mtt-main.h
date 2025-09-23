#include <string>
#include <vector>
#include "slang.h"
#include "slang-com-ptr.h"
#include "slang-com-helper.h"

struct MttSlangModuleSource
{
    std::string moduleName;
    std::string source;
};

class MttSlangc
{
private:
    Slang::ComPtr<slang::IGlobalSession> globalSession;
    std::vector<MttSlangModuleSource> moduleSources;

    template <class T>
    T *leak(Slang::ComPtr<slang::IBlob> blob);
    char *leak_from_str(const char *src);

public:
    MttSlangc();
    void addModuleSource(const char *moduleName, const char *source);
    int compile(
        const char *entryPointName,
        uint8_t **outSpirv,
        size_t *outSize,
        char **outErrorMsg);

    template <class T>
    void free(const T *p);
};

//===== C interface =====
extern "C" void mttSlangcAddModuleSource(const char *moduleName, const char *source);
extern "C" int mttSlangcCompileIntoSpirv(
    const char *entryPointName,
    uint8_t **outSpirv,
    size_t *outSize,
    char **outErrorMsg);
extern "C" void mttSlangcFreeUint8t(const uint8_t *p);
extern "C" void mttSlangcFreeStr(const char *p);
