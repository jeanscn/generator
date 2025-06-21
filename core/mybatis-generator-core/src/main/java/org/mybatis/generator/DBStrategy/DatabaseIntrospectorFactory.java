package org.mybatis.generator.DBStrategy;

import org.mybatis.generator.DBStrategy.introspectorStrategy.MySQLIntrospectorStrategy;
import org.mybatis.generator.DBStrategy.introspectorStrategy.OracleIntrospectorStrategy;
import org.mybatis.generator.DBStrategy.introspectorStrategy.SQLServerIntrospectorStrategy;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseIntrospectorFactory {
    public static DatabaseIntrospectorStrategy createIntrospectorStrategy(Connection conn) throws SQLException {
        String dbType = conn.getMetaData().getDatabaseProductName().toLowerCase();

        if (dbType.contains("mysql")) {
            return new MySQLIntrospectorStrategy();
        } else if (dbType.contains("sql server")) {
            return new SQLServerIntrospectorStrategy();
        } else if (dbType.contains("oracle")) {
            return new OracleIntrospectorStrategy();
        } else {
            throw new SQLException("Unsupported database type: " + dbType);
        }
    }
}
