package org.mybatis.generator.plugins;

import java.util.List;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * This plugin adds a cache element to generated sqlMaps.  This plugin
 * is for MyBatis3 targeted runtimes only.  The plugin accepts the
 * following properties (all are optional):
 *
 * <ul>
 *   <li>cache_eviction</li>
 *   <li>cache_flushInterval</li>
 *   <li>cache_size</li>
 *   <li>cache_readOnly</li>
 *   <li>cache_type</li>
 * </ul>
 *
 * <p>All properties correspond to properties of the MyBatis cache element and
 * are passed "as is" to the corresponding properties of the generated cache
 * element.  All properties can be specified at the table level, or on the
 * plugin element.  The property on the table element will override any
 * property on the plugin element.
 *
 * @author Jason Bennett
 * @author Jeff Butler
 */
public class CachePlugin extends PluginAdapter {
    public enum CacheProperty {
        EVICTION("cache_eviction", "eviction"),
        FLUSH_INTERVAL("cache_flushInterval", "flushInterval"),
        READ_ONLY("cache_readOnly", "readOnly"),
        SIZE("cache_size", "size"),
        TYPE("cache_type", "type");

        private final String propertyName;
        private final String attributeName;

        CacheProperty(String propertyName, String attributeName) {
            this.propertyName = propertyName;
            this.attributeName = attributeName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }

    public CachePlugin() {
        super();
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, AbstractIntrospectedTable abstractIntrospectedTable) {

        XmlElement element = new XmlElement("cache");
        context.getCommentGenerator().addComment(element);

        for (CacheProperty cacheProperty : CacheProperty.values()) {
            addAttributeIfExists(element, abstractIntrospectedTable, cacheProperty);
        }

        document.getRootElement().addElement(element);

        return true;
    }

    private void addAttributeIfExists(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable,
            CacheProperty cacheProperty) {
        String property = abstractIntrospectedTable.getTableConfigurationProperty(cacheProperty.getPropertyName());
        if (property == null) {
            property = properties.getProperty(cacheProperty.getPropertyName());
        }

        if (StringUtility.stringHasValue(property)) {
            element.addAttribute(new Attribute(cacheProperty.getAttributeName(), property));
        }
    }
}
