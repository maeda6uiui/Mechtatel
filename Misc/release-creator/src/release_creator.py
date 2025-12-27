import os
import requests
import shutil
import uuid
import xml.etree.ElementTree as ET
from logging import getLogger,Logger
from pathlib import Path


class ReleaseCreator:
    def __init__(
        self,
        openjdk_download_url: str,
        project_filepath: str,
        package_dirname: str,
        output_archive_filename: str,
        output_jar_filename:str,
        remove_package_dir_on_exit: bool,
        existing_openjdk_dirname: str | None = None,
        filenames_to_package: list[str] | None = None,
        logger: Logger | None = None,
    ):
        self.__openjdk_download_url = openjdk_download_url
        self.__output_archive_filename = output_archive_filename
        self.__output_jar_filename=output_jar_filename
        self.__remove_package_dir_on_exit = remove_package_dir_on_exit
        self.__existing_openjdk_dirname = existing_openjdk_dirname
        self.__filenames_to_package = filenames_to_package

        if logger is not None:
            self.__logger = logger
        else:
            self.__logger = getLogger(__name__)

        self.__project_dir = Path(project_filepath)

        # Create working directory
        self.__working_dir = Path("./WorkingDir")
        self.__working_dir.mkdir(exist_ok=True)

        #Create cache directory
        self.__cache_dir=self.__working_dir.joinpath("Cache")
        self.__cache_dir.mkdir(exist_ok=True)

        # Create package directory
        package_root_dirname = str(uuid.uuid4())
        self.__package_root_dir=self.__working_dir.joinpath(package_root_dirname)
        self.__package_root_dir.mkdir()

        self.__package_dir = self.__package_root_dir.joinpath(
            package_dirname
        )
        self.__package_dir.mkdir()
        self.__logger.info(f"Created a package directory: {self.__package_dir}")

    def __download_openjdk(self) -> Path:
        self.__logger.info(f"Start downloading JDK from {self.__openjdk_download_url}")

        openjdk_filename = self.__openjdk_download_url.split("/")[-1]
        openjdk_archive_file = self.__package_dir.joinpath(openjdk_filename)

        data = requests.get(self.__openjdk_download_url).content
        with openjdk_archive_file.open("wb") as w:
            w.write(data)

        self.__logger.info(f"OpenJDK downloaded successfully: {openjdk_archive_file}")
        return openjdk_archive_file

    def __extract_openjdk(self, openjdk_archive_file: Path) -> Path:
        self.__logger.info("Start extracting OpenJDK archive")

        shutil.unpack_archive(openjdk_archive_file, self.__package_dir)

        # Get extracted dirnane
        openjdk_dir: Path | None = None
        files = self.__package_dir.glob("jdk-*")
        for file in files:
            if file.is_dir():
                openjdk_dir = file
                break

        if openjdk_dir is None:
            raise RuntimeError("Failed to find OpenJDK directory")

        # Rename the directory to "OpenJDK"
        openjdk_dir = openjdk_dir.rename(self.__package_dir.joinpath("OpenJDK"))
        return openjdk_dir
    
    def __move_openjdk_archive_to_cache(self,openjdk_archive_file:Path)->Path:
        move_to=self.__cache_dir.joinpath(openjdk_archive_file.name)
        openjdk_archive_file.rename(move_to)
        return move_to

    def __get_project_info(self) -> dict[str, str]:
        pom_file = self.__project_dir.joinpath("pom.xml")
        self.__logger.info(f"Loading pom.xml: {pom_file}")

        tree = ET.parse(pom_file)
        root = tree.getroot()

        artifact_id = root.find("{http://maven.apache.org/POM/4.0.0}artifactId")
        if artifact_id is None:
            raise RuntimeError("'artifactId' element not found in pom.xml")
        if artifact_id.text is None:
            raise RuntimeError("Failed to get artifactId from pom.xml")

        version = root.find("{http://maven.apache.org/POM/4.0.0}version")
        if version is None:
            raise RuntimeError("'version' element not found in pom.xml")
        if version.text is None:
            raise RuntimeError("Failed to get version from pom.xml")

        self.__logger.info(f"ArtifactId of target project: {artifact_id.text}")
        self.__logger.info(f"Version of target project: {version.text}")

        return {"artifactId": artifact_id.text, "version": version.text}

    def __copy_uber_jar_to_package_dir(self, artifact_id: str, version: str) -> Path:
        self.__logger.info("Copying JAR file")

        src_jar_file = self.__project_dir.joinpath(f"{artifact_id}-{version}.jar")
        if not src_jar_file.exists():
            raise RuntimeError(f"JAR file not found: {src_jar_file}")

        dest_jar_file = self.__package_dir.joinpath(self.__output_jar_filename)
        shutil.copy(src_jar_file, dest_jar_file)
        self.__logger.info(f"JAR file was copied: {src_jar_file} -> {dest_jar_file}")

        return dest_jar_file

    def __generate_start_scripts(self, jar_file: Path):
        self.__logger.info("Generating start scripts")

        with open("start.sh.template", "r", encoding="utf-8") as r:
            sh_template = r.read()

        with open("start.bat.template", "r", encoding="utf-8") as r:
            bat_template = r.read()

        sh_replaced = sh_template.replace("${jarFilename}", jar_file.name)
        sh_file = self.__package_dir.joinpath("start.sh")
        with sh_file.open("w", encoding="utf-8") as w:
            w.write(sh_replaced)

        bat_replaced = bat_template.replace("${jarFilename}", jar_file.name)
        bat_file = self.__package_dir.joinpath("start.bat")
        with bat_file.open("w", encoding="utf-8") as w:
            w.write(bat_replaced)

        os.chmod(sh_file, 0o755)
        os.chmod(bat_file, 0o755)

    def __is_file_inside_dir(self,file:Path,dir:Path)->bool:
        abs_file=file.resolve()
        abs_dir=dir.resolve()

        filepath=str(abs_file)
        dirpath=str(abs_dir)

        return filepath.startswith(dirpath)

    def __copy_files_to_package(self):
        self.__logger.info("Copying files to the package")
        if self.__filenames_to_package is None or len(self.__filenames_to_package) == 0:
            self.__logger.info("No additional files to add")
            return

        for filename in self.__filenames_to_package:
            src_file = self.__project_dir.joinpath(filename)
            if not self.__is_file_inside_dir(src_file,self.__project_dir):
                self.__logger.warning(f"Skip copying because it cannot traverse project directory: {src_file}")
                continue

            dest_file = self.__package_dir.joinpath(filename)
            self.__logger.info(f"Copying file: {src_file} -> {dest_file}")

            if src_file.is_dir():
                shutil.copytree(src_file, dest_file)
            else:
                shutil.copy(src_file, dest_file)

    def __create_release_archive(self) -> Path:
        self.__logger.info("Creating release archive")

        archive_format: str = ""
        base_name: str = ""
        if ".tar.gz" in self.__output_archive_filename:
            archive_format = "gztar"
            base_name = self.__output_archive_filename.replace(".tar.gz", "")
        elif ".zip" in self.__output_archive_filename:
            archive_format = "zip"
            base_name = self.__output_archive_filename.replace(".zip", "")
        else:
            self.__logger.warning(f"Cannot infer archive format, defaults to 'gztar'")
            archive_format = "gztar"
            base_name = self.__output_archive_filename

        cwd = os.getcwd()
        os.chdir(self.__package_dir.parent)
        archive_filename = shutil.make_archive(base_name, archive_format)
        os.chdir(cwd)

        archive_file = self.__package_dir.parent.joinpath(archive_filename)
        archive_file = archive_file.rename(
            self.__working_dir.joinpath(archive_file.name)
        )
        self.__logger.info(f"Created release archive: {archive_file}")

        return archive_file
    
    def __copy_uber_jar_to_working_dir(self,jar_file:Path)->Path:
        copy_to=self.__working_dir.joinpath(self.__output_jar_filename)
        shutil.copy(jar_file,copy_to)
        self.__logger.info(f"JAR file was copied: {jar_file} -> {copy_to}")

        return copy_to
    
    def __list_files_in_package_dir(self)->set[str]:
        filenames=set()
        files=self.__package_dir.glob("*")
        for file in files:
            filenames.add(file.name)
        
        return filenames


    def run(self)->dict[str,object]:
        openjdk_dir: Path | None = None
        if self.__existing_openjdk_dirname:
            existing_openjdk_dir = Path(self.__existing_openjdk_dirname)
            openjdk_dir = self.__package_dir.joinpath("OpenJDK")
            shutil.copytree(existing_openjdk_dir, openjdk_dir)
        else:
            openjdk_archive_file = self.__download_openjdk()
            openjdk_dir = self.__extract_openjdk(openjdk_archive_file)
            self.__move_openjdk_archive_to_cache(openjdk_archive_file)

        project_info = self.__get_project_info()
        jar_file = self.__copy_uber_jar_to_package_dir(
            project_info["artifactId"], project_info["version"]
        )
        self.__generate_start_scripts(jar_file)
        self.__copy_files_to_package()
        self.__create_release_archive()
        self.__copy_uber_jar_to_working_dir(jar_file)

        packaged_filenames=self.__list_files_in_package_dir()

        if self.__remove_package_dir_on_exit:
            self.__logger.info(f"Removing directory: {self.__package_root_dir}")
            shutil.rmtree(self.__package_root_dir)

        return {
            "packageDir": str(self.__package_dir),
            "packagedFiles": packaged_filenames
        }
