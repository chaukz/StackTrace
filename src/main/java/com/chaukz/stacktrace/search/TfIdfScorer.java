package com.chaukz.stacktrace.search;

import com.chaukz.stacktrace.indexing.InvertedIndex;
import com.chaukz.stacktrace.indexing.DocumentRegistry;

public class TfIdfScorer {

    private final InvertedIndex index;
    private final DocumentRegistry registry;

    public TfIdfScorer(InvertedIndex index, DocumentRegistry registry) {
        this.index = index;
        this.registry = registry;
    }

    public double inverseDocumentFrequency(String term) {
        int N = registry.documentCount();
        int df = index.documentFrequency(term);

        if(N == 0 || df == 0){
            return 0.0;
        }

        double idf = Math.log((double) N / df);

        return idf;
    }

    public double score(String term, int documentId) {
        double tf = index.termFrequency(term, documentId);
        double idf = inverseDocumentFrequency(term);

        return tf * idf;
    }
}
