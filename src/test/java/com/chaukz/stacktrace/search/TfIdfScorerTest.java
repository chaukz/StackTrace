package com.chaukz.stacktrace.search;

import com.chaukz.stacktrace.indexing.InvertedIndex;
import com.chaukz.stacktrace.model.Posting;
import com.chaukz.stacktrace.model.Document;
import java.nio.file.Path;
import com.chaukz.stacktrace.indexing.DocumentRegistry;
import org.junit.jupiter.api.Test;
import com.chaukz.stacktrace.model.Document;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TfIdfScorerTest {

    @Test
    void calculatesInverseDocumentFrequencyCorrectly() {
        InvertedIndex index = new InvertedIndex();
        DocumentRegistry registry = new DocumentRegistry();

        registry.add(new Document(1, "Java Basics", Path.of("java.txt")));
        registry.add(new Document(2, "Python Basics", Path.of("python.txt")));
        registry.add(new Document(3, "Algorithms", Path.of("algorithms.txt")));
        registry.add(new Document(4, "Databases", Path.of("databases.txt")));

        // "java" appears in 2 of the 4 documents.
        index.addPosting("java", new Posting(1, List.of(0, 3)));
        index.addPosting("java", new Posting(2, List.of(1)));

        TfIdfScorer scorer = new TfIdfScorer(index, registry);

        double expected = Math.log(4.0 / 2);

        assertEquals(
                expected,
                scorer.inverseDocumentFrequency("java"),
                0.000001
        );
    }

    @Test
    void returnsZeroForUnknownTerm() {
        InvertedIndex index = new InvertedIndex();
        DocumentRegistry registry = new DocumentRegistry();

        TfIdfScorer scorer = new TfIdfScorer(index, registry);

        assertEquals(0.0, scorer.inverseDocumentFrequency("rust"));
    }

    @Test
    void calculatesTfIdfScoreCorrectly() {
        InvertedIndex index = new InvertedIndex();
        DocumentRegistry registry = new DocumentRegistry();

        registry.add(new Document(1, "Java Basics", Path.of("java.txt")));
        registry.add(new Document(2, "Python Basics", Path.of("python.txt")));
        registry.add(new Document(3, "Algorithms", Path.of("algorithms.txt")));
        registry.add(new Document(4, "Databases", Path.of("databases.txt")));

        index.addPosting("java", new Posting(1, List.of(0, 3, 7)));
        index.addPosting("java", new Posting(2, List.of(1)));

        TfIdfScorer scorer = new TfIdfScorer(index, registry);

        double idf = Math.log(4.0 / 2);

        // Document 1: TF = 3
        assertEquals(3 * idf, scorer.score("java", 1), 0.000001);

        // Document 2: TF = 1
        assertEquals(idf, scorer.score("java", 2), 0.000001);

        // Document 3: Java does not appear
        assertEquals(0.0, scorer.score("java", 3), 0.000001);

        // Unknown term
        assertEquals(0.0, scorer.score("rust", 1), 0.000001);

        // Unknown document
        assertEquals(0.0, scorer.score("java", 99), 0.000001);
    }
}