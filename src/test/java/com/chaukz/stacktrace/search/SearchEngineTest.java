package com.chaukz.stacktrace.search;

import com.chaukz.stacktrace.indexing.InvertedIndex;
import com.chaukz.stacktrace.model.Posting;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchEngineTest {
    @Test
    void searchingJavaReturnsMatchingDocumentIds() {
        InvertedIndex index = new InvertedIndex();
        index.addPosting("java", new Posting(1, List.of(0)));
        index.addPosting("python", new Posting(2, List.of(0)));
        index.addPosting("java", new Posting(3, List.of(1)));

        SearchEngine searchEngine = new SearchEngine(index);

        assertEquals(Set.of(1, 3), searchEngine.search("java"));
    }

    @Test
    void searchingUnknownTermReturnsEmptySet() {
        InvertedIndex index = new InvertedIndex();
        index.addPosting("java", new Posting(1, List.of(0)));

        SearchEngine searchEngine = new SearchEngine(index);

        assertEquals(Set.of(), searchEngine.search("rust"));
    }

    @Test
    void searchNormalizesQueryCase() {
        InvertedIndex index = new InvertedIndex();
        index.addPosting("java", new Posting(1, List.of(0)));
        index.addPosting("java", new Posting(3, List.of(2)));

        SearchEngine searchEngine = new SearchEngine(index);

        assertEquals(Set.of(1, 3), searchEngine.search("java"));
        assertEquals(Set.of(1, 3), searchEngine.search("Java"));
        assertEquals(Set.of(1, 3), searchEngine.search("JAVA"));
    }

    @Test
    void emptyQueryReturnsNoResults() {
        InvertedIndex index = new InvertedIndex();
        SearchEngine searchEngine = new SearchEngine(index);

        assertEquals(Set.of(), searchEngine.search(""));
        assertEquals(Set.of(), searchEngine.search("   "));
    }

}
