package org.justdoit.blog.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Properties;

public class DynamicProperties {

    private static final String PROPERTIES_FILE_PATH = "config/dynamic.properties";

    // Singleton
    private static class SingletonHelper {
        private static final DynamicProperties INSTANCE = new DynamicProperties();
    }

    public static DynamicProperties getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private DynamicProperties() {}

    public String getString(String key, String defaultValue) {
        Properties props = loadProperties();
        return props.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        Properties props = loadProperties();
        String val = props.getProperty(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            System.err.println("Invalid int for key " + key + ": " + val);
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Properties props = loadProperties();
        String val = props.getProperty(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(PROPERTIES_FILE_PATH))) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("Failed to load properties: " + e.getMessage());
        }
        return props;
    }
}

