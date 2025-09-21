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
        const char *moduleName,
        const char *modulePath,
        const char *source,
        const char *entryPointName,
        uint8_t **outSpirv,
        size_t *outSize,
        char **outErrorMsg);

    template <class T>
    void free(const T *p);
};

//===== C interface =====
extern "C" int mtt_slangc_compile_to_spirv(
    const char *moduleName,
    const char *modulePath,
    const char *source,
    const char *entryPointName,
    uint8_t **outSpirv,
    size_t *outSize,
    char **outErrorMsg);
extern "C" void mtt_slangc_free_uint8_t(const uint8_t *p);
extern "C" void mtt_slangc_free_str(const char *p);
