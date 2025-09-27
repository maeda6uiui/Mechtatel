#include "slang.h"
#include "slang-com-ptr.h"
#include "slang-com-helper.h"

class MttSlangc
{
private:
    Slang::ComPtr<slang::IGlobalSession> globalSession;

    template <class T>
    T *leak(Slang::ComPtr<slang::IBlob> blob);
    char *leak_from_str(const char *src);

public:
    MttSlangc();
    int compile(
        const char *mainModuleFilepath,
        uint8_t **outSpirv,
        size_t *outSize,
        char **outErrorMsg);

    template <class T>
    void free(const T *p);
};

//===== C interface =====
extern "C" int mttSlangcCompileIntoSpirv(
    const char *mainModuleFilepath,
    uint8_t **outSpirv,
    size_t *outSize,
    char **outErrorMsg);
extern "C" void mttSlangcFreeUint8t(const uint8_t *p);
extern "C" void mttSlangcFreeStr(const char *p);
