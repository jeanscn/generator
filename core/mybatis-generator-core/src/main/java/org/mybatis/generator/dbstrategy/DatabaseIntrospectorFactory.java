package org.mybatis.generator.dbstrategy;

import org.mybatis.generator.dbstrategy.introspectorstrategy.DamengIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.MysqlIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.OracleIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.SqlServerIntrospectorStrategy;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author cen_c
 */
public class DatabaseIntrospectorFactory {
    public static DatabaseIntrospectorStrategy createIntrospectorStrategy(Connection conn) throws SQLException {
        String dbType = conn.getMetaData().getDatabaseProductName().toLowerCase();

        if (dbType.contains("mysql")) {
            return new MysqlIntrospectorStrategy();
        } else if (dbType.contains("sql server")) {
            return new SqlServerIntrospectorStrategy();
        } else if (dbType.contains("oracle")) {
            return new OracleIntrospectorStrategy();
        } else if (dbType.contains("dm dbms")) {
            return new DamengIntrospectorStrategy();
        } else {
            throw new SQLException("Unsupported database type: " + dbType);
        }
    }
}
