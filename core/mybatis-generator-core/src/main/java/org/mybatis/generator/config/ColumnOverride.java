package org.mybatis.generator.config;

import static org.mybatis.generator.internal.util.StringUtility.stringContainsSpace;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;

public class ColumnOverride extends PropertyHolder {

    private final String columnName;

    private String javaProperty;

    private String jdbcType;

    private String javaType;

    private String typeHandler;

    private boolean isColumnNameDelimited;

    /**
     * If true, the column is a GENERATED ALWAYS column which means
     * that it should not be used in insert or update statements.
     */
    private boolean isGeneratedAlways;

    private Integer maxLength;
    private Integer minLength;

    private Integer scale;

    private String columnComment;

    private String columnSubComment;

    public ColumnOverride(String columnName) {
        super();

        this.columnName = columnName;
        isColumnNameDelimited = stringContainsSpace(columnName);
    }

    public String getColumnName() {
        return columnName;
    }

    public String getJavaProperty() {
        return javaProperty;
    }

    public void setJavaProperty(String javaProperty) {
        this.javaProperty = javaProperty;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }

    public String getTypeHandler() {
        return typeHandler;
    }

    public void setTypeHandler(String typeHandler) {
        this.typeHandler = typeHandler;
    }

    public boolean isColumnNameDelimited() {
        return isColumnNameDelimited;
    }

    public void setColumnNameDelimited(boolean isColumnNameDelimited) {
        this.isColumnNameDelimited = isColumnNameDelimited;
    }

    public void validate(List<String> errors, String tableName) {
        if (!stringHasValue(columnName)) {
            errors.add(getString("ValidationError.22", //$NON-NLS-1$
                    tableName));
        }
    }

    public boolean isGeneratedAlways() {
        return isGeneratedAlways;
    }

    public void setGeneratedAlways(boolean isGeneratedAlways) {
        this.isGeneratedAlways = isGeneratedAlways;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getColumnSubComment() {
        return columnSubComment;
    }

    public void setColumnSubComment(String columnSubComment) {
        this.columnSubComment = columnSubComment;
    }
}
