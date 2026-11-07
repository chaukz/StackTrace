package com.chaukz.stacktrace.search;

import com.chaukz.stacktrace.analysis.Tokenizer;
import com.chaukz.stacktrace.indexing.InvertedIndex;
import com.chaukz.stacktrace.model.SearchResult;
import com.chaukz.stacktrace.model.Token;

import java.util.*;

public class SearchEngine {

    private final Tokenizer tokenizer;
    private final InvertedIndex index;
    private final TfIdfScorer scorer;

    // Constructor for existing Boolean search tests
    public SearchEngine(InvertedIndex index) {
        this(index, null);
    }

    // Constructor with TF-IDF ranking support
    public SearchEngine(InvertedIndex index, TfIdfScorer scorer) {
        this.index = Objects.requireNonNull(index);
        this.tokenizer = new Tokenizer();
        this.scorer = scorer;
    }

    // Single-term search
    private Set<Integer> searchTerm(String query) {
        if (query == null || query.isBlank()) {
            return Set.of();
        }

        List<Token> tokens = tokenizer.tokenize(query);

        if (tokens.isEmpty()) {
            return Set.of();
        }

        String term = tokens.get(0).term();

        return index.getPostings(term).keySet();
    }

    // Main query parser
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
            String operator = parts[1].toUpperCase(Locale.ROOT);
            String second = parts[2];

            return switch (operator) {
                case "AND" -> searchAnd(first, second);
                case "OR" -> searchOr(first, second);
                case "NOT" -> searchNot(first, second);

                default -> throw new IllegalArgumentException(
                        "Unknown operator: " + operator
                );
            };
        }

        throw new IllegalArgumentException(
                "Invalid query: " + query
        );
    }

    // AND: Set intersection
    public Set<Integer> searchAnd(String first, String second) {
        Set<Integer> firstResults = searchTerm(first);
        Set<Integer> secondResults = searchTerm(second);

        Set<Integer> smaller =
                firstResults.size() <= secondResults.size()
                        ? firstResults
                        : secondResults;

        Set<Integer> larger =
                firstResults.size() <= secondResults.size()
                        ? secondResults
                        : firstResults;

        Set<Integer> intersection = new HashSet<>(smaller);
        intersection.retainAll(larger);

        return intersection;
    }

    // OR: Set union
    public Set<Integer> searchOr(String first, String second) {
        Set<Integer> firstResults = searchTerm(first);
        Set<Integer> secondResults = searchTerm(second);

        Set<Integer> union = new HashSet<>(firstResults);
        union.addAll(secondResults);

        return union;
    }

    // NOT: Set difference
    public Set<Integer> searchNot(String include, String exclude) {
        Set<Integer> included = searchTerm(include);
        Set<Integer> excluded = searchTerm(exclude);

        Set<Integer> result = new HashSet<>(included);
        result.removeAll(excluded);

        return result;
    }

    private double calculateQueryScore(String query, int documentId) {
        String[] parts = query.trim().split("\\s+");
        List<Token> firstTokens = tokenizer.tokenize(parts[0]);
        double firstScore = firstTokens.isEmpty() ? 0.0 : scorer.score(firstTokens.get(0).term(), documentId);

        if (parts.length == 1 || parts[1].equalsIgnoreCase("NOT")) {
            return firstScore;
        }

        List<Token> secondTokens = tokenizer.tokenize(parts[2]);
        return firstScore + (secondTokens.isEmpty() ? 0.0 : scorer.score(secondTokens.get(0).term(), documentId));
    }

    // Ranked search using TF-IDF
    public List<SearchResult> searchRanked(String query) {
        if (scorer == null) {
            throw new IllegalStateException(
                    "TF-IDF scorer is not configured"
            );
        }

        if (query == null || query.isBlank()) {
            return List.of();
        }

        Set<Integer> documentIds = search(query);
        List<SearchResult> results = new ArrayList<>();

        for (int documentId : documentIds) {
            double score = calculateQueryScore(query, documentId);

            results.add(
                    new SearchResult(documentId, score)
            );
        }

        // Highest relevance first; document ID breaks ties
        results.sort(
                Comparator.comparingDouble(SearchResult::score)
                        .reversed()
                        .thenComparingInt(SearchResult::documentId)
        );

        return results;
    }
}
