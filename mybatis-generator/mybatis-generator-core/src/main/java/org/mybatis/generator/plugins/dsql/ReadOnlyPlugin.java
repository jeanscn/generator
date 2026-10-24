package org.mybatis.generator.plugins.dsql;

import java.util.List;

import org.mybatis.generator.api.CompositePlugin;

/**
 * Disables delete, insert, delete, and update methods in the MyBatisDynamicSQLV2 runtime.
 *
 * @author Jeff Butler
 *
 */
public class ReadOnlyPlugin extends CompositePlugin {

    public ReadOnlyPlugin() {
        addPlugin(new DisableDeletePlugin());
        addPlugin(new DisableInsertPlugin());
        addPlugin(new DisableUpdatePlugin());
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}
