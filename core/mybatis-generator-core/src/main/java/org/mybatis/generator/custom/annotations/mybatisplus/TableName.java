package org.mybatis.generator.custom.annotations.mybatisplus;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * mybatisplus的@TableName注解对象
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-27 13:14
 * @version 3.0
 */
public class TableName extends AbstractAnnotation {
    public static final String ANNOTATION_NAME = "@TableName";
    public static final String importClass = "com.baomidou.mybatisplus.annotation.TableName";
    private String value;
    private String schema;
    private boolean keepGlobalPrefix = false;
    private String resultMap;
    private boolean autoResultMap = false;

    private String[] excludeProperty;

    public static TableName create(String value) {
        return new TableName(value);
    }

    public TableName() {
        super();
        this.addImports(importClass);
    }

    public TableName(String value) {
        super();
        this.value = value;
        this.addImports(importClass);
    }

    @Override
    public String toAnnotation() {
        if (value != null) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        if (schema != null) {
            this.items.add(VStringUtil.format("schema = \"{0}\"", this.schema));
        }
        this.items.add(VStringUtil.format("keepGlobalPrefix = {0}", this.keepGlobalPrefix));
        if (resultMap != null) {
            this.items.add(VStringUtil.format("resultMap = \"{0}\"", this.resultMap));
        }
        this.items.add(VStringUtil.format("autoResultMap = {0}", this.autoResultMap));
        if (excludeProperty != null) {
            this.items.add(VStringUtil.format("excludeProperty = {0}", String.join( ",",excludeProperty)));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

    public String getValue() {
        return value;
    }

    public TableName setValue(String value) {
        this.value = value;
        return this;
    }

    public boolean isAutoResultMap() {
        return autoResultMap;
    }

    public TableName setAutoResultMap(boolean autoResultMap) {
        this.autoResultMap = autoResultMap;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public boolean isKeepGlobalPrefix() {
        return keepGlobalPrefix;
    }

    public void setKeepGlobalPrefix(boolean keepGlobalPrefix) {
        this.keepGlobalPrefix = keepGlobalPrefix;
    }

    public String getResultMap() {
        return resultMap;
    }

    public void setResultMap(String resultMap) {
        this.resultMap = resultMap;
    }

    public String[] getExcludeProperty() {
        return excludeProperty;
    }

    public void setExcludeProperty(String[] excludeProperty) {
        this.excludeProperty = excludeProperty;
    }
}
