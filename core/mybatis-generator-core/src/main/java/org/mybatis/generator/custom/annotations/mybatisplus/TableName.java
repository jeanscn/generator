package org.mybatis.generator.custom.annotations.mybatisplus;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * mybatisplus的@TableName注解对象
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-27 13:14
 * @version 3.0
 */
@Getter
@Setter
public class TableName extends AbstractAnnotation {
    public static final String ANNOTATION_NAME = "@TableName";
    public static final String TABLE_NAME = "com.baomidou.mybatisplus.annotation.TableName";
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
        this.addImports(TABLE_NAME);
    }

    public TableName(String value) {
        super();
        this.value = value;
        this.addImports(TABLE_NAME);
    }

    @Override
    public String toAnnotation() {
        if (value != null) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        if (schema != null) {
            this.items.add(VStringUtil.format("schema = \"{0}\"", this.schema));
        }
        if (this.keepGlobalPrefix) {
            this.items.add("keepGlobalPrefix = true");
        }
        if (resultMap != null) {
            this.items.add(VStringUtil.format("resultMap = \"{0}\"", this.resultMap));
        }
        if (this.autoResultMap) {
            this.items.add("autoResultMap = true");
        }
        if (excludeProperty != null) {
            this.items.add(VStringUtil.format("excludeProperty = {0}", String.join( ",",excludeProperty)));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

    public TableName setValue(String value) {
        this.value = value;
        return this;
    }

    public TableName setAutoResultMap(boolean autoResultMap) {
        this.autoResultMap = autoResultMap;
        return this;
    }
}
