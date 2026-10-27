package com.chaukz.stacktrace.indexing;

import com.chaukz.stacktrace.model.Document;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
