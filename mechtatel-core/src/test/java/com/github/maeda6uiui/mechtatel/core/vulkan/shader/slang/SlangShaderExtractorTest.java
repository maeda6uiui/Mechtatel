package com.github.maeda6uiui.mechtatel.core.vulkan.shader.slang;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlangShaderExtractorTest {
    private static final String VERT_SOURCE = """
            //@mtt.extractionPath "shader.vert.slang"
            //@mtt.entryPoint
            [shader("vertex")]
            float4 main() : SV_Position { return float4(0, 0, 0, 1); }
            """;

    private static final String FRAG_SOURCE = """
            //@mtt.extractionPath "shader.frag.slang"
            [shader("fragment")]
            float4 main() : SV_Target0 { return float4(1, 1, 1, 1); }
            """;

    private static URL writeSource(Path dir, String name, String content) throws IOException {
        Path file = dir.resolve(name);
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file.toUri().toURL();
    }

    @Test
    void extract_writesAllSourcesAndIdentifiesEntryPoint(@TempDir Path inputDir) throws IOException {
        URL vert = writeSource(inputDir, "in.vert.slang", VERT_SOURCE);
        URL frag = writeSource(inputDir, "in.frag.slang", FRAG_SOURCE);

        var extractor = new SlangShaderExtractor(List.of(vert, frag));
        extractor.extract();

        Path entryPoint = extractor.getEntryPointPath();
        assertNotNull(entryPoint);
        assertTrue(Files.exists(entryPoint), "Entry point file should exist on disk");
        assertEquals("shader.vert.slang", entryPoint.getFileName().toString());
        assertEquals(VERT_SOURCE, Files.readString(entryPoint, StandardCharsets.UTF_8));

        Path fragExtracted = entryPoint.getParent().resolve("shader.frag.slang");
        assertTrue(Files.exists(fragExtracted), "Non-entry-point shader should also be extracted");
        assertEquals(FRAG_SOURCE, Files.readString(fragExtracted, StandardCharsets.UTF_8));
    }

    @Test
    void getAllSourcesConcatenated_containsEverySourceVerbatim(@TempDir Path inputDir) throws IOException {
        URL vert = writeSource(inputDir, "in.vert.slang", VERT_SOURCE);
        URL frag = writeSource(inputDir, "in.frag.slang", FRAG_SOURCE);

        var extractor = new SlangShaderExtractor(List.of(vert, frag));
        extractor.extract();

        String concatenated = extractor.getAllSourcesConcatenated();
        assertEquals(VERT_SOURCE + FRAG_SOURCE, concatenated);
    }

    @Test
    void extractionPath_supportsNestedDirectories(@TempDir Path inputDir) throws IOException {
        String nestedSource = """
                //@mtt.extractionPath "sub/dir/entry.slang"
                //@mtt.entryPoint
                void main() {}
                """;
        URL nested = writeSource(inputDir, "nested.slang", nestedSource);

        var extractor = new SlangShaderExtractor(List.of(nested));
        extractor.extract();

        Path entryPoint = extractor.getEntryPointPath();
        assertTrue(Files.exists(entryPoint));
        assertEquals("entry.slang", entryPoint.getFileName().toString());
        assertEquals("dir", entryPoint.getParent().getFileName().toString());
        assertEquals("sub", entryPoint.getParent().getParent().getFileName().toString());
    }

    @Test
    void getters_returnNullBeforeExtract() {
        var extractor = new SlangShaderExtractor(List.of());
        assertNull(extractor.getAllSourcesConcatenated());
        assertNull(extractor.getEntryPointPath());
    }

    @Test
    void extract_throwsWhenSourceLacksExtractionPathAnnotation(@TempDir Path inputDir) throws IOException {
        String noPath = """
                //@mtt.entryPoint
                [shader("vertex")] float4 main() : SV_Position { return 0; }
                """;
        URL bad = writeSource(inputDir, "bad.slang", noPath);

        var extractor = new SlangShaderExtractor(List.of(bad));
        RuntimeException ex = assertThrows(RuntimeException.class, extractor::extract);
        assertTrue(ex.getMessage().contains("No extraction path annotation"),
                "Message should mention missing extraction path: " + ex.getMessage());
    }

    @Test
    void extract_throwsWhenNoSourceHasEntryPoint(@TempDir Path inputDir) throws IOException {
        String noEntry = """
                //@mtt.extractionPath "a.slang"
                void helper() {}
                """;
        URL bad = writeSource(inputDir, "noentry.slang", noEntry);

        var extractor = new SlangShaderExtractor(List.of(bad));
        RuntimeException ex = assertThrows(RuntimeException.class, extractor::extract);
        assertTrue(ex.getMessage().contains("entrypoint"),
                "Message should mention missing entrypoint: " + ex.getMessage());
    }

    @Test
    void extract_throwsWhenExtractionPathTraversesRoot(@TempDir Path inputDir) throws IOException {
        String traversal = """
                //@mtt.extractionPath "../escape.slang"
                //@mtt.entryPoint
                void main() {}
                """;
        URL bad = writeSource(inputDir, "traversal.slang", traversal);

        var extractor = new SlangShaderExtractor(List.of(bad));
        RuntimeException ex = assertThrows(RuntimeException.class, extractor::extract);
        assertTrue(ex.getMessage().contains("traverse"),
                "Message should mention path traversal: " + ex.getMessage());
    }
}