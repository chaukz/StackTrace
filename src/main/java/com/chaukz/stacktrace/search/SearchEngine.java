package com.chaukz.stacktrace.search;

import com.chaukz.stacktrace.analysis.Tokenizer;
import com.chaukz.stacktrace.indexing.InvertedIndex;
import com.chaukz.stacktrace.model.Token;
import java.util.List;
import java.util.Set;

public class SearchEngine {
    public Tokenizer tokenizer = new Tokenizer();


    private final InvertedIndex index;
    public SearchEngine(InvertedIndex index) {
        this.index = index;

    }
    public Set<Integer> search(String query) {
        List<Token> tokens = tokenizer.tokenize(query);

        if(tokens.isEmpty()) return Set.of();
        String term = tokens.get(0).term();
        return index.getPostings(term).keySet();
    }
}
