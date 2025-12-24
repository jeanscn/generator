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
public class IgnoredColumn {

    @Getter
    protected final String columnName;

    @Setter
    private boolean isColumnNameDelimited;

    public IgnoredColumn(String columnName) {
        super();
        this.columnName = columnName;
        isColumnNameDelimited = stringContainsSpace(columnName);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IgnoredColumn)) {
            return false;
        }

        return columnName.equals(((IgnoredColumn) obj).getColumnName());
    }

    @Override
    public int hashCode() {
        return columnName.hashCode();
    }

    public void validate(List<String> errors, String tableName) {
        if (!stringHasValue(columnName)) {
            errors.add(getString("ValidationError.21",
                    tableName));
        }
    }

    public boolean matches(String columnName) {
        if (isColumnNameDelimited) {
            return this.columnName.equals(columnName);
        } else {
            return this.columnName.equalsIgnoreCase(columnName);
        }
    }
}
