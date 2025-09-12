package org.mybatis.generator.plugins;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinType;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * This plugin adds a CacheNamespace annotation to generated Java or Kotlin mapper interfaces.
 * The plugin accepts the following properties (all are optional):
 *
 * <ul>
 *   <li>cache_blocking</li>
 *   <li>cache_flushInterval</li>
 *   <li>cache_readWrite</li>
 *   <li>cache_size</li>
 *   <li>cache_implementation</li>
 *   <li>cache_eviction</li>
 *   <li>cache_skip</li>
 * </ul>
 *
 * <p>All properties (except cache_skip) correspond to properties of the MyBatis CacheNamespace annotation.
 * Most properties are passed "as is" to the corresponding properties of the generated
 * annotation.  The properties "cache_implementation" and "cache_eviction" must be fully qualified class names.
 * If specified, the values
 * will be added to the import list of the mapper file, and the short names will be used in the generated annotation.
 * All properties can be specified at the table level, or on the
 * plugin element.  The property on the table element will override any
 * property on the plugin element.
 *
 * <p>If the "cache_skip" property is set to "true" - either on the plugin or on a specific table,
 * the annotation will not be applied to the generated interface.
 *
 * @author Jeff Butler
 */
public class CacheNamespacePlugin extends PluginAdapter {

    public enum CacheProperty {
        BLOCKING("cache_blocking", "blocking", false),
        FLUSH_INTERVAL("cache_flushInterval", "flushInterval", false),
        READ_WRITE("cache_readWrite", "readWrite", false),
        SIZE("cache_size", "size", false),
        IMPLEMENTATION("cache_implementation", "implementation", true),
        EVICTION("cache_eviction", "eviction", true);

        private final String propertyName;
        private final String attributeName;
        private final boolean isClassName;

        CacheProperty(String propertyName, String attributeName, boolean isClassName) {
            this.propertyName = propertyName;
            this.attributeName = attributeName;
            this.isClassName = isClassName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public boolean isClassName() {
            return isClassName;
        }
    }

    @Override
    public boolean validate(List<String> arg0) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, AbstractIntrospectedTable abstractIntrospectedTable) {
        if (!skip(abstractIntrospectedTable)) {
            interfaze.addImportedType(
                    new FullyQualifiedJavaType("org.apache.ibatis.annotations.CacheNamespace"));

            Arrays.stream(CacheProperty.values())
                    .filter(CacheProperty::isClassName)
                    .map(cp -> getRawPropertyValue(abstractIntrospectedTable, cp.getPropertyName()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(FullyQualifiedJavaType::new)
                    .forEach(interfaze::addImportedType);

            interfaze.addAnnotation(calculateAnnotation(abstractIntrospectedTable, ".class"));
        }

        return true;
    }

    @Override
    public boolean mapperGenerated(KotlinFile mapperFile, KotlinType mapper, AbstractIntrospectedTable abstractIntrospectedTable) {
        if (!skip(abstractIntrospectedTable)) {
            mapperFile.addImport("org.apache.ibatis.annotations.CacheNamespace");

            Arrays.stream(CacheProperty.values())
                    .filter(CacheProperty::isClassName)
                    .map(cp -> getRawPropertyValue(abstractIntrospectedTable, cp.getPropertyName()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(mapperFile::addImport);

            mapper.addAnnotation(calculateAnnotation(abstractIntrospectedTable, "::class"));
        }

        return true;
    }

    private boolean skip(AbstractIntrospectedTable abstractIntrospectedTable) {
        return getRawPropertyValue(abstractIntrospectedTable, "cache_skip")
                .map("true"::equalsIgnoreCase)
                .orElse(false);
    }

    private String calculateAnnotation(AbstractIntrospectedTable abstractIntrospectedTable, String classAccessor) {
        String attributes = Arrays.stream(CacheProperty.values())
                .map(cp -> calculateAttribute(abstractIntrospectedTable, cp, classAccessor))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining(", "));

        if (StringUtility.stringHasValue(attributes)) {
            return "@CacheNamespace(" + attributes + ")";
        } else {
            return "@CacheNamespace";
        }
    }

    private Optional<String> calculateAttribute(AbstractIntrospectedTable abstractIntrospectedTable,
                                                CacheProperty cacheProperty,
                                                String classAccessor) {
        return getPropertyValueForAttribute(abstractIntrospectedTable, cacheProperty, classAccessor)
                .map(v -> String.format("%s = %s", cacheProperty.getAttributeName(), v));
    }

    private Optional<String> getPropertyValueForAttribute(AbstractIntrospectedTable abstractIntrospectedTable,
                                                          CacheProperty cacheProperty,
                                                          String classAccessor) {
        Optional<String> value = getRawPropertyValue(abstractIntrospectedTable, cacheProperty.getPropertyName());

        if (cacheProperty.isClassName()) {
            value = value.map(FullyQualifiedJavaType::new)
                    .map(FullyQualifiedJavaType::getShortName)
                    .map(s -> s + classAccessor);
        }

        return value;
    }

    private Optional<String> getRawPropertyValue(AbstractIntrospectedTable abstractIntrospectedTable, String propertyName) {
        String value = abstractIntrospectedTable.getTableConfigurationProperty(propertyName);
        if (value == null) {
            value = properties.getProperty(propertyName);
        }

        if (StringUtility.stringHasValue(value)) {
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }
}
