package com.chaukz.stacktrace.analysis;

import com.chaukz.stacktrace.model.Token;

import java.util.ArrayList;
import java.util.List;

/** Splits normalized text into contiguous Unicode letter-or-digit runs. */
public final class Tokenizer {
    private final TextNormalizer normalizer;

    public Tokenizer() {
        this(new TextNormalizer());
    }

    public Tokenizer(TextNormalizer normalizer) {
        if (normalizer == null) {
            throw new IllegalArgumentException("Normalizer must not be null");
        }
        this.normalizer = normalizer;
    }

    public List<Token> tokenize(String text) {
        String normalized = normalizer.normalize(text);
        List<Token> tokens = new ArrayList<>();
        StringBuilder term = new StringBuilder();
        int position = 0;

        for (int offset = 0; offset < normalized.length();) {
            int codePoint = normalized.codePointAt(offset);
            if (Character.isLetterOrDigit(codePoint)) {
                term.appendCodePoint(codePoint);
            } else if (!term.isEmpty()) {
                tokens.add(new Token(term.toString(), position++));
                term.setLength(0);
            }
            offset += Character.charCount(codePoint);
        }
        if (!term.isEmpty()) {
            tokens.add(new Token(term.toString(), position));
        }
        return List.copyOf(tokens);
    }
}
