package com.chaukz.stacktrace.analysis;

import com.chaukz.stacktrace.model.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenizerTest {
    private final Tokenizer tokenizer = new Tokenizer();

    @Test
    void lowercasesTermsAndPreservesZeroBasedPositions() {
        assertEquals(List.of(new Token("hello", 0), new Token("world", 1)),
                tokenizer.tokenize("Hello WORLD"));
    }

    @Test
    void punctuationAndWhitespaceSeparateTerms() {
        assertEquals(List.of(new Token("one", 0), new Token("two", 1), new Token("three", 2)),
                tokenizer.tokenize("one,\t two!three."));
    }

    @Test
    void emptyOrPunctuationOnlyTextProducesNoTokens() {
        assertEquals(List.of(), tokenizer.tokenize(""));
        assertEquals(List.of(), tokenizer.tokenize("... — !!!"));
    }

    @Test
    void repeatedTermsKeepDistinctPositions() {
        assertEquals(List.of(new Token("echo", 0), new Token("echo", 1), new Token("echo", 2)),
                tokenizer.tokenize("echo echo echo"));
    }

    @Test
    void handlesUnicodeLettersDigitsAndCompatibilityForms() {
        assertEquals(List.of(new Token("café", 0), new Token("東京", 1), new Token("42", 2),
                        new Token("office", 3)),
                tokenizer.tokenize("Cafe\u0301 東京 ４２ oﬃce"));
    }

    @Test
    void rejectsNullText() {
        assertThrows(IllegalArgumentException.class, () -> tokenizer.tokenize(null));
    }
}
