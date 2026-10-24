package org.mybatis.generator.dbstrategy;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.FullyQualifiedTable;

import java.sql.*;
import java.util.Map;

/**
 * @author cen_c
 */
public interface DatabaseIntrospectorStrategy {
    /**
     * 获取索引列详情
     *
     * @param conn          连接
     * @param table         表对象
     * @param indexComments 列注释
     * @param indexTypes    索引列类型
     * @throws SQLException sql异常
     */
    void getIndexDetails(Connection conn, FullyQualifiedTable table, Map<String, String> indexComments, Map<String, String> indexTypes) throws SQLException;

    /**
     * 获取数据库名
     *
     * @param conn  连接
     * @param table 表对象
     * @return 数据库名
     * @throws SQLException sql异常
     */
    default String getDatabaseName(Connection conn, FullyQualifiedTable table) throws SQLException {
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
    }

    ;

    /**
     * 获取索引列注释
     *
     * @param conn  连接
     * @param table 表对象
     * @return 索引列注释
     * @throws SQLException sql异常
     */
    Map<String, String> getIndexComments(Connection conn, FullyQualifiedTable table) throws SQLException;

    /**
     * 获取索引列类型
     *
     * @param conn      连接
     * @param tableName 表名
     * @return 索引列类型
     * @throws SQLException sql异常
     */
    Map<String, String> getColumnRemarks(Connection conn, String tableName) throws SQLException;

    /**
     * 更新表注释
     *
     * @param abstractIntrospectedTable 表对象
     * @param conn                      连接
     * @throws SQLException sql异常
     */
    void updateTableRemark(AbstractIntrospectedTable abstractIntrospectedTable, Connection conn) throws SQLException;

    /**
     * 关闭ResultSet
     *
     * @param rs ResultSet对象
     */
    default void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * 根据索引类型代码返回类型名称
     *
     * @param type 索引类型代码
     * @return 索引类型名称
     */
    default String getIndexTypeString(short type) {
        return switch (type) {
            case DatabaseMetaData.tableIndexStatistic -> "STATISTIC";
            case DatabaseMetaData.tableIndexClustered -> "CLUSTERED";
            case DatabaseMetaData.tableIndexHashed -> "HASHED";
            case DatabaseMetaData.tableIndexOther -> "OTHER";
            default -> "UNKNOWN";
        };
    }
}
