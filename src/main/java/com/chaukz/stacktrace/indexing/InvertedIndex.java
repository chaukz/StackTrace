package com.chaukz.stacktrace.indexing;

import com.chaukz.stacktrace.model.Posting;

import java.util.Hashtable;
import java.util.Map;

public class InvertedIndex {
    Map<String, Map<Integer, Posting>> index = new Hashtable<>();

    public void addPosting(String term, Posting posting) {
        index.computeIfAbsent(term, k -> new Hashtable<>()).putIfAbsent(posting.documentId(), posting);
    }

    public Map<Integer, Posting> getPostings(String term) {
        return index.getOrDefault(term, Map.of());
    }
}

