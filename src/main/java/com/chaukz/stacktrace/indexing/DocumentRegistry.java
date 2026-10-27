package com.chaukz.stacktrace.indexing;

import com.chaukz.stacktrace.model.Document;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DocumentRegistry {
    private final Map<Integer, Document> documents = new HashMap<>();
    private final Map<Path, Integer> documentsByPath = new HashMap<>();

    public void add(Document document) {
      if (document == null) {
          throw new IllegalArgumentException("Document must not be null");
      }
        if(documents.containsKey(Integer.valueOf(document.id()))) {
            throw new IllegalArgumentException("Document with id " + document.id() + " already exists");
        }
        Path path = document.path().toAbsolutePath().normalize();
        if (documentsByPath.containsKey(path)) {
            throw new IllegalArgumentException("Document path " + path + " already exists");
        }
        documents.put(document.id(), document);
        documentsByPath.put(path, document.id());
    }
    public Document get(int id) {
        return documents.get(id);
    }
    public boolean contains(int id) {
        return documents.containsKey(id);
    }

}
