package com.chaukz.stacktrace.indexing;

import com.chaukz.stacktrace.model.Document;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

class DocumentRegistryTest {
    @Test
    void addsAndFindsDocument() {
        DocumentRegistry registry = new DocumentRegistry();
        Document document = new Document(1, "notes.txt", Path.of("notes.txt"));

        registry.add(document);

        assertEquals(document, registry.get(1));
        assertTrue(registry.contains(1));
    }

    @Test
    void returnsNullForUnknownId() {
        DocumentRegistry registry = new DocumentRegistry();

        assertNull(registry.get(99));
        assertFalse(registry.contains(99));
    }

    @Test
    void rejectsDuplicateIdWithoutReplacingOriginal() {
        DocumentRegistry registry = new DocumentRegistry();
        Document original = new Document(1, "original.txt", Path.of("original.txt"));
        Document duplicate = new Document(1, "duplicate.txt", Path.of("duplicate.txt"));
        registry.add(original);

        assertThrows(IllegalArgumentException.class, () -> registry.add(duplicate));
        assertEquals(original, registry.get(1));
    }

    @Test
    void rejectsDuplicateNormalizedPathWithDifferentId() {
        DocumentRegistry registry = new DocumentRegistry();
        Path path = Path.of("notes.txt");
        registry.add(new Document(1, "notes.txt", path));

        assertThrows(IllegalArgumentException.class,
                () -> registry.add(new Document(2, "notes.txt", path.resolveSibling("folder")
                        .resolve("../notes.txt"))));
    }

    @Test
    void rejectsNullDocument() {
        DocumentRegistry registry = new DocumentRegistry();

        assertThrows(IllegalArgumentException.class, () -> registry.add(null));
    }

    @Test
    void upsertAddsNewDocument() {
        DocumentRegistry registry = new DocumentRegistry();
        Document document = new Document(1, "Notes", Path.of("notes.txt"));

        registry.upsert(document);

        assertEquals(document, registry.get(1));
        assertTrue(registry.contains(1));
    }

    @Test
    void upsertUpdatesExistingDocument() {
        DocumentRegistry registry = new DocumentRegistry();
        Document original = new Document(1, "Old Notes", Path.of("notes.txt"));
        Document updated = new Document(1, "Updated Notes", Path.of("notes.txt"));

        registry.upsert(original);
        registry.upsert(updated);

        assertEquals(updated, registry.get(1));
        assertEquals("Updated Notes", registry.get(1).title());
    }

    @Test
    void upsertRejectsConflictingIds() {
        DocumentRegistry registry = new DocumentRegistry();
        Document original = new Document(1, "Java", Path.of("java.txt"));
        Document conflicting = new Document(1, "Python", Path.of("python.txt"));

        registry.upsert(original);

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.upsert(conflicting)
        );

        assertEquals(original, registry.get(1));
    }

    @Test
    void upsertRejectsConflictingPaths() {
        DocumentRegistry registry = new DocumentRegistry();
        Document original = new Document(1, "Java", Path.of("java.txt"));
        Document conflicting = new Document(2, "Java Copy", Path.of("java.txt"));

        registry.upsert(original);

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.upsert(conflicting)
        );

        assertEquals(original, registry.get(1));
        assertFalse(registry.contains(2));
    }

    @Test
    void documentCountTracksUniqueDocuments() {
        DocumentRegistry registry = new DocumentRegistry();

        assertEquals(0, registry.documentCount());

        registry.upsert(new Document(1, "Java", Path.of("java.txt")));
        assertEquals(1, registry.documentCount());

        registry.upsert(new Document(5, "Python", Path.of("python.txt")));
        assertEquals(2, registry.documentCount());

        registry.upsert(new Document(1, "Updated Java", Path.of("java.txt")));
        assertEquals(2, registry.documentCount());

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.upsert(new Document(6, "Duplicate", Path.of("java.txt")))
        );

        assertEquals(2, registry.documentCount());
    }
}
