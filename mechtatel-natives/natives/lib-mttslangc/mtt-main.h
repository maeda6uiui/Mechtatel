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
#ifdef _WIN32
#define DLL_EXPORT __declspec(dllexport)
#else
#define DLL_EXPORT
#endif

extern "C" DLL_EXPORT int mttSlangcCompileIntoSpirv(
    const char *mainModuleFilepath,
    uint8_t **outSpirv,
    size_t *outSize,
    char **outErrorMsg);
extern "C" DLL_EXPORT void mttSlangcFreeUint8t(const uint8_t *p);
extern "C" DLL_EXPORT void mttSlangcFreeStr(const char *p);
