# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Mechtatel is a Vulkan-based game engine in Java built on LWJGL. Java 21 is required; Vulkan 1.3 is the default target. The repo is a multi-module Maven project plus two native sub-projects (C++ for Slang, Rust for audio playback).

## Build commands

```bash
# Initial clone needs submodules: shader-slang/slang and imgui-java
git submodule update --init

# Build all modules (uses the active OS/arch profile to select the right
# platform-pack + audio-natives via the mechtatel.platformPack and
# mechtatel.audio.natives properties — see the profiles in the root pom.xml)
mvn -DskipTests install

# Skip GPG signing for local builds (the parent pom binds maven-gpg-plugin
# to the verify phase)
mvn -DskipTests -Dgpg.skip install

# Build a single module (Maven's reactor will fetch siblings from your
# local repo, so install the parent first)
mvn -pl mechtatel-core -am install
```

## Tests / demo runners

The classes under `mechtatel-core/src/test/java/.../core/` are **interactive Vulkan demo apps**, not JUnit tests — every one has a `public static void main(String[] args)` and none use `@Test`. They open a window, load `./Mechtatel/settings.json` (relative to the working directory; the repo root contains a `Mechtatel/` resource folder with this file plus the `Standard/` asset tree), and require a working Vulkan driver. `mvn test` will not exercise them.

Run a single demo from the repo root, e.g.:

```bash
mvn -pl mechtatel-core -am test-compile
mvn -pl mechtatel-core exec:java \
    -Dexec.classpathScope=test \
    -Dexec.mainClass=com.github.maeda6uiui.mechtatel.core.SpotlightTest
```

`HeadlessModeTest` / `HeadlessModeTest2` / `ImGuiHeadlessTest` exercise the headless render path and don't need a display.

## High-level architecture

### Module layout

The Maven reactor (`pom.xml`) groups modules into a few families:

- **`mechtatel-core`** — engine entry points (`Mechtatel`, `MttWindow`, `MttHeadlessInstance`), settings/shader-config loading, and `core.vulkan.*` which contains `MttVulkanInstance` plus all Vulkan plumbing (`nabor` = render-pass + pipeline bundles, `screen`, `shader`, `swapchain`, `ubo`, `drawer`, `cache`, `creator`). Higher-level scene types (`screen.component`, `model`, `physics`, `postprocessing`, `shadow`, `fseffect`, `text`, `sound`, `camera`, `input`) sit on top of the Vulkan layer. Standard GLSL shaders live under `src/main/resources/Standard/Shader/{GBuffer,FullScreenEffect,PostProcessing,Primitive,Present,ShadowMapping,MergeScenes,TextureOperation}`.
- **`mechtatel-audio`** — pure-Java JNA wrapper around a Rust audio library; usable independently of `mechtatel-core`.
- **`mechtatel-natives`** + per-platform `mechtatel-natives-{linux,linux-arm64,windows,macos-arm64}` — packaged binaries of `libmttslangc` (Slang shader compiler bridge, C++ via CMake) wrapped by `MttNativeLoaderBase`.
- **`mechtatel-audio-natives`** + per-platform `mechtatel-audio-natives-{linux,linux-arm64,windows,macos-arm64}` — packaged binaries of `lib-audio-player` (Rust crate under `mechtatel-audio-natives/natives/lib-audio-player`).
- **`mechtatel-platform-pack-{linux,linux-arm64,windows,macos-arm64}`** — meta-modules that pull in the matching LWJGL `${lwjgl.natives}` classifier jars **plus** the matching `mechtatel-natives-*`. Downstream apps depend on **one** platform-pack (selected by Maven OS/arch profile via `${mechtatel.platformPack}`).
- **`mechtatel-common-utils`**, **`mechtatel-logging`** — shared helpers and an optional Logback config.

### Native loading

`MttNativeLoaderFactory2` reflectively instantiates `com.github.maeda6uiui.mechtatel.natives.{windows,linux,linuxarm64,macosarm64}.MttNativeLoader2` based on the platform string passed in. The class is provided by whichever `mechtatel-natives-*` jar is on the classpath, so adding a new platform means adding a new natives module + an entry in this factory.

### Settings & shader config

Engine entry points are constructed from `MttSettings` (typically loaded from `./Mechtatel/settings.json`). `MttShaderConfig` (default `./Mechtatel/shader_config.json`) lets apps override the engine's standard shaders with on-disk GLSL/Slang files — the shader pipeline supports both compile-time (shaderc) GLSL and runtime Slang via the `libmttslangc` native bridge.

## Native library builds (CI-driven)

The native libraries are **not** built by `mvn install`. Each platform binary is produced by a dedicated GitHub Actions workflow under `.github/workflows/build-lib-*-{linux,linux-arm64,windows,macos-arm64}.yml`, triggered by tags like `mttslangc-natives-linux-*` or `audio-player-natives-windows-*`. Workflows attach the built `.so`/`.dll`/`.dylib` to the GitHub release; the per-platform Maven natives modules then bundle those artifacts.

To build `libmttslangc` locally (Linux example): see `.github/workflows/build-lib-mttslangc-linux.yml` — it downloads a Slang release, copies `libslang.so` into `mechtatel-natives/natives/lib-mttslangc/lib/`, then runs cmake from a `build/` subdir. The Rust audio library lives at `mechtatel-audio-natives/natives/lib-audio-player` and builds with standard `cargo build --release`.

## Release & publishing

- Maven Central is the publish target; `.github/workflows/deploy-artifacts-on-push.yml` deploys on every push to `main` (excluding `**.md` and `Misc/**`) using the `central-publishing-maven-plugin`. GPG signing is required for releases.
- `Misc/release-creator/` is a separate Python (uv) tool that assembles distributable bundles for the `mechtatel-hello` sample app.
- `Misc/mechtatel-hello/` is the canonical "how to consume Mechtatel as a library" sample, including the OS/arch profile pattern that downstream users should copy.