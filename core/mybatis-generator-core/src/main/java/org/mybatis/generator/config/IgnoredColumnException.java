package org.mybatis.generator.config;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;

/**
 * @author cen_c
 */
public class IgnoredColumnException extends IgnoredColumn {

    public IgnoredColumnException(String columnName) {
        super(columnName);
    }

    @Override
    public void validate(List<String> errors, String tableName) {
        if (!stringHasValue(columnName)) {
            errors.add(getString("ValidationError.26",
                    tableName));
        }
    }
}
