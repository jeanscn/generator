package org.mybatis.generator.dbstrategy;

import org.mybatis.generator.dbstrategy.introspectorstrategy.MySQLIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.OracleIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.SqlServerIntrospectorStrategy;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseIntrospectorFactory {
    public static DatabaseIntrospectorStrategy createIntrospectorStrategy(Connection conn) throws SQLException {
        String dbType = conn.getMetaData().getDatabaseProductName().toLowerCase();

        if (dbType.contains("mysql")) {
            return new MySQLIntrospectorStrategy();
        } else if (dbType.contains("sql server")) {
            return new SqlServerIntrospectorStrategy();
        } else if (dbType.contains("oracle")) {
            return new OracleIntrospectorStrategy();
        } else {
            throw new SQLException("Unsupported database type: " + dbType);
        }
    }
}
