package com.chaukz.stacktrace.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentTest {
    @Test
    void acceptsValidMetadata() {
        assertDoesNotThrow(() -> new Document(1, "notes.txt", Path.of("notes.txt")));
    }

    @Test
    void rejectsInvalidMetadata() {
        assertThrows(IllegalArgumentException.class, () -> new Document(0, "notes", Path.of("notes")));
        assertThrows(IllegalArgumentException.class, () -> new Document(-1, "notes", Path.of("notes")));
        assertThrows(IllegalArgumentException.class, () -> new Document(1, "  ", Path.of("notes")));
        assertThrows(IllegalArgumentException.class, () -> new Document(1, null, Path.of("notes")));
        assertThrows(IllegalArgumentException.class, () -> new Document(1, "notes", null));
    }
}
