package com.chaukz.stacktrace.analysis;

import java.text.Normalizer;
import java.util.Locale;

/** Applies compatibility Unicode normalization and locale-independent lowercase conversion. */
public final class TextNormalizer {
    public String normalize(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text must not be null");
        }
        return Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }
}
