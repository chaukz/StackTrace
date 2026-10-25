package com.chaukz.stacktrace.indexing;

import com.chaukz.stacktrace.model.Posting;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class InvertedIndex {
    Map<String, Map<Integer, Posting>> index = new Hashtable<>();

    public void addPosting(String term, Posting posting) {
        Map<Integer, Posting> postings = index.computeIfAbsent(term, k -> new Hashtable<>());
        Posting existing = postings.get(posting.documentId());
        if (existing == null) {
            postings.put(posting.documentId(), posting);
            return;
        }

        List<Integer> positions = Stream.concat(
                existing.positions().stream(),
                posting.positions().stream().filter(position -> !existing.positions().contains(position))
        ).toList();
        postings.put(posting.documentId(), new Posting(posting.documentId(), positions));
    }

    public void removePosting(String term, int documentId) {
        index.get(term).remove(documentId);
    }

    public void updatePosting(String term, int documentId, List<Integer> positions) {
        index.get(term).put(documentId, new Posting(documentId, positions));
    }

    public void removeDocument(int documentId) {
        Iterator<Map.Entry<String, Map<Integer, Posting>>> terms = index.entrySet().iterator();
        while (terms.hasNext()) {
            Map<Integer, Posting> postings = terms.next().getValue();
            postings.remove(documentId);
            if (postings.isEmpty()) {
                terms.remove();
            }
        }
    }

    public void replaceDocument(int documentId, Map<String, Posting> postings) {
        if (documentId <= 0) {
            throw new IllegalArgumentException("Document id must be positive");
        }
        Map<String, Posting> replacements = Map.copyOf(postings);
        for (Map.Entry<String, Posting> entry : replacements.entrySet()) {
            if (entry.getKey().isBlank() || entry.getValue().documentId() != documentId) {
                throw new IllegalArgumentException("Replacement postings must have a term and match the document id");
            }
        }

        removeDocument(documentId);
        replacements.forEach(this::addPosting);
    }

    public Map<Integer, Posting> getPostings(String term) {
        return index.getOrDefault(term, Map.of());
    }
}

