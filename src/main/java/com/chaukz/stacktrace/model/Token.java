package com.chaukz.stacktrace.model;

public record Token(String term, int position) {
    public Token {
        if (term == null || term.isBlank()) {
            throw new IllegalArgumentException("Token term must not be blank");
        }
        if (position < 0) {
            throw new IllegalArgumentException("Token position must not be negative");
        }
    }
}
