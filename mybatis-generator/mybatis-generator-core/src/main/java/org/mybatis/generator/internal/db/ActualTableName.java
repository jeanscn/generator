package org.mybatis.generator.internal.db;

import lombok.Getter;

import static org.mybatis.generator.internal.util.StringUtility.composeFullyQualifiedTableName;

/**
 * This class holds the actual catalog, schema, and table name returned from the
 * database introspection.
 *
 * @author Jeff Butler
 *
 */
public class ActualTableName {

    @Getter
    private final String tableName;
    @Getter
    private final String catalog;
    @Getter
    private final String schema;
    private final String fullName;

    public ActualTableName(String catalog, String schema, String tableName) {
        this.catalog = catalog;
        this.schema = schema;
        this.tableName = tableName;
        fullName = composeFullyQualifiedTableName(catalog,
                schema, tableName, '.');
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ActualTableName)) {
            return false;
        }

        return obj.toString().equals(this.toString());
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

    @Override
    public String toString() {
        return fullName;
    }
}
