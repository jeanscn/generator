package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

import static org.mybatis.generator.internal.util.StringUtility.stringContainsSpace;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;

/**
 * @author cen_c
 */
@Setter
@Getter
public class ColumnOverride extends PropertyHolder {

    private final String columnName;

    private String javaProperty;

    private String jdbcType;

    private String javaType;

    private String typeHandler;

    private boolean isColumnNameDelimited;

    private String  headerAlign;

    private String  align;

    /**
     * If true, the column is a GENERATED ALWAYS column which means
     * that it should not be used in insert or update statements.
     */
    private boolean isGeneratedAlways;

    private boolean required;

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

    public boolean isColumnNameDelimited() {
        return isColumnNameDelimited;
    }

    public void setColumnNameDelimited(boolean isColumnNameDelimited) {
        this.isColumnNameDelimited = isColumnNameDelimited;
    }

    public void validate(List<String> errors, String tableName) {
        if (!stringHasValue(columnName)) {
            errors.add(getString("ValidationError.22",
                    tableName));
        }
    }

    public boolean isGeneratedAlways() {
        return isGeneratedAlways;
    }

    public void setGeneratedAlways(boolean isGeneratedAlways) {
        this.isGeneratedAlways = isGeneratedAlways;
    }

}
