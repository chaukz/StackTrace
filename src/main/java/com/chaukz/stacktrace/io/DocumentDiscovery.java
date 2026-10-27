package com.chaukz.stacktrace.io;

import com.chaukz.stacktrace.model.Document;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DocumentDiscovery {
    public List<Document> discover(Path directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("Directory must not be null");
        }

        Path root = directory.toAbsolutePath().normalize();
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Not a directory: " + root);
        }

        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> files = paths
                    .filter(path -> !path.equals(root))
                    .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .filter(path -> !Files.isSymbolicLink(path))
                    .filter(DocumentDiscovery::isSupported)
                    .sorted(Comparator.comparing(path -> root.relativize(path).toString()))
                    .toList();
            return IntStream.range(0, files.size())
                    .mapToObj(i -> new Document(i + 1, files.get(i).getFileName().toString(), files.get(i)))
                    .toList();
        } catch (UncheckedIOException exception) {
            throw exception.getCause();
        }
    }

    private static boolean isSupported(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".txt") || name.endsWith(".md");
    }
}
