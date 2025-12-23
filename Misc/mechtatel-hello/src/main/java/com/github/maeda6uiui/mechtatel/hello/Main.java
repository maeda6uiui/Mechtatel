package com.github.maeda6uiui.mechtatel.hello;

import com.github.maeda6uiui.mechtatel.core.MttSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * Entrypoint for mechtatel-hello
 *
 * @author maeda6uiui
 */
@CommandLine.Command.Command(name = "mechtatel-hello", mixinStandardHelpOptions = true)
public class Main implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @CommandLine.Option(
            names = {"-t", "--rendering-type"},
            defaultValue = "primitives",
            description = "Rendering type [primitives, imgui, monochrome-image]"
    )
    private String renderingType;
    @CommandLine.Option(
            names = {"-o", "--image-output-filepath"},
            defaultValue = "./Data/rendering.png",
            description = "Rendering result is written to the file specified."
    )
    private String imageOutputFilepath;
    @CommandLine.Option(
            names = {"-f", "--setting-filepath"},
            defaultValue = "./Data/settings.json",
            description = "Specify the filepath of the setting file."
    )
    private String settingFilepath;

    @Override
    public void run() {
        var options = new MttHelloMain.MttHelloOptions(renderingType, imageOutputFilepath);
        MttHelloMain.setOptions(options);

        MttSettings
                .load(settingFilepath)
                .ifPresentOrElse(
                        MttHelloMain::new,
                        () -> {
                            logger.warn(
                                    "Unable to load settings file ({}), fall back to default settings",
                                    settingFilepath
                            );
                            new MttHelloMain(new MttSettings());
                        }
                );
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
