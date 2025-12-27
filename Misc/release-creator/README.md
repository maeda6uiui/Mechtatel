# release-creator

This is a utility script to create a release artifact for projects that use the Mechtatel engine.
It packages an uber JAR of your project along with OpenJDK.

For instance, the following command generates `mechtatel-hello_linux_x64.tar.gz` that contains an uber JAR of mechtatel-hello along with OpenJDK for Linux x64, into the `WorkingDir` directory.

```
uv run main.py \
    -p ../mechtatel-hello \
    -d mechtatel-hello \
    -oa mechtatel-hello_linux_x64.tar.gz \
    -oj mechtatel-hello_linux_x64.jar \
    --remove-package-dir-on-exit \
    -u https://download.java.net/java/GA/jdk21/fd2272bbf8e04c3dbaee13770090416c/35/GPL/openjdk-21_linux-x64_bin.tar.gz \
    -f Data README.md 
```

The above command doesn't package the JAR of the target project.
It only fetches the JAR that already exists in the target project.
