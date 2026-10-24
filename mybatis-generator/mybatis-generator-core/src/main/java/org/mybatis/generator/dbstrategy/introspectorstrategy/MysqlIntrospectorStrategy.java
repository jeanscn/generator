package org.mybatis.generator.dbstrategy.introspectorstrategy;

import org.mybatis.generator.api.FullyQualifiedTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cen_c
 */
public class MysqlIntrospectorStrategy extends QueryBackedRemarksIntrospectorStrategy {
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

}
