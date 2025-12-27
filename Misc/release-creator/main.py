import argparse
import sys
import yaml
from logging import getLogger, config

from src.release_creator import ReleaseCreator


def main(args):
    project_filepath: str = args.project_filepath
    package_dirname: str = args.package_dirname
    output_archive_filename: str = args.output_archive_filename
    output_jar_filename: str = args.output_jar_filename
    remove_package_dir_on_exit: bool = args.remove_package_dir_on_exit
    openjdk_download_url: str | None = args.openjdk_download_url
    existing_openjdk_archive_filepath: str | None = (
        args.existing_openjdk_archive_filepath
    )
    filenames_to_package: list[str] | None = args.filenames_to_package

    # Set up logger
    with open("./logging_config.yaml", "r", encoding="utf-8") as r:
        logging_config = yaml.safe_load(r)

    config.dictConfig(logging_config)

    logger = getLogger(__name__)

    # Run release creator
    release_creator = ReleaseCreator(
        project_filepath,
        package_dirname,
        output_archive_filename,
        output_jar_filename,
        remove_package_dir_on_exit,
        openjdk_download_url=openjdk_download_url,
        existing_openjdk_archive_filepath=existing_openjdk_archive_filepath,
        filenames_to_package=filenames_to_package,
        logger=logger,
    )
    try:
        release_creator.run()
    except Exception as e:
        logger.error(f"Failed to run release creator: {e}")
        sys.exit(1)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-p", "--project-filepath", type=str)
    parser.add_argument("-d", "--package-dirname", type=str)
    parser.add_argument("-oa", "--output-archive-filename", type=str)
    parser.add_argument("-oj", "--output-jar-filename", type=str)
    parser.add_argument("--remove-package-dir-on-exit", action="store_true")
    parser.add_argument("-u", "--openjdk-download-url", type=str)
    parser.add_argument("-j", "--existing-openjdk-archive-filepath", type=str)
    parser.add_argument("-f", "--filenames-to-package", type=str, nargs="*")
    args = parser.parse_args()

    main(args)
