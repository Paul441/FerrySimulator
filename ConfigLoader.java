package com.example.ferry;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;

public class ConfigLoader {
    private static final String FILE = "/config.yaml";
    public static Config load() {
        Yaml yaml = new Yaml();
        try (InputStream in = ConfigLoader.class.getResourceAsStream(FILE)) {
            if (in == null) throw new IllegalStateException("Missing " + FILE);
            return yaml.loadAs(in, Config.class);
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse config", e);
        }
    }
}