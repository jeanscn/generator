package org.mybatis.generator.dbstrategy.introspectorstrategy;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.dbstrategy.DatabaseIntrospectorStrategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cen_c
 */
public class SqlServerIntrospectorStrategy implements DatabaseIntrospectorStrategy {

    @Override
    public void getIndexDetails(Connection conn, FullyQualifiedTable table, Map<String, String> indexComments, Map<String, String> indexTypes) {

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
        ResultSet sqlServerResultSet;
        String sb = "SELECT  B.name AS NAME,convert(varchar(1000), C.VALUE) AS REMARKS " + "FROM sys.tables A INNER JOIN sys.columns B ON B.object_id = A.object_id " +
                "LEFT JOIN sys.extended_properties C ON C.major_id = B.object_id AND C.minor_id = B.column_id " +
                "WHERE A.name = ? ";
        PreparedStatement ps = conn.prepareStatement(sb);
        ps.setString(1, tableName);
        sqlServerResultSet = ps.executeQuery();
        while (sqlServerResultSet.next()) {
            String colName = sqlServerResultSet.getString(1);
            String colRemark = sqlServerResultSet.getString(2);
            remarks.put(colName, colRemark);
        }
        sqlServerResultSet.close();
        return remarks;
    }

    @Override
    public void updateTableRemark(AbstractIntrospectedTable abstractIntrospectedTable, Connection conn) throws SQLException {
        ResultSet sqlServerResultSet;
        String sql = "SELECT b.value from sysobjects a\n" +
                "left join sys.extended_properties b on a.id=b.major_id and b.minor_id=0\n" +
                "WHERE a.name = ? ";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, abstractIntrospectedTable.getTableConfiguration().getTableName());
        sqlServerResultSet = ps.executeQuery();
        while (sqlServerResultSet.next()) {
            String remark = sqlServerResultSet.getString(1);
            abstractIntrospectedTable.setRemarks(remark);
        }
        sqlServerResultSet.close();
    }
}
