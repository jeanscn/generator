package org.mybatis.generator.api;

import java.util.Properties;

import org.mybatis.generator.config.Context;

/**
 * This class is a convenient base class for implementing plugins.
 *
 * <p>This adapter does not implement the <code>validate</code> method - all plugins
 * must perform validation.
 *
 * @author Jeff Butler
 *
 */
public abstract class PluginAdapter implements Plugin {
    protected Context context;
    protected final Properties properties = new Properties();

    protected PluginAdapter() {}

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties.putAll(properties);
    }
}
