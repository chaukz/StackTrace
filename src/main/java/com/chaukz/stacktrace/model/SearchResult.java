package com.chaukz.stacktrace.model;

import java.nio.file.Path;

public record SearchResult(int documentId, Path path, String title, double score) {}
