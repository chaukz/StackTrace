package com.chaukz.stacktrace.indexing;

import com.chaukz.stacktrace.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IndexBuilderLengthTest {

    @TempDir
    Path tempDir;

    private InvertedIndex index;
    private IndexBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new IndexBuilder();
        index = builder.index;
    }

    @Test
    void recordsDocumentLengthDuringIndexing() throws IOException {
        Path file = tempDir.resolve("java.txt");
        Files.writeString(file, "Java is a programming language",
                StandardCharsets.UTF_8);

        builder.indexDocument(new Document(1, "Java", file));

        assertEquals(5, index.getDocumentLength(1));
    }

    @Test
    void countsRepeatedTokens() throws IOException {
        Path file = tempDir.resolve("repeated.txt");
        Files.writeString(file, "java java java python",
                StandardCharsets.UTF_8);

        builder.indexDocument(new Document(2, "Repeated", file));

        assertEquals(4, index.getDocumentLength(2));
    }

    @Test
    void recordsZeroForEmptyDocument() throws IOException {
        Path file = tempDir.resolve("empty.txt");
        Files.writeString(file, "", StandardCharsets.UTF_8);

        builder.indexDocument(new Document(3, "Empty", file));

        assertEquals(0, index.getDocumentLength(3));
    }

    @Test
    void updatesLengthWhenDocumentIsReindexed() throws IOException {
        Path file = tempDir.resolve("updated.txt");

        Files.writeString(file, "java python", StandardCharsets.UTF_8);
        Document document = new Document(4, "Updated", file);

        builder.indexDocument(document);
        assertEquals(2, index.getDocumentLength(4));

        Files.writeString(file, "java python kotlin rust",
                StandardCharsets.UTF_8);

        builder.indexDocument(document);

        assertEquals(4, index.getDocumentLength(4));
    }

    @Test
    void failedReadDoesNotUpdateDocumentLength() throws IOException {
        Path file = tempDir.resolve("missing.txt");
        Document document = new Document(5, "Missing", file);

        assertThrows(IOException.class,
                () -> builder.indexDocument(document));

        assertEquals(0, index.getDocumentLength(5));
    }
}
