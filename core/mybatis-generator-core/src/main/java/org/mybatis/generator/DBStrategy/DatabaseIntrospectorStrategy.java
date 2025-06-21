package org.mybatis.generator.DBStrategy;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;

import java.sql.*;
import java.util.Map;

public interface DatabaseIntrospectorStrategy {
    void getIndexDetails(Connection conn, FullyQualifiedTable table, Map<String, String> indexComments, Map<String, String> indexTypes) throws SQLException;
    default String getDatabaseName(Connection conn, FullyQualifiedTable table) throws SQLException{
        String schemaName = table.getIntrospectedCatalog();
        if (schemaName == null) {
            schemaName = table.getIntrospectedSchema();

            if (schemaName == null) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT DATABASE()")) {
                    if (rs.next()) {
                        schemaName = rs.getString(1);
                    }
                }
            }
        }
        return schemaName;
    };
    Map<String, String> getIndexComments(Connection conn, FullyQualifiedTable table) throws SQLException;
    Map<String, String> getColumnRemarks(Connection conn, String tableName) throws SQLException;
    void updateTableRemark(IntrospectedTable introspectedTable, Connection conn) throws SQLException;
    default void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    // 根据索引类型代码返回类型名称
    default String getIndexTypeString(short type) {
        switch (type) {
            case DatabaseMetaData.tableIndexStatistic:
                return "STATISTIC";
            case DatabaseMetaData.tableIndexClustered:
                return "CLUSTERED";
            case DatabaseMetaData.tableIndexHashed:
                return "HASHED";
            case DatabaseMetaData.tableIndexOther:
                return "OTHER";
            default:
                return "UNKNOWN";
        }
    }
}
