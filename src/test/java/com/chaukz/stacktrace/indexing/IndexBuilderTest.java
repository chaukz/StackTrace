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
}
