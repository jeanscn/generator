package org.mybatis.generator.dbstrategy;

import com.vgosoft.core.db.query.DatabaseType;
import com.vgosoft.core.db.util.JdbcUtils;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;
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
    private static final DatabaseProfile GENERIC_PROFILE = new DatabaseProfile(GENERIC_STRATEGY, null);

    private static final Map<DatabaseType, DatabaseProfile> PROFILES = createProfiles();

    public static DatabaseIntrospectorStrategy createIntrospectorStrategy(Connection conn) throws SQLException {
        return createProfile(conn).introspectorStrategy();
    }

    public static DatabaseProfile createProfile(Connection conn) throws SQLException {
        DatabaseType databaseType = resolveDatabaseType(conn);
        return PROFILES.getOrDefault(databaseType, GENERIC_PROFILE);
    }

    public static DatabaseProfile createProfile(DatabaseType databaseType) {
        return PROFILES.getOrDefault(databaseType, GENERIC_PROFILE);
    }

    public static DatabaseProfile createProfile(String databaseProductName, String jdbcUrl) {
        DatabaseType databaseType = resolveDatabaseType(databaseProductName, jdbcUrl);
        return PROFILES.getOrDefault(databaseType, GENERIC_PROFILE);
    }

    static DatabaseType resolveDatabaseType(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        return resolveDatabaseType(metaData.getDatabaseProductName(), metaData.getURL());
    }

    static DatabaseType resolveDatabaseType(String productName, String jdbcUrl) {
        DatabaseType databaseType = JdbcUtils.getDbType(productName, jdbcUrl);
        return databaseType == null ? DatabaseType.OTHER : databaseType;
    }

    private static Map<DatabaseType, DatabaseProfile> createProfiles() {
        Map<DatabaseType, DatabaseProfile> profiles = new EnumMap<>(DatabaseType.class);

        DatabaseIntrospectorStrategy mysqlStrategy = new MysqlIntrospectorStrategy();
        DatabaseProfile mysqlProfile = new DatabaseProfile(mysqlStrategy, DatabaseDDLDialects.MYSQL);
        profiles.put(DatabaseType.MYSQL, mysqlProfile);
        profiles.put(DatabaseType.MARIADB, mysqlProfile);

        DatabaseIntrospectorStrategy sqlServerStrategy = new SqlServerIntrospectorStrategy();
        DatabaseProfile sqlServerProfile = new DatabaseProfile(sqlServerStrategy, DatabaseDDLDialects.SQL_SERVER);
        profiles.put(DatabaseType.SQL_SERVER, sqlServerProfile);
        profiles.put(DatabaseType.SQL_SERVER2005, sqlServerProfile);

        profiles.put(DatabaseType.ORACLE, new DatabaseProfile(new OracleIntrospectorStrategy(), DatabaseDDLDialects.ORACLE));
        DatabaseIntrospectorStrategy postgreSqlStrategy = new PostgreSqlIntrospectorStrategy();
        profiles.put(DatabaseType.POSTGRE_SQL, new DatabaseProfile(postgreSqlStrategy, DatabaseDDLDialects.POSTGRE_SQL));
        profiles.put(DatabaseType.HIGHGO, new DatabaseProfile(postgreSqlStrategy, DatabaseDDLDialects.HIGHGO));
        profiles.put(DatabaseType.KINGBASE_ES, new DatabaseProfile(postgreSqlStrategy, DatabaseDDLDialects.KINGBASE_ES));

        profiles.put(DatabaseType.DM, new DatabaseProfile(new DamengIntrospectorStrategy(), DatabaseDDLDialects.DM));
        profiles.put(DatabaseType.H2, new DatabaseProfile(GENERIC_STRATEGY, DatabaseDDLDialects.H2));
        profiles.put(DatabaseType.HSQL, new DatabaseProfile(GENERIC_STRATEGY, DatabaseDDLDialects.HSQL));
        profiles.put(DatabaseType.DB2, new DatabaseProfile(GENERIC_STRATEGY, DatabaseDDLDialects.DB2));
        profiles.put(DatabaseType.XU_GU, new DatabaseProfile(GENERIC_STRATEGY, DatabaseDDLDialects.XU_GU));
        return profiles;
    }
}
