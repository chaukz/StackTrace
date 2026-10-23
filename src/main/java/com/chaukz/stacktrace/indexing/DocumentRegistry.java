package com.chaukz.stacktrace.indexing;

import com.chaukz.stacktrace.model.Document;

import java.util.HashMap;
import java.util.Map;

public class DocumentRegistry {
    private final Map<Integer, Document> documents = new HashMap<>();
}
