package org.delcom.ifs25001pbo2026springbootcashflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Memuat file .env ke system properties sebelum Spring Boot start. */
public final class DotEnv {
    private DotEnv() {}

    public static void load() throws IOException {
        load(Path.of(".env"));
    }

    static void load(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        for (String raw : Files.readAllLines(path)) {
            String line = raw.trim();
            int eq = line.indexOf('=');
            if (line.isEmpty() || line.startsWith("#") || eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
                    || value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }
            if (System.getProperty(key) == null && System.getenv(key) == null) {
                System.setProperty(key, value);
            }
        }
    }
}
