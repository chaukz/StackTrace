package com.chaukz.stacktrace.indexing;

import com.chaukz.stacktrace.model.Posting;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvertedIndexTest {
    @Test
    void storesPostingsForMultipleDocumentsUnderOneTerm() {
        InvertedIndex index = new InvertedIndex();
        Posting firstDocument = new Posting(1, List.of(0, 3));
        Posting secondDocument = new Posting(2, List.of(1));

        index.addPosting("java", firstDocument);
        index.addPosting("java", secondDocument);

        assertEquals(Map.of(1, firstDocument, 2, secondDocument), index.getPostings("java"));
    }

    @Test
    void mergesPositionsForSameTermAndDocument() {
        InvertedIndex index = new InvertedIndex();
        Posting first = new Posting(1, List.of(0));
        Posting later = new Posting(1, List.of(2));
        index.addPosting("java", first);

        index.addPosting("java", later);

        assertEquals(Map.of(1, new Posting(1, List.of(0, 2))), index.getPostings("java"));
    }

    @Test
    void replacesOldPostingsWhenDocumentIsReindexed() {
        InvertedIndex index = new InvertedIndex();
        index.addPosting("java", new Posting(1, List.of(0)));

        index.replaceDocument(1, Map.of("python", new Posting(1, List.of(0))));

        assertEquals(Map.of(), index.getPostings("java"));
        assertEquals(Map.of(1, new Posting(1, List.of(0))), index.getPostings("python"));
    }

    @Test
    void removesDocumentPostingsAcrossTermsAndKeepsOtherDocuments() {
        InvertedIndex index = new InvertedIndex();
        Posting remaining = new Posting(2, List.of(1));
        index.addPosting("java", new Posting(1, List.of(0)));
        index.addPosting("java", remaining);
        index.addPosting("python", new Posting(1, List.of(2)));

        index.removeDocument(1);

        assertEquals(Map.of(2, remaining), index.getPostings("java"));
        assertEquals(Map.of(), index.getPostings("python"));
    }

    @Test
    void calculatesDocumentFrequency() {
        InvertedIndex index = new InvertedIndex();

        // "java" appears twice in document 1
        index.addPosting("java", new Posting(1, List.of(0, 2)));

        // "java" also appears in document 2
        index.addPosting("java", new Posting(2, List.of(1)));

        // "python" appears only in document 3
        index.addPosting("python", new Posting(3, List.of(0)));

        // Document frequency counts distinct documents
        assertEquals(2, index.documentFrequency("java"));
        assertEquals(1, index.documentFrequency("python"));

        // Unknown terms have document frequency 0
        assertEquals(0, index.documentFrequency("rust"));

        // Repeated occurrences in the same document
        // must not increase document frequency
        index.addPosting("java", new Posting(1, List.of(4)));

        assertEquals(2, index.documentFrequency("java"));
    }
}
