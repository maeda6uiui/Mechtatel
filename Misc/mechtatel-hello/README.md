<!-- @formatter:off -->

# mechtatel-hello

Release tag: ${releaseTag}

## Overview

mechtatel-hello is a test kit to check if the Mechtatel engine works on your system.
It basically does off-screen rendering and outputs the resulting image to a file.
It doesn't guarantee that the Mechtatel engine is fully functional on your system even if mechtatel-hello works properly, but I assume it will in most cases.

## How to run it

### Use release package

There are release packages available in releases of this repo.

The easiest way is to download a release package for your platform (`.tar.gz` for Linux and macOS, `.zip` for Windows), extract it, and then execute `start.sh` (Linux and macOS) or `start.bat` (Windows).
It includes OpenJDK, so you don't have to install Java on your system.

If you already have Java 21 or higher installed, you can use the JAR file for your platform and execute it with `java -jar` command.
In this case, you also have to download `data.zip` from the release and extract it to the same directory as the JAR file.

In either case, rendering result is written to `./Data/rendering.png` by default.

### Build it yourself

You need to have Java 21 or higher installed, along with maven.
Run the following command to package mechtatel-hello:

```
mvn clean package
```

That should produce an uber JAR under the `target` directory.
Copy the uber JAR to the root directory of the mechtatel-hello project (one level higher from `target`).
Then you can run it with the following command:

```
java -jar {filename of the JAR file}
```

Rendering result is written to `./Data/rendering.png` by default.
Run the JAR file with `-h` option to display help for the command.
