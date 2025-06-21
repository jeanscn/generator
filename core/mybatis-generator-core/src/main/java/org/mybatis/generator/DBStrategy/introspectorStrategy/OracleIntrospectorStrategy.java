package org.mybatis.generator.DBStrategy.introspectorStrategy;

import org.mybatis.generator.DBStrategy.DatabaseIntrospectorStrategy;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OracleIntrospectorStrategy implements DatabaseIntrospectorStrategy {
    @Override
    public void getIndexDetails(Connection conn, FullyQualifiedTable table, Map<String, String> indexComments, Map<String, String> indexTypes) throws SQLException {

    }

    @Override
    public Map<String, String> getIndexComments(Connection conn, FullyQualifiedTable table) throws SQLException {
        Map<String, String> comments = new HashMap<>();
        String schema = table.getIntrospectedSchema();
        if (schema == null) {
            // 获取当前用户作为默认schema
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT USER FROM DUAL")) {
                if (rs.next()) {
                    schema = rs.getString(1);
                }
            }
        }
        String sql = "SELECT INDEX_NAME, COMMENTS FROM ALL_IND_COMMENTS " +
                "WHERE OWNER = ? AND TABLE_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, table.getIntrospectedTableName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.put(rs.getString("INDEX_NAME"), rs.getString("COMMENTS"));
                }
            }
        }
        return comments;
    }

    @Override
    public Map<String, String> getColumnRemarks(Connection conn, String tableName) throws SQLException {
        return Collections.emptyMap();
    }

    @Override
    public void updateTableRemark(IntrospectedTable introspectedTable, Connection conn) throws SQLException {

    }
}
