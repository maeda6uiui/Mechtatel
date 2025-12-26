# release-creator

This is a utility script to create a release artifact for projects that use the Mechtatel engine.
It packages an uber JAR of your project along with OpenJDK.

For instance, the following command generates `release_linux_x64.tar.gz` that contains an uber JAR of mechtatel-hello along with OpenJDK for Linux x64, into the `WorkingDir` directory.

```
uv run main.py \
    -u https://download.java.net/java/GA/jdk21/fd2272bbf8e04c3dbaee13770090416c/35/GPL/openjdk-21_linux-x64_bin.tar.gz \
    -p ../mechtatel-hello \
    -f Data README.md \
    -d mechtatel-hello \
    -o release_linux_x64.tar.gz \
    --remove-package-dir-on-exit
```
