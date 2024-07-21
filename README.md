# Mechtatel

Mechtatel (ru: Мечтатель en: Dreamer)

> Мечтай до тех пор, пока не узнаешь, что давно пора повзрослеть и бросить эту фигню.

## Overview

This project aims to create a Vulkan-based game engine in Java with help of [LWJGL](https://www.lwjgl.org/).
It's still far from what you call a game engine, but I'll keep on developing little by little in my free time.
Leave a star in this repo if you like it!

Unfortunately, there are no elaborate documents for this project so far.
However, you could check out the [test code](./mechtatel-core/src/test/java/com/github/maeda6uiui/mechtatel/) and hopefully get to learn what Mechtatel has to offer!

Note that this project is currently under development and is subject to drastic changes.

## Message from developer

It's been years since I came up with an idea like "Isn't it great if I could create a game engine and build my own game upon it?"
If you just want to create a game, then you should take advantage of the great game engines such as Unity and Unreal Engine.
As for me, I simply love to write code, and want to develop a game starting from the lowest level possible.

*Mechtatel* is a word that means "Dreamer" in Russian.
It's a word that best describes me, only dreaming and being far from achivements.
Maybe I wouldn't achieve anything until I die, maybe I would make some aesthetic garbage, I don't know...
But if you are a nerd or something and like my work, then leave a star in this repo.
It'll be my great mental support.

Thank you!

## Try it out

Snapshot builds are available in the Maven Snapshot Repository.

```xml
<dependencies>
    <dependency>
        <groupId>com.github.dabasan</groupId>
        <artifactId>mechtatel-core</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.github.dabasan</groupId>
        <artifactId>mechtatel-logging</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

Use of `mechtatel-logging` module is optional.
It provides minimal logging functionality with Logback, which prints out logs above the INFO level to stdout.
You can overwrite it or use another logging implementation at your discretion.

## Currently working on

- Implement headless mode (rendering without a window)

## Todo

### Will certainly do

### Probably...

- Investigate the methods to reproduce water surface
- Implement functionality to simulate sea waves
  - Implement it in another module or as an add-on
- Design and implement API that can be invoked from external applications
  - Establish connection between Mechtatel (server) and client app
  - Build client app in any language (Go, Rust, etc.)

## Special thanks to

- [Vulkan Tutorial](https://vulkan-tutorial.com/)
- [Vulkan-Tutorial-Java](https://github.com/Naitsirc98/Vulkan-Tutorial-Java)
- [3D Game Development with LWJGL 3](https://ahbejarano.gitbook.io/lwjglgamedev/)

## Progress report

