package com.chaukz.stacktrace.analysis;

import com.chaukz.stacktrace.model.Token;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    TextNormalizer normalizer = new TextNormalizer();
    public List<Token> tokenize(String text) {
        List<Token> tokens = new ArrayList<>();
        text = normalizer.normalize(text);
        int position = 0;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length();) {
            int codePoint = text.codePointAt(i);

            if (Character.isLetterOrDigit(codePoint)) {
                sb.appendCodePoint(codePoint);
            }
            else if (sb.length() > 0){
                tokens.add(new Token(sb.toString(), position));
                position++;
                sb.setLength(0);
                }
            i += Character.charCount(codePoint);
            }
        if (sb.length() > 0) {
            tokens.add(new Token(sb.toString(), position));
        }
        return tokens;
    }
}
