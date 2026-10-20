package com.chaukz.stacktrace.model;

import java.util.List;

public record Posting (int documentId,List <Integer> positions) {

    public int frequency() {
        return positions.size();
    }

    public Posting {
        positions = List.copyOf(positions);

        if (documentId <= 0) {
            throw new IllegalArgumentException("Document id must be positive");
        }
        if (positions.isEmpty()) {
            throw new IllegalArgumentException("Positions must not be empty");
        }
        for (int position : positions) {
            if (position < 0) {
                throw new IllegalArgumentException("Position must not be negative");
            }
        }

    }

}
