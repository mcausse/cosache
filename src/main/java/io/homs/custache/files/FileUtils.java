package io.homs.custache.files;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static String loadFromClasspath(String resourceName) throws URISyntaxException, IOException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        final URI uri = resource.toURI();
        final Path path = Paths.get(uri);
        final List<String> elements = Files.readAllLines(path);
        return String.join("\n", elements);
    }

    public static String classPathResourceToFullPath(String resourceName) throws URISyntaxException {
        return Path.of(ClassLoader.getSystemResource(resourceName).toURI()).toString();
//        return Path.of(Thread.currentThread().getContextClassLoader().getResource(resourceName).toURI()).toString();
    }

    public static String loadFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Stream<String> lines = Files.lines(path);
        String data = lines.collect(Collectors.joining("\n"));
        lines.close();
        return data;
    }
}
