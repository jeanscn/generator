package org.mybatis.generator.config;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;

import lombok.Getter;
import org.mybatis.generator.internal.db.DatabaseDialects;

/**
 * This class specifies that a key is auto-generated, either as an identity
 * column (post insert), or as some other query like a sequences (pre insert).
 *
 * @author Jeff Butler
 */
public class GeneratedKey {

    @Getter
    private final String column;

    @Getter
    private final String runtimeSqlStatement;

    @Getter
    private final boolean isIdentity;

    private final String type;

    public GeneratedKey(String column, String configuredSqlStatement,
            boolean isIdentity, String type) {
        super();
        this.column = column;
        this.type = type;
        this.isIdentity = isIdentity;

        DatabaseDialects dialect = DatabaseDialects
                .getDatabaseDialect(configuredSqlStatement);
        if (dialect == null) {
            this.runtimeSqlStatement = configuredSqlStatement;
        } else {
            this.runtimeSqlStatement = dialect.getIdentityRetrievalStatement();
        }
    }

    public String getMyBatis3Order() {
        return isIdentity ? "AFTER" : "BEFORE";
    }

    public void validate(List<String> errors, String tableName) {
        if (!stringHasValue(runtimeSqlStatement)) {
            errors.add(getString("ValidationError.7",
                    tableName));
        }

        if (stringHasValue(type)
                && !"pre".equals(type)
                && !"post".equals(type)) {
            errors.add(getString("ValidationError.15", tableName));
        }

        if ("pre".equals(type) && isIdentity) {
            errors.add(getString("ValidationError.23",
                    tableName));
        }

        if ("post".equals(type) && !isIdentity) {
            errors.add(getString("ValidationError.24",
                    tableName));
        }
    }

    public boolean isJdbcStandard() {
        return "JDBC".equals(runtimeSqlStatement);
    }
}
