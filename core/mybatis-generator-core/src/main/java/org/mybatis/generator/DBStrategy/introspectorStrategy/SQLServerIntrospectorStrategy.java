package org.mybatis.generator.DBStrategy.introspectorStrategy;

import org.mybatis.generator.DBStrategy.DatabaseIntrospectorStrategy;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SQLServerIntrospectorStrategy implements DatabaseIntrospectorStrategy {

    @Override
    public void getIndexDetails(Connection conn, FullyQualifiedTable table, Map<String, String> indexComments, Map<String, String> indexTypes) throws SQLException {

    }

    @Override
    public Map<String, String> getIndexComments(Connection conn, FullyQualifiedTable table) throws SQLException {
        Map<String, String> comments = new HashMap<>();
        String sql = "SELECT i.name as INDEX_NAME, ep.value as INDEX_COMMENT " +
                "FROM sys.indexes i " +
                "LEFT JOIN sys.extended_properties ep ON ep.major_id = i.object_id AND ep.minor_id = i.index_id " +
                "AND ep.class = 7 " +
                "JOIN sys.tables t ON i.object_id = t.object_id " +
                "WHERE t.name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table.getIntrospectedTableName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.put(rs.getString("INDEX_NAME"), rs.getString("INDEX_COMMENT"));
                }
            }
        }
        return comments;
    }

    @Override
    public Map<String, String> getColumnRemarks(Connection conn, String tableName) throws SQLException {
        Map<String, String> remarks = new HashMap<>();
        ResultSet sqlServerResultSet = null;
        StringBuilder sb = new StringBuilder("SELECT  B.name AS NAME,convert(varchar(1000), C.VALUE) AS REMARKS ");
        sb.append("FROM sys.tables A INNER JOIN sys.columns B ON B.object_id = A.object_id ");
        sb.append("LEFT JOIN sys.extended_properties C ON C.major_id = B.object_id AND C.minor_id = B.column_id ");
        sb.append("WHERE A.name = ? ");
        PreparedStatement ps = conn.prepareStatement(sb.toString());
        ps.setString(1, tableName);
        sqlServerResultSet = ps.executeQuery();
        while (sqlServerResultSet.next()) {
            String col_Name = sqlServerResultSet.getString(1);
            String col_Remark = sqlServerResultSet.getString(2);
            remarks.put(col_Name, col_Remark);
        }
        sqlServerResultSet.close();
        return remarks;
    }

    @Override
    public void updateTableRemark(IntrospectedTable introspectedTable, Connection conn) throws SQLException {
        ResultSet sqlServerResultSet = null;
        String sql = "SELECT b.value from sysobjects a\n" +
                "left join sys.extended_properties b on a.id=b.major_id and b.minor_id=0\n" +
                "WHERE a.name = ? ";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, introspectedTable.getTableConfiguration().getTableName());
        sqlServerResultSet = ps.executeQuery();
        while (sqlServerResultSet.next()) {
            String remark = sqlServerResultSet.getString(1);
            introspectedTable.setRemarks(remark);
        }
        sqlServerResultSet.close();
    }
}
