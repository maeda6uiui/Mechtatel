import pytest
import shutil
from pathlib import Path

from src.release_creator import ReleaseCreator


def __common_check(release_creator_ret: dict[str, object]):
    working_dir = Path("./WorkingDir")

    # Check if package directory does NOT exist
    package_dirname: str = release_creator_ret["packageDir"]  # type: ignore
    package_dir = Path(package_dirname)
    assert not package_dir.exists()

    # Check if release files exist
    release_package_file = working_dir.joinpath("mechtatel-hello_linux-x64.tar.gz")
    assert release_package_file.exists()

    release_jar_file = working_dir.joinpath("mechtatel-hello_linux-x64.jar")
    assert release_jar_file.exists()

    # Check the content of Cache directory
    cache_dir = working_dir.joinpath("Cache")
    assert cache_dir.exists() and cache_dir.is_dir()

    openjdk_archive_file = cache_dir.joinpath("openjdk-21_linux-x64_bin.tar.gz")
    assert openjdk_archive_file.exists()

    # Check the content of the package
    packaged_filenames: set[str] = release_creator_ret["packagedFiles"]  # type: ignore
    expected_filenames = [
        "Data",
        "OpenJDK",
        "README.md",
        "mechtatel-hello_linux-x64.jar",
        "start.bat",
        "start.sh",
    ]
    for filename in expected_filenames:
        assert filename in packaged_filenames


@pytest.mark.order(0)
def test_release_creator():
    release_creator = ReleaseCreator(
        "../mechtatel-hello",
        "mechtatel-hello",
        "mechtatel-hello_linux-x64.tar.gz",
        "mechtatel-hello_linux-x64.jar",
        True,
        openjdk_download_url="https://download.java.net/java/GA/jdk21/fd2272bbf8e04c3dbaee13770090416c/35/GPL/openjdk-21_linux-x64_bin.tar.gz",
        filenames_to_package=[
            "Data",
            "README.md",
            "../../../testfile.txt",  # This should not be added to package
        ],
    )
    ret = release_creator.run()
    __common_check(ret)


@pytest.mark.order(1)
def test_release_creator_with_openjdk_cache():
    release_creator = ReleaseCreator(
        "../mechtatel-hello",
        "mechtatel-hello",
        "mechtatel-hello_linux-x64.tar.gz",
        "mechtatel-hello_linux-x64.jar",
        True,
        existing_openjdk_archive_filepath="./WorkingDir/Cache/openjdk-21_linux-x64_bin.tar.gz",
        filenames_to_package=[
            "Data",
            "README.md",
            "../../../testfile.txt",  # This should not be added to package
        ],
    )
    ret = release_creator.run()
    __common_check(ret)


@pytest.mark.order(2)
def test_release_package_contents():
    working_dir = Path("./WorkingDir")
    release_package_file = working_dir.joinpath("mechtatel-hello_linux-x64.tar.gz")

    # Extract release package into Test directory
    test_dir = working_dir.joinpath("Test")
    test_dir.mkdir(exist_ok=True)
    shutil.unpack_archive(release_package_file, test_dir)

    # Check if files exist
    mechtatel_hello_dir = test_dir.joinpath("mechtatel-hello")
    assert mechtatel_hello_dir.exists()

    files = mechtatel_hello_dir.glob("*")
    actual_filenames = [file.name for file in files]
    actual_filenames = set(actual_filenames)
    expected_filenames = [
        "Data",
        "OpenJDK",
        "mechtatel-hello_linux-x64.jar",
        "README.md",
        "start.bat",
        "start.sh",
    ]
    for filename in expected_filenames:
        assert filename in actual_filenames

    # Check the content of start scripts
    bat_command = ".\\OpenJDK\\bin\\java -jar mechtatel-hello_linux-x64.jar %*"
    bat_file = mechtatel_hello_dir.joinpath("start.bat")
    with bat_file.open("r", encoding="utf-8") as r:
        bat_content = r.read()

    assert bat_command in bat_content

    sh_command = "./OpenJDK/bin/java -jar mechtatel-hello_linux-x64.jar $@"
    sh_file = mechtatel_hello_dir.joinpath("start.sh")
    with sh_file.open("r", encoding="utf-8") as r:
        sh_content = r.read()

    assert sh_command in sh_content

    # Remove Test directory
    shutil.rmtree(test_dir)
