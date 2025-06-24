package org.mybatis.generator.config;

import lombok.Getter;

import java.util.Properties;

@Getter
public abstract class PropertyHolder {
    private final Properties properties;

    protected PropertyHolder() {
        super();
        properties = new Properties();
    }

    public void addProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

}
