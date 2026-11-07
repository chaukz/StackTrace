package com.chaukz.stacktrace.indexing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvertedIndexLengthTest {

    private InvertedIndex index;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
    }

    @Test
    void storesDocumentLength() {
        index.setDocumentLength(1, 100);

        assertEquals(100, index.getDocumentLength(1));
    }

    @Test
    void returnsZeroForUnknownDocument() {
        assertEquals(0, index.getDocumentLength(999));
    }

    @Test
    void updatesExistingDocumentLength() {
        index.setDocumentLength(1, 100);
        index.setDocumentLength(1, 250);

        assertEquals(250, index.getDocumentLength(1));
    }

    @Test
    void storesLengthsForMultipleDocuments() {
        index.setDocumentLength(1, 100);
        index.setDocumentLength(2, 200);
        index.setDocumentLength(3, 50);

        assertEquals(100, index.getDocumentLength(1));
        assertEquals(200, index.getDocumentLength(2));
        assertEquals(50, index.getDocumentLength(3));
    }

    @Test
    void supportsEmptyDocuments() {
        index.setDocumentLength(1, 0);

        assertEquals(0, index.getDocumentLength(1));
    }
}
