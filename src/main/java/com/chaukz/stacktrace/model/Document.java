package com.chaukz.stacktrace.model;

import java.nio.file.Path;
public record Document (int id, String title, Path path) {
}
