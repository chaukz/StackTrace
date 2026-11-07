package com.chaukz.stacktrace.search;

import com.chaukz.stacktrace.analysis.Tokenizer;
import com.chaukz.stacktrace.indexing.InvertedIndex;
import com.chaukz.stacktrace.model.Token;

import java.util.HashSet;
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

    public Set<Integer> searchAnd(String first, String second) {
        Set<Integer> firstResults = search(first);
        Set<Integer> secondResults = search(second);

        Set<Integer> smaller = firstResults.size() <= secondResults.size()
                ? firstResults : secondResults;

        Set<Integer> larger = firstResults.size() <= secondResults.size()
                ? secondResults : firstResults;

        Set<Integer> intersection = new HashSet<>(smaller);
        intersection.retainAll(larger);

        return intersection;
    }

    public Set<Integer> searchOr(String first, String second) {
        Set<Integer> firstResults = search(first);
        Set<Integer> secondResults = search(second);

        Set<Integer> smaller = firstResults.size() <= secondResults.size()
                ? firstResults : secondResults;

        Set<Integer> larger = firstResults.size() <= secondResults.size()
                ? secondResults : firstResults;

        Set<Integer> union = new HashSet<>(smaller);
        union.addAll(larger);

        return union;
    }
}
