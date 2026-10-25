package com.chaukz.stacktrace.analysis;

import com.chaukz.stacktrace.model.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenizerTest {
    private final Tokenizer tokenizer = new Tokenizer();

    @Test
    void tokenizesPunctuationAndWhitespaceWithSequentialPositions() {
        assertEquals(List.of(
                new Token("hello", 0),
                new Token("world", 1),
                new Token("42", 2)
        ), tokenizer.tokenize("  HELLO,\tworld! 42. "));
    }

    @Test
    void tokenizesUnicodeLettersAndDigits() {
        assertEquals(List.of(
                new Token("café", 0),
                new Token("東京", 1),
                new Token("123", 2)
        ), tokenizer.tokenize("CAFÉ 東京 １２３"));
    }

    @Test
    void preservesEachRepeatedTermOccurrenceAndPosition() {
        assertEquals(List.of(
                new Token("echo", 0),
                new Token("echo", 1),
                new Token("echo", 2)
        ), tokenizer.tokenize("echo echo echo"));
    }

    @Test
    void returnsNoTokensForEmptyOrWhitespaceText() {
        assertEquals(List.of(), tokenizer.tokenize(""));
        assertEquals(List.of(), tokenizer.tokenize(" \t\n "));
    }
}
