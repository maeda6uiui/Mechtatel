<!-- @formatter:off -->

# mechtatel-hello

## Overview

mechtatel-hello is a test kit to check if the Mechtatel engine works on your system.
It basically does off-screen rendering and outputs the resulting image to a file.
It doesn't guarantee that the Mechtatel engine is fully functional on your system even if mechtatel-hello works properly, but I assume it will in most cases.

## How to run it

> [!NOTE]
> I have a plan to package OpenJDK along with the JAR file of mechtatel-hello so that it can be executed if you don't have Java installed yet.
> In the meantime, you have to install Java 21 or higher and then package mechtatel-hello by yourself.

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
