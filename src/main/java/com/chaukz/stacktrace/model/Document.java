package com.chaukz.stacktrace.model;

import java.nio.file.Path;

public record Document(int id, String title, Path path) {
    public Document {
        if (id <= 0) {
            throw new IllegalArgumentException("Document id must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Document title must not be blank");
        }
        if (path == null) {
            throw new IllegalArgumentException("Document path must not be null");
        }
    }
}
