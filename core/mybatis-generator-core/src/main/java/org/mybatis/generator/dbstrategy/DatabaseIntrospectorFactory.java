package org.mybatis.generator.dbstrategy;

import com.vgosoft.core.db.query.DatabaseType;
import com.vgosoft.core.db.util.JdbcUtils;
import org.mybatis.generator.dbstrategy.introspectorstrategy.DamengIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.GenericIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.MysqlIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.OracleIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.PostgreSqlIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.SqlServerIntrospectorStrategy;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author cen_c
 */
public class DatabaseIntrospectorFactory {
    private static final DatabaseIntrospectorStrategy GENERIC_STRATEGY = new GenericIntrospectorStrategy();

    private static final Map<DatabaseType, DatabaseIntrospectorStrategy> STRATEGIES = createStrategies();

    public static DatabaseIntrospectorStrategy createIntrospectorStrategy(Connection conn) throws SQLException {
        DatabaseType databaseType = resolveDatabaseType(conn);
        return STRATEGIES.getOrDefault(databaseType, GENERIC_STRATEGY);
    }

    static DatabaseType resolveDatabaseType(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String productName = metaData.getDatabaseProductName();
        String jdbcUrl = metaData.getURL();
        DatabaseType databaseType = JdbcUtils.getDbType(productName, jdbcUrl);
        return databaseType == null ? DatabaseType.OTHER : databaseType;
    }

    private static Map<DatabaseType, DatabaseIntrospectorStrategy> createStrategies() {
        Map<DatabaseType, DatabaseIntrospectorStrategy> strategies = new EnumMap<>(DatabaseType.class);

        DatabaseIntrospectorStrategy mysqlStrategy = new MysqlIntrospectorStrategy();
        strategies.put(DatabaseType.MYSQL, mysqlStrategy);
        strategies.put(DatabaseType.MARIADB, mysqlStrategy);

        DatabaseIntrospectorStrategy sqlServerStrategy = new SqlServerIntrospectorStrategy();
        strategies.put(DatabaseType.SQL_SERVER, sqlServerStrategy);
        strategies.put(DatabaseType.SQL_SERVER2005, sqlServerStrategy);

        strategies.put(DatabaseType.ORACLE, new OracleIntrospectorStrategy());
        strategies.put(DatabaseType.POSTGRE_SQL, new PostgreSqlIntrospectorStrategy());
        strategies.put(DatabaseType.DM, new DamengIntrospectorStrategy());
        return strategies;
    }
}
