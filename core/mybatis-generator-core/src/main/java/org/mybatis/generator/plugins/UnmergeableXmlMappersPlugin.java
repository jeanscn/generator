package org.mybatis.generator.plugins;

import java.util.List;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

/**
 * This plugin marks generated XML mapper files as unmergeable.  This will cause the generator to either
 * overwrite the files, or save the files under a new name depending on how the overwrite setting is configured.
 *
 * <p>This can be useful when comments are disabled so the normal XML merge won't work.
 *
 * @author Jeff Butler
 *
 */
public class UnmergeableXmlMappersPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap,
            IntrospectedTable introspectedTable) {
        sqlMap.setMergeable(false);
        return true;
    }
}
