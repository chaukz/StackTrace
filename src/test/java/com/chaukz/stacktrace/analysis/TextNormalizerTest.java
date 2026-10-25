package com.chaukz.stacktrace.analysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextNormalizerTest {
    private final TextNormalizer normalizer = new TextNormalizer();

    @Test
    void normalizesCompatibilityCharactersAndCase() {
        assertEquals("office 123", normalizer.normalize("ＯＦＦＩＣＥ ①②③"));
    }

    @Test
    void normalizesEmptyText() {
        assertEquals("", normalizer.normalize(""));
    }

    @Test
    void rejectsNullText() {
        assertThrows(IllegalArgumentException.class, () -> normalizer.normalize(null));
    }
}
