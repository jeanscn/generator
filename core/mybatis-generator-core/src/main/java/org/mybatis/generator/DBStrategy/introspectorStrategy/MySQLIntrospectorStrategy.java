package org.mybatis.generator.DBStrategy.introspectorStrategy;

import org.mybatis.generator.DBStrategy.DatabaseIntrospectorStrategy;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MySQLIntrospectorStrategy implements DatabaseIntrospectorStrategy {
    @Override
    public void getIndexDetails(Connection conn, FullyQualifiedTable table,
                                Map<String, String> indexComments,
                                Map<String, String> indexTypes) throws SQLException {
        String sql = "SELECT INDEX_NAME, MAX(INDEX_COMMENT) AS INDEX_COMMENT, MAX(INDEX_TYPE) AS INDEX_TYPE " +
                "FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                "AND INDEX_NAME != 'PRIMARY' " +
                "GROUP BY INDEX_NAME";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, getDatabaseName(conn, table));
            ps.setString(2, table.getIntrospectedTableName());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    indexComments.put(indexName, rs.getString("INDEX_COMMENT"));
                    indexTypes.put(indexName, rs.getString("INDEX_TYPE"));
                }
            }
        }
    }

    @Override
    public Map<String, String> getIndexComments(Connection conn, FullyQualifiedTable table) throws SQLException {
        Map<String, String> indexComments = new HashMap<>();
        String sql = "SELECT INDEX_NAME, MAX(INDEX_COMMENT) AS INDEX_COMMENT, MAX(INDEX_TYPE) AS INDEX_TYPE " +
                "FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                "AND INDEX_NAME != 'PRIMARY' " +
                "GROUP BY INDEX_NAME";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, getDatabaseName(conn, table));
            ps.setString(2, table.getIntrospectedTableName());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    indexComments.put(indexName, rs.getString("INDEX_COMMENT"));
                }
            }
        }
        return indexComments;
    }

    @Override
    public Map<String, String> getColumnRemarks(Connection conn, String tableName) throws SQLException {
        return Collections.emptyMap();
    }

    @Override
    public void updateTableRemark(IntrospectedTable introspectedTable, Connection conn) throws SQLException {

    }
}
