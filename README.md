# Mechtatel

Mechtatel (ru: Мечтатель en: Dreamer)

> Мечтай до тех пор, пока не узнаешь, что давно пора повзрослеть и бросить эту фигню.

## Overview

This project aims to create a Vulkan-based game engine in Java with help of [LWJGL](https://www.lwjgl.org/).
It's still far from what you call a game engine, but I'll keep on developing little by little in my free time.
Leave a star in this repo if you like it!

Unfortunately, there are no elaborate documents for this project so far.
However, you could check out the [test code](./mechtatel-core/src/test/java/com/github/maeda6uiui/mechtatel/) and hopefully get to learn what Mechtatel has to offer!

Note that this project is currently under development and is subject to drastic change.

## Message from developer

It's been years since I came up with an idea like "Isn't it great if I could create a game engine and build my own game upon it?"
If you just want to create a game, then you should take advantage of the great game engines such as Unity and Unreal Engine.
As for me, I simply love to write code, and want to develop a game starting from the lowest level possible.

*Mechtatel* is a word that means "Dreamer" in Russian.
It's by far the best word to describe me, only dreaming and being far from any achivements.
God knows if I could complete this work by the time you'd think I'm no more...

## Try it out

`groupId` is going to be changed to `io.github.maeda6uiui` in the next GA release.

```xml
<dependencies>
    <dependency>
        <groupId>com.github.dabasan</groupId>
        <artifactId>mechtatel-core</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.dabasan</groupId>
        <artifactId>mechtatel-logging</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

Snapshot builds are available in the Maven Snapshot Repository.

```xml
<dependencies>
    <dependency>
        <groupId>io.github.maeda6uiui</groupId>
        <artifactId>mechtatel-core</artifactId>
        <version>0.1.1-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>io.github.maeda6uiui</groupId>
        <artifactId>mechtatel-logging</artifactId>
        <version>0.1.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

Use of `mechtatel-logging` module is optional.
It provides minimal logging functionality with Logback, which prints out logs above the INFO level to stdout.
You can overwrite it or use another logging implementation at your discretion.

## Currently working on

## Todo

### Will certainly do

### Probably...

- Investigate the methods to reproduce water surface
- Implement functionality to simulate sea waves
- Design and implement API that can be invoked from external applications
  - Establish connection between Mechtatel (server) and client app
  - Build client app in any language (Go, Rust, etc.)

## Special thanks to

- [Vulkan Tutorial](https://vulkan-tutorial.com/)
- [Vulkan-Tutorial-Java](https://github.com/Naitsirc98/Vulkan-Tutorial-Java)
- [3D Game Development with LWJGL 3](https://ahbejarano.gitbook.io/lwjglgamedev/)

## Screenshots

Some basic primitives

![Primitives](./Image/primitives.png)

Physics objects

![Physics objects](./Image/physics_objects.png)

Spotlights

![Spotlights](./Image/spotlights.png)

Shadow mapping

![Shadow mapping](./Image/shadow_mapping.png)

Skeletal animation

![Skeletal animation](./Image/skeletal_animation.png)

ImGui

![ImGui](./Image/imgui.png)

Rendering to multiple windows

![Rendering to multiple windows](./Image/rendering_to_multiple_windows.png)

## Progress report

### 2025-04-23

Releases `v0.1.0`.
Check the release note for the changes that have been made since the previous release!

### 2025-04-22

Added an option to limit the number of shadow maps to be created.
The previous implementation created 16 shadow maps regardless of whether they are used, which may cause wasteful allocation of VRAM.
New implementation allows to specify the number of shadow maps between 1 and 16, and the default is 1.
This value can be changed via settings.json (`vulkan.numShadowMaps`).

### 2025-04-20

Added a test code for audio playback using [LibSoundPlayer](https://github.com/maeda6uiui/LibSoundPlayer).
Check the [test code](./mechtatel-core/src/test/java/com/github/maeda6uiui/mechtatel/SoundPlayerTest.java) to find out the usage!

### 2025-03-09

Releases `v0.0.1` with the same functionalities as the last alpha release.
This is the first GA version of the Mechtatel engine.

### 2025-01-13

Releases `v0.0.1-alpha8`.
Check the release note for the changes that have been made since the previous release!

### 2024-12-27

Releases `v0.0.1-alpha6`.
Check the release note for the changes that have been made since the previous release!

### 2024-12-01

Releases `v0.0.1-alpha5` with some refactoring and changes to methods.

### 2024-10-06

Released `v0.0.1-alpha4`.

### 2024-09-29

Released `v0.0.1-alpha3`.

### 2024-09-23

Implemented tracking camera to provide third-person view for the target character.

### 2024-08-26

`v0.0.1-alpha0` fails to start because it fails to load native libraries and shaders from inside a JAR.
Currently working on a fix and refactoring of that issue.

Released `v0.0.1-alpha1`.

### 2024-08-25

Removed app info from settings and changed it to constant values.

### 2024-08-24

Updated the versions of the dependencies.

Released the first non-snapshot version `0.0.1-alpha0` of the Mechtatel engine.

### 2024-08-18

Fixed a bug that the control of frames per second doesn't work properly.
Previous implementation sees a lower frames per second and laggy rendering because of improper update of the `lastTime` variable.

Fixed a bug that the shader for texture operations ignored factors given as a parameter.

Added test code to play sound files.

### 2024-08-17

Texture operations can now take up to 8 textures.
Merge-by-depth operation has been removed.

### 2024-08-16

Implemented headless mode, that is, rendering without a window.
This feature will enable you to run a GPU instance on a cloud service and obtain rendering results from the Mechtatel engine, although it is yet to be tested on a cloud service. 

### 2024-07-21

Implemented Gaussian blur as a full-screen effect.
Full-screen effects are intended to be applied after post-processing stages.
Post-processing shaders consume images other than color (such as depth and position), whereas full-screen effect shaders consume only a color image.
