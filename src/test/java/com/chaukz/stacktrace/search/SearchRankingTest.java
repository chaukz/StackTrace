package com.chaukz.stacktrace.search;

import com.chaukz.stacktrace.indexing.DocumentRegistry;
import com.chaukz.stacktrace.indexing.InvertedIndex;
import com.chaukz.stacktrace.model.Document;
import com.chaukz.stacktrace.model.Posting;
import com.chaukz.stacktrace.model.SearchResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchRankingTest {

    private SearchEngine engine;

    @BeforeEach
    void setUp() {
        InvertedIndex index = new InvertedIndex();
        DocumentRegistry registry = new DocumentRegistry();

        // Register four documents
        registry.add(new Document(1, "Java Basics", Path.of("java.txt")));
        registry.add(new Document(2, "Python Basics", Path.of("python.txt")));
        registry.add(new Document(3, "Advanced Java", Path.of("advanced.txt")));
        registry.add(new Document(4, "Databases", Path.of("database.txt")));

        // Java occurs in three documents
        // Document 1: TF = 3
        index.addPosting("java", new Posting(1, List.of(0, 3, 7)));

        // Document 2: TF = 1
        index.addPosting("java", new Posting(2, List.of(1)));

        // Document 3: TF = 2
        index.addPosting("java", new Posting(3, List.of(0, 4)));

        // Python occurs in one document
        index.addPosting("python", new Posting(2, List.of(0, 5)));

        index.setDocumentLength(1, 10);
        index.setDocumentLength(2, 10);
        index.setDocumentLength(3, 10);
        index.setDocumentLength(4, 10);

        TfIdfScorer scorer = new TfIdfScorer(index, registry);
        engine = new SearchEngine(index, scorer);
    }

    @Test
    void ranksDocumentsByTfIdfScoreDescending() {
        List<SearchResult> results = engine.searchRanked("java");

        assertEquals(3, results.size());

        // Highest score first
        assertEquals(1, results.get(0).documentId());
        assertEquals(3, results.get(1).documentId());
        assertEquals(2, results.get(2).documentId());

        double idf = Math.log(4.0 / 3);

        assertEquals((3.0 / 10) * idf, results.get(0).score(), 0.000001);
        assertEquals((2.0 / 10) * idf, results.get(1).score(), 0.000001);
        assertEquals((1.0 / 10) * idf, results.get(2).score(), 0.000001);
        assertEquals(Path.of("java.txt"), results.get(0).path());
        assertEquals("Java Basics", results.get(0).title());
    }

    @Test
    void rankedSearchIsCaseInsensitive() {
        List<SearchResult> lowercase = engine.searchRanked("java");
        List<SearchResult> uppercase = engine.searchRanked("JAVA");

        assertEquals(lowercase, uppercase);
    }

    @Test
    void returnsEmptyListForUnknownTerm() {
        List<SearchResult> results = engine.searchRanked("rust");

        assertTrue(results.isEmpty());
    }

    @Test
    void returnsEmptyListForBlankOrNullQuery() {
        assertTrue(engine.searchRanked("").isEmpty());
        assertTrue(engine.searchRanked("   ").isEmpty());
        assertTrue(engine.searchRanked(null).isEmpty());
    }

    @Test
    void andRanksOnlyDocumentsContainingBothTerms() {
        assertEquals(
                List.of(new SearchResult(2, Path.of("python.txt"), "Python Basics",
                        (1.0 / 10) * Math.log(4.0 / 3)
                        + (2.0 / 10) * Math.log(4))),
                engine.searchRanked("JAVA and PYTHON")
        );
    }

    @Test
    void orAddsBothTermScoresForMatchingDocuments() {
        double javaIdf = Math.log(4.0 / 3);

        assertEquals(
                List.of(
                        new SearchResult(2, Path.of("python.txt"), "Python Basics",
                                (1.0 / 10) * javaIdf + (2.0 / 10) * Math.log(4)),
                        new SearchResult(1, Path.of("java.txt"), "Java Basics", (3.0 / 10) * javaIdf),
                        new SearchResult(3, Path.of("advanced.txt"), "Advanced Java", (2.0 / 10) * javaIdf)
                ),
                engine.searchRanked("java OR python")
        );
    }

    @Test
    void notUsesExcludedTermOnlyAsFilter() {
        double javaIdf = Math.log(4.0 / 3);

        assertEquals(
                List.of(
                        new SearchResult(1, Path.of("java.txt"), "Java Basics", (3.0 / 10) * javaIdf),
                        new SearchResult(3, Path.of("advanced.txt"), "Advanced Java", (2.0 / 10) * javaIdf)
                ),
                engine.searchRanked("java NOT python")
        );
    }

    @Test
    void invalidRankedQueriesStillFail() {
        assertThrows(IllegalArgumentException.class, () -> engine.searchRanked("java XOR python"));
        assertThrows(IllegalArgumentException.class, () -> engine.searchRanked("java AND"));
    }

    @Test
    void emptyTokenInOrQueryDoesNotPreventOtherTermFromRanking() {
        assertEquals(engine.searchRanked("java"), engine.searchRanked("!!! OR java"));
    }

    @Test
    void preservesOriginalBooleanSearchFunctionality() {
        assertEquals(
                java.util.Set.of(2),
                engine.search("java AND python")
        );

        assertEquals(
                java.util.Set.of(1, 2, 3),
                engine.search("java OR python")
        );

        assertEquals(
                java.util.Set.of(1, 3),
                engine.search("java NOT python")
        );
    }

    @Test
    void breaksScoreTiesUsingDocumentId() {
        // Documents 1 and 3 have equal Python-free Java scores
        // only in this separate setup.
        InvertedIndex index = new InvertedIndex();
        DocumentRegistry registry = new DocumentRegistry();

        registry.add(new Document(1, "First", Path.of("first.txt")));
        registry.add(new Document(2, "Second", Path.of("second.txt")));
        registry.add(new Document(3, "Third", Path.of("third.txt")));

        index.addPosting("java", new Posting(3, List.of(0)));
        index.addPosting("java", new Posting(1, List.of(0)));
        index.setDocumentLength(1, 10);
        index.setDocumentLength(3, 10);

        SearchEngine tiedEngine = new SearchEngine(
                index,
                new TfIdfScorer(index, registry)
        );

        List<SearchResult> results = tiedEngine.searchRanked("java");

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).documentId());
        assertEquals(3, results.get(1).documentId());
        assertEquals(results.get(0).score(), results.get(1).score(), 0.000001);
        assertTrue(results.get(0).score() > 0);
    }

    @Test
    void rankingRequiresConfiguredScorer() {
        SearchEngine basicEngine = new SearchEngine(new InvertedIndex());

        assertThrows(
                IllegalStateException.class,
                () -> basicEngine.searchRanked("java")
        );
    }

    @Test
    void shorterDocumentCanOutrankLongerDocumentWithMoreMatches() {
        InvertedIndex index = new InvertedIndex();
        DocumentRegistry registry = new DocumentRegistry();
        registry.add(new Document(1, "Short", Path.of("short.txt")));
        registry.add(new Document(2, "Long", Path.of("long.txt")));
        registry.add(new Document(3, "Other", Path.of("other.txt")));
        index.addPosting("java", new Posting(1, List.of(0, 1)));
        index.addPosting("java", new Posting(2, List.of(0, 1, 2)));
        index.setDocumentLength(1, 4);
        index.setDocumentLength(2, 12);
        index.setDocumentLength(3, 1);
        SearchEngine ranked = new SearchEngine(index, new TfIdfScorer(index, registry));

        List<SearchResult> results = ranked.searchRanked("java");

        assertEquals(List.of(1, 2), results.stream().map(SearchResult::documentId).toList());
        assertEquals(0.5 * Math.log(3.0 / 2), results.get(0).score(), 0.000001);
        assertEquals(0.25 * Math.log(3.0 / 2), results.get(1).score(), 0.000001);
    }

    @Test
    void topKMatchesPrefixOfFullRanking() {
        for (String query : List.of("java", "java AND python", "java OR python", "java NOT python")) {
            List<SearchResult> full = engine.searchRanked(query);
            for (int k = 0; k <= full.size() + 1; k++) {
                assertEquals(full.subList(0, Math.min(k, full.size())), engine.searchRanked(query, k));
            }
        }
    }

    @Test
    void topKUsesDocumentIdToBreakScoreTies() {
        InvertedIndex index = new InvertedIndex();
        DocumentRegistry registry = new DocumentRegistry();
        registry.add(new Document(1, "First", Path.of("first.txt")));
        registry.add(new Document(2, "Second", Path.of("second.txt")));
        registry.add(new Document(3, "Other", Path.of("other.txt")));
        index.addPosting("java", new Posting(2, List.of(0)));
        index.addPosting("java", new Posting(1, List.of(0)));
        index.setDocumentLength(1, 2);
        index.setDocumentLength(2, 2);
        SearchEngine ranked = new SearchEngine(index, new TfIdfScorer(index, registry));

        assertEquals(1, ranked.searchRanked("java", 1).get(0).documentId());
    }

    @Test
    void topKHandlesEmptyAndInvalidRequests() {
        assertTrue(engine.searchRanked("unknown", 3).isEmpty());
        assertTrue(engine.searchRanked(" ", 3).isEmpty());
        assertTrue(engine.searchRanked(null, 3).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> engine.searchRanked("java", -1));
        assertThrows(IllegalArgumentException.class, () -> engine.searchRanked("java XOR python", 2));
    }
}
