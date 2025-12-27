from pathlib import Path

from src.release_creator import ReleaseCreator

def test_release_creator():
    release_creator=ReleaseCreator(
        "https://download.java.net/java/GA/jdk21/fd2272bbf8e04c3dbaee13770090416c/35/GPL/openjdk-21_linux-x64_bin.tar.gz",
        "../mechtatel-hello",
        "mechtatel-hello",
        "mechtatel-hello_linux-x64.tar.gz",
        "mechtatel-hello-linux-x64.jar",
        True,
        filenames_to_package=[
            "Data",
            "README.md"
        ]
    )
    release_creator.run()

    #Check the number of files in WorkingDir
    working_dir=Path("./WorkingDir")
    files=list(working_dir.glob("*"))
    assert len(files)==3

    #Check if release files exist
    release_package_file=working_dir.joinpath("mechtatel-hello_linux-x64.tar.gz")
    assert release_package_file.exists()

    release_jar_file=working_dir.joinpath("mechtatel-hello-linux-x64.jar")
    assert release_jar_file.exists()

    #Check the content of Cache directory
    cache_dir=working_dir.joinpath("Cache")
    assert cache_dir.exists() and cache_dir.is_dir()

    openjdk_archive_file=cache_dir.joinpath("openjdk-21_linux-x64_bin.tar.gz")
    assert openjdk_archive_file.exists()
