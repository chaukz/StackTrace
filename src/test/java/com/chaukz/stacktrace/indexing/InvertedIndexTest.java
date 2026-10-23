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
    void ignoresLaterPostingForSameTermAndDocument() {
        InvertedIndex index = new InvertedIndex();
        Posting first = new Posting(1, List.of(0));
        Posting later = new Posting(1, List.of(2));
        index.addPosting("java", first);

        index.addPosting("java", later);

        assertEquals(Map.of(1, first), index.getPostings("java"));
    }
}
