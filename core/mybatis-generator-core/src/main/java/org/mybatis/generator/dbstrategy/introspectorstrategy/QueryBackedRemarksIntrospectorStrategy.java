package org.mybatis.generator.dbstrategy.introspectorstrategy;

import com.vgosoft.core.db.model.metadata.Column;
import com.vgosoft.core.db.model.metadata.Table;
import com.vgosoft.core.db.query.DatabaseQuery;
import com.vgosoft.core.db.query.DatabaseQueryFactory;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.dbstrategy.DatabaseIntrospectorStrategy;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

abstract class QueryBackedRemarksIntrospectorStrategy implements DatabaseIntrospectorStrategy {
    @Override
    public Map<String, String> getColumnRemarks(Connection conn, String tableName) {
        if (!stringHasValue(tableName)) {
            return Collections.emptyMap();
        }

        try {
            List<? extends Column> columns = createDatabaseQuery(conn).getTableColumns(tableName);
            Map<String, String> remarks = new HashMap<>();
            for (Column column : columns) {
                if (stringHasValue(column.getColumnName()) && stringHasValue(column.getRemarks())) {
                    remarks.put(column.getColumnName(), column.getRemarks());
                }
            }
            return remarks;
        } catch (RuntimeException e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public void updateTableRemark(AbstractIntrospectedTable abstractIntrospectedTable, Connection conn) {
        String tableName = abstractIntrospectedTable.getFullyQualifiedTable().getIntrospectedTableName();
        if (!stringHasValue(tableName)) {
            return;
        }

        try {
            for (Table table : createDatabaseQuery(conn).getTables()) {
                if (sameIdentifier(tableName, table.getTableName()) && stringHasValue(table.getRemarks())) {
                    abstractIntrospectedTable.setRemarks(table.getRemarks());
                    return;
                }
            }
        } catch (RuntimeException e) {
            // fall back to JDBC metadata remark already loaded by DatabaseIntrospector
        }
    }

    protected DatabaseQuery createDatabaseQuery(Connection conn) {
        return DatabaseQueryFactory.create(conn);
    }

    protected boolean sameIdentifier(String source, String target) {
        return source != null && source.equalsIgnoreCase(target);
    }
}
