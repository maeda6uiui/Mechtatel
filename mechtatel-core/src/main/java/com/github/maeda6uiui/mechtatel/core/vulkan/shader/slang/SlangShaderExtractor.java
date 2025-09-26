package com.github.maeda6uiui.mechtatel.core.vulkan.shader.slang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts Slang shaders according to the annotation in the shader file.
 *
 * @author maeda6uiui
 */
public class SlangShaderExtractor implements ISlangShaderExtractorGetters {
    private static final Logger logger = LoggerFactory.getLogger(SlangShaderExtractor.class);

    private List<URL> shaderResources;
    private String allSourcesConcatenated;
    private Path entryPointPath;

    public SlangShaderExtractor(List<URL> shaderResources) {
        this.shaderResources = shaderResources;
    }

    private boolean isFileInsideDirectory(Path file, Path directory) {
        Path absoluteFilePath = file.toAbsolutePath().normalize();
        Path absoluteDirectoryPath = directory.toAbsolutePath().normalize();

        return absoluteFilePath.startsWith(absoluteDirectoryPath);
    }

    /**
     * Extracts Slang shaders into a temporary directory
     * according to the extraction path annotation in shader files.
     *
     * @throws IOException If it fails to load sources, write to a file, etc.
     */
    public void extract() throws IOException {
        //Get all shader sources
        var sources = new ArrayList<String>();
        for (var shaderResource : shaderResources) {
            try (var bis = new BufferedInputStream(shaderResource.openStream())) {
                byte[] b = bis.readAllBytes();
                String source = new String(b, StandardCharsets.UTF_8);
                sources.add(source);
            }
        }

        var sb = new StringBuilder();
        sources.forEach(sb::append);
        allSourcesConcatenated = sb.toString();

        //Associate shader source to its extraction path
        //Also, find the extraction path that has the entrypoint
        Pattern ptnExtractionPath = Pattern.compile("@mtt\\.extractionPath\\s+\"([^\"]+)\"");

        String entryPointExtractionPath = "";
        var mSources = new HashMap<String, String>();  //(extraction path, source)
        for (int i = 0; i < sources.size(); i++) {
            String source = sources.get(i);
            Matcher matcher = ptnExtractionPath.matcher(source);
            if (!matcher.find()) {
                throw new RuntimeException(
                        String.format("No extraction path annotation found in %s", shaderResources.get(i).getPath()));
            }

            String extractionPath = matcher.group(1);
            mSources.put(extractionPath, source);

            if (source.contains("@mtt.entryPoint")) {
                entryPointExtractionPath = extractionPath;
            }
        }
        if (entryPointExtractionPath.isEmpty()) {
            throw new RuntimeException("None of the sources has an entrypoint annotation");
        }

        //Create a temporary file for each shader source and write shader source to it
        Path tempDir = Files.createTempDirectory(null);
        tempDir.toFile().deleteOnExit();
        logger.debug("Slang shaders will be extracted to {}", tempDir);

        for (var entry : mSources.entrySet()) {
            String extractionPath = entry.getKey();
            String source = entry.getValue();

            Path sourceFile = tempDir.resolve(extractionPath);
            logger.debug("Extracting {}", sourceFile);
            if (!this.isFileInsideDirectory(sourceFile, tempDir)) {
                throw new RuntimeException("Extraction path cannot traverse its root directory: " + extractionPath);
            }
            if (Files.exists(sourceFile)) {
                throw new RuntimeException("File already exists: " + sourceFile);
            }

            Path sourceParentDir = sourceFile.getParent();
            if (!Files.exists(sourceParentDir)) {
                Files.createDirectories(sourceParentDir);
                sourceParentDir.toFile().deleteOnExit();
            }

            Files.writeString(sourceFile, source, StandardCharsets.UTF_8);
            sourceFile.toFile().deleteOnExit();
        }

        entryPointPath = tempDir.resolve(entryPointExtractionPath).toAbsolutePath().normalize();
    }

    @Override
    public String getAllSourcesConcatenated() {
        return allSourcesConcatenated;
    }

    @Override
    public Path getEntryPointPath() {
        return entryPointPath;
    }
}
