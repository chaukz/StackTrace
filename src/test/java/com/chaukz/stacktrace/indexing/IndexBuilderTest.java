package com.chaukz.stacktrace.indexing;

import com.chaukz.stacktrace.model.Document;
import com.chaukz.stacktrace.model.Posting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexBuilderTest {
    @TempDir
    Path directory;

    @Test
    void buildsPostingsFromDocumentText() throws IOException {
        Path path = directory.resolve("notes.txt");
        Files.writeString(path, "Java search java", StandardCharsets.UTF_8);
        Document document = new Document(7, "notes.txt", path);
        IndexBuilder builder = new IndexBuilder();

        builder.indexDocument(document);

        assertEquals(Map.of(7, new Posting(7, List.of(0, 2))),
                builder.index.getPostings("java"));
    }

    @Test
    void indexesNestedSupportedFilesWithStableIdsAndSkipsUnsupportedFiles() throws IOException {
        Path nested = Files.createDirectories(directory.resolve("nested"));
        Files.writeString(directory.resolve("z.txt"), "last", StandardCharsets.UTF_8);
        Files.writeString(nested.resolve("a.md"), "first", StandardCharsets.UTF_8);
        Files.writeString(directory.resolve("ignored.pdf"), "ignored", StandardCharsets.UTF_8);
        IndexBuilder builder = new IndexBuilder();

        builder.indexDirectory(directory);

        assertEquals(nested.resolve("a.md").toAbsolutePath(), builder.registry.get(1).path());
        assertEquals("z.txt", builder.registry.get(2).title());
        assertEquals(Map.of(1, new Posting(1, List.of(0))), builder.index.getPostings("first"));
        assertEquals(Map.of(), builder.index.getPostings("ignored"));
    }

    @Test
    void indexesEmptyFilesAsRegisteredDocumentsWithoutPostings() throws IOException {
        Path path = directory.resolve("empty.txt");
        Files.createFile(path);
        IndexBuilder builder = new IndexBuilder();

        builder.indexDocument(new Document(1, "empty.txt", path));

        assertEquals("empty.txt", builder.registry.get(1).title());
        assertTrue(builder.index.getPostings("anything").isEmpty());
    }

    @Test
    void readsUtf8DocumentText() throws IOException {
        Path path = directory.resolve("unicode.txt");
        Files.writeString(path, "München 東京", StandardCharsets.UTF_8);
        IndexBuilder builder = new IndexBuilder();

        builder.indexDocument(new Document(1, "unicode.txt", path));

        assertEquals(Map.of(1, new Posting(1, List.of(0))), builder.index.getPostings("münchen"));
        assertEquals(Map.of(1, new Posting(1, List.of(1))), builder.index.getPostings("東京"));
    }

    @Test
    void propagatesReadFailures() {
        IndexBuilder builder = new IndexBuilder();
        Document missing = new Document(1, "missing.txt", directory.resolve("missing.txt"));

        assertThrows(IOException.class, () -> builder.indexDocument(missing));
    }

    @Test
    void skipsSymbolicLinksDuringDirectoryDiscovery() throws IOException {
        Path target = directory.resolve("target.txt");
        Files.writeString(target, "target", StandardCharsets.UTF_8);
        Path link = directory.resolve("link.txt");
        Files.createSymbolicLink(link, target.getFileName());
        IndexBuilder builder = new IndexBuilder();

        builder.indexDirectory(directory);

        assertEquals(target.toAbsolutePath(), builder.registry.get(1).path());
        assertEquals(Map.of(1, new Posting(1, List.of(0))), builder.index.getPostings("target"));
    }
}
