package com.chaukz.stacktrace.search;

import com.chaukz.stacktrace.indexing.InvertedIndex;
import com.chaukz.stacktrace.model.Posting;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void searchAndReturnsIntersectionWithoutChangingIndex() {
        InvertedIndex index = new InvertedIndex();
        Posting javaOne = new Posting(1, List.of(0));
        Posting javaTwo = new Posting(2, List.of(1));
        Posting pythonTwo = new Posting(2, List.of(0));
        Posting pythonThree = new Posting(3, List.of(1));
        Posting pythonFour = new Posting(4, List.of(2));
        index.addPosting("java", javaOne);
        index.addPosting("java", javaTwo);
        index.addPosting("python", pythonTwo);
        index.addPosting("python", pythonThree);
        index.addPosting("python", pythonFour);

        SearchEngine searchEngine = new SearchEngine(index);

        assertEquals(Set.of(2), searchEngine.searchAnd("java", "python"));
        assertEquals(Map.of(1, javaOne, 2, javaTwo), index.getPostings("java"));
        assertEquals(Map.of(2, pythonTwo, 3, pythonThree, 4, pythonFour), index.getPostings("python"));
    }

    @Test
    void searchOrReturnsUnionWithoutModifyingIndex() {
        InvertedIndex index = new InvertedIndex();

        index.addPosting("java", new Posting(1, List.of(0)));
        index.addPosting("java", new Posting(2, List.of(0)));
        index.addPosting("python", new Posting(2, List.of(1)));
        index.addPosting("python", new Posting(3, List.of(0)));

        SearchEngine engine = new SearchEngine(index);

        assertEquals(Set.of(1, 2, 3), engine.searchOr("java", "python"));
        assertEquals(Set.of(1, 2), engine.search("java"));
        assertEquals(Set.of(2, 3), engine.search("python"));
        assertEquals(Set.of(1, 2), engine.searchOr("java", "rust"));
        assertEquals(Set.of(), engine.searchOr("rust", "go"));
        assertEquals(Set.of(1, 2, 3), engine.searchOr("JAVA", "Python"));
    }

    @Test
    void searchNotExcludesDocumentsWithoutModifyingIndex() {
        InvertedIndex index = new InvertedIndex();

        index.addPosting("java", new Posting(1, List.of(0)));
        index.addPosting("java", new Posting(2, List.of(0)));
        index.addPosting("java", new Posting(4, List.of(0)));

        index.addPosting("python", new Posting(2, List.of(1)));
        index.addPosting("python", new Posting(3, List.of(0)));
        index.addPosting("python", new Posting(4, List.of(1)));

        SearchEngine engine = new SearchEngine(index);

        assertEquals(Set.of(1), engine.searchNot("java", "python"));
        assertEquals(Set.of(3), engine.searchNot("python", "java"));
        assertEquals(Set.of(1, 2, 4), engine.searchNot("java", "rust"));
        assertEquals(Set.of(), engine.searchNot("java", "java"));
        assertEquals(Set.of(), engine.searchNot("rust", "java"));

        // Verify the original index remains unchanged
        assertEquals(Set.of(1, 2, 4), engine.search("java"));
        assertEquals(Set.of(2, 3, 4), engine.search("python"));
    }

    @Test
    void parsesBooleanQueriesCorrectly() {
        InvertedIndex index = new InvertedIndex();

        index.addPosting("java", new Posting(1, List.of(0)));
        index.addPosting("java", new Posting(2, List.of(0)));
        index.addPosting("java", new Posting(4, List.of(0)));

        index.addPosting("python", new Posting(2, List.of(1)));
        index.addPosting("python", new Posting(3, List.of(0)));
        index.addPosting("python", new Posting(4, List.of(1)));

        SearchEngine engine = new SearchEngine(index);

        // Single-term search
        assertEquals(Set.of(1, 2, 4), engine.search("java"));

        // AND
        assertEquals(Set.of(2, 4), engine.search("java AND python"));

        // OR
        assertEquals(Set.of(1, 2, 3, 4), engine.search("java OR python"));

        // NOT
        assertEquals(Set.of(1), engine.search("java NOT python"));

        // Case-insensitive operators and terms
        assertEquals(Set.of(2, 4), engine.search("JAVA and PYTHON"));

        // Empty and unknown queries
        assertEquals(Set.of(), engine.search(""));
        assertEquals(Set.of(), engine.search("rust"));

        // Invalid queries
        assertThrows(IllegalArgumentException.class,
                () -> engine.search("java XOR python"));

        assertThrows(IllegalArgumentException.class,
                () -> engine.search("java AND"));

        // Ensure the index remains unchanged
        assertEquals(Set.of(1, 2, 4), engine.search("java"));
    }
}
