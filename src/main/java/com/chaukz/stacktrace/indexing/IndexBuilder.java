package com.chaukz.stacktrace.indexing;

import com.chaukz.stacktrace.analysis.Tokenizer;
import com.chaukz.stacktrace.io.DocumentDiscovery;
import com.chaukz.stacktrace.model.Document;
import com.chaukz.stacktrace.model.Token;
import com.chaukz.stacktrace.model.Posting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexBuilder {

    Tokenizer tokenizer = new Tokenizer();
    InvertedIndex index = new InvertedIndex();
    DocumentRegistry registry = new DocumentRegistry();


    public IndexBuilder() {}

    public void indexDocument(Document document) throws IOException {
        String text = Files.readString(document.path(), StandardCharsets.UTF_8);
        List<Token> tokens = tokenizer.tokenize(text);
        Map<String, List<Integer>> positonsByTerm = new HashMap<>();
        for (Token token : tokens) {
            positonsByTerm.computeIfAbsent(token
                    .term(), term -> new ArrayList<>())
                    .add(token.position());
        }
    Map<String, Posting> postings = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : positonsByTerm.entrySet()) {
            postings.put(entry.getKey(), new Posting(document.id(), entry.getValue()));
        }
        registry.add(document);
        index.replaceDocument(document.id(), postings);

    }

    public void indexDirectory(Path directory) throws IOException {
        for (Document document : new DocumentDiscovery().discover(directory)) {
            indexDocument(document);
        }
    }


}
