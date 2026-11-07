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
    private Set<Integer> searchTerm(String query) {
        List<Token> tokens = tokenizer.tokenize(query);

        if(tokens.isEmpty()) return Set.of();
        String term = tokens.get(0).term();
        return index.getPostings(term).keySet();
    }

    public Set<Integer> search(String query) {
        if (query == null || query.isBlank()) {
            return Set.of();
        }

        String[] parts = query.trim().split("\\s+");

        if (parts.length == 1) {
            return searchTerm(parts[0]);
        }

        if (parts.length == 3) {
            String first = parts[0];
            String operator = parts[1].toUpperCase();
            String second = parts[2];

            // Your Boolean logic goes here
            return switch (operator) {
                case "AND" -> searchAnd(first, second);
                case "OR"  -> searchOr(first, second);
                case "NOT" -> searchNot(first, second);
                default -> throw new IllegalArgumentException(
                        "Unknown operator: " + operator
                );
            };
        }

        throw new IllegalArgumentException("Invalid query: " + query);
    }

    public Set<Integer> searchAnd(String first, String second) {
        Set<Integer> firstResults = searchTerm(first);
        Set<Integer> secondResults = searchTerm(second);

        Set<Integer> smaller = firstResults.size() <= secondResults.size()
                ? firstResults : secondResults;

        Set<Integer> larger = firstResults.size() <= secondResults.size()
                ? secondResults : firstResults;

        Set<Integer> intersection = new HashSet<>(smaller);
        intersection.retainAll(larger);

        return intersection;
    }

    public Set<Integer> searchOr(String first, String second) {
        Set<Integer> firstResults = searchTerm(first);
        Set<Integer> secondResults = searchTerm(second);

        Set<Integer> smaller = firstResults.size() <= secondResults.size()
                ? firstResults : secondResults;

        Set<Integer> larger = firstResults.size() <= secondResults.size()
                ? secondResults : firstResults;

        Set<Integer> union = new HashSet<>(smaller);
        union.addAll(larger);

        return union;
    }

    public Set<Integer> searchNot(String include, String exclude) {

        Set <Integer> included = searchTerm(include);
        Set <Integer> excluded = searchTerm(exclude);

        Set <Integer> result = new HashSet<>(included);
        result.removeAll(excluded);
        return result;

    }
}
