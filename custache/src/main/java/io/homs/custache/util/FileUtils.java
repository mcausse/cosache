package io.homs.custache.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    public static String loadFromClasspath(String resourceName) throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Resource not found: '" + resourceName + "'");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8).replace("\r", "");
        }
    }
}
