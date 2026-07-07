package org.mybatis.generator.internal.db;

import com.vgosoft.core.db.model.metadata.Column;
import com.vgosoft.core.db.model.metadata.ForeignKey;
import com.vgosoft.core.db.model.metadata.Index;
import com.vgosoft.core.db.model.metadata.PrimaryKey;
import com.vgosoft.core.db.model.metadata.Table;
import com.vgosoft.core.db.query.Capability;
import com.vgosoft.core.db.query.DatabaseQuery;
import com.vgosoft.core.db.query.DatabaseQueryFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

class DatabaseQueryMetadataAdapter {
    private final DatabaseQuery databaseQuery;

    DatabaseQueryMetadataAdapter(Connection connection) {
        this.databaseQuery = DatabaseQueryFactory.create(connection);
    }

    boolean supports(Capability capability) {
        return databaseQuery.supports(capability);
    }

    List<? extends Column> getTableColumns(String table) {
        return databaseQuery.getTableColumns(table);
    }

    List<? extends PrimaryKey> getPrimaryKeys(String table) {
        return databaseQuery.getPrimaryKeys(table);
    }

    List<? extends ForeignKey> getForeignKeys(String table) {
        return databaseQuery.getForeignKeys(table);
    }

    List<? extends Index> getIndexes(String table) {
        return databaseQuery.getIndexes(table);
    }

    Optional<Table> findTable(String tableName) {
        for (Table table : databaseQuery.getTables()) {
            if (tableName != null && tableName.equalsIgnoreCase(table.getTableName())) {
                return Optional.of(table);
            }
        }
        return Optional.empty();
    }
}
