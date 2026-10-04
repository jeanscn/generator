package org.mybatis.generator.dbstrategy;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;
import org.mybatis.generator.dbstrategy.introspectorstrategy.DamengIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.GenericIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.MysqlIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.OracleIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.PostgreSqlIntrospectorStrategy;
import org.mybatis.generator.dbstrategy.introspectorstrategy.SqlServerIntrospectorStrategy;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseIntrospectorFactoryTest {

    @Test
    void shouldCreateMysqlStrategy() throws SQLException {
        DatabaseProfile profile = DatabaseIntrospectorFactory
                .createProfile(connection("MySQL", "jdbc:mysql://localhost:3306/demo"));

        assertThat(profile.introspectorStrategy()).isInstanceOf(MysqlIntrospectorStrategy.class);
        assertThat(profile.ddlDialect()).isEqualTo(DatabaseDDLDialects.MYSQL);
    }

    @Test
    void shouldCreateMysqlStrategyForMariaDbUrl() throws SQLException {
        DatabaseProfile profile = DatabaseIntrospectorFactory
                .createProfile(connection("MySQL", "jdbc:mariadb://localhost:3306/demo"));

        assertThat(profile.introspectorStrategy()).isInstanceOf(MysqlIntrospectorStrategy.class);
        assertThat(profile.ddlDialect()).isEqualTo(DatabaseDDLDialects.MYSQL);
    }

    @Test
    void shouldCreateMysqlStrategyForMariaDbProduct() throws SQLException {
        DatabaseProfile profile = DatabaseIntrospectorFactory
                .createProfile(connection("MariaDB", "jdbc:unknown://localhost:3306/demo"));

        assertThat(profile.introspectorStrategy()).isInstanceOf(MysqlIntrospectorStrategy.class);
        assertThat(profile.ddlDialect()).isEqualTo(DatabaseDDLDialects.MYSQL);
    }

    @Test
    void shouldCreateSqlServerStrategy() throws SQLException {
        DatabaseProfile profile = DatabaseIntrospectorFactory
                .createProfile(connection("Microsoft SQL Server", "jdbc:sqlserver://localhost:1433;databaseName=demo"));

        assertThat(profile.introspectorStrategy()).isInstanceOf(SqlServerIntrospectorStrategy.class);
        assertThat(profile.ddlDialect()).isEqualTo(DatabaseDDLDialects.SQL_SERVER);
    }

    @Test
    void shouldCreateOracleStrategy() throws SQLException {
        DatabaseProfile profile = DatabaseIntrospectorFactory
                .createProfile(connection("Oracle", "jdbc:oracle:thin:@localhost:1521:xe"));

        assertThat(profile.introspectorStrategy()).isInstanceOf(OracleIntrospectorStrategy.class);
        assertThat(profile.ddlDialect()).isEqualTo(DatabaseDDLDialects.ORACLE);
    }

    @Test
    void shouldCreateDamengStrategy() throws SQLException {
        DatabaseProfile profile = DatabaseIntrospectorFactory
                .createProfile(connection("DM DBMS", "jdbc:dm://localhost:5236"));

        assertThat(profile.introspectorStrategy()).isInstanceOf(DamengIntrospectorStrategy.class);
        assertThat(profile.ddlDialect()).isEqualTo(DatabaseDDLDialects.DM);
    }

    @Test
    void shouldCreatePostgreSqlStrategy() throws SQLException {
        DatabaseProfile profile = DatabaseIntrospectorFactory
                .createProfile(connection("PostgreSQL", "jdbc:postgresql://localhost:5432/demo"));

        assertThat(profile.introspectorStrategy()).isInstanceOf(PostgreSqlIntrospectorStrategy.class);
        assertThat(profile.ddlDialect()).isEqualTo(DatabaseDDLDialects.POSTGRE_SQL);
    }

    @Test
    void shouldCreatePostgreSqlStrategyForHighgo() throws SQLException {
        DatabaseProfile profile = DatabaseIntrospectorFactory
                .createProfile(connection("HIGHGO", "jdbc:highgo://localhost:5866/demo"));

        assertThat(profile.introspectorStrategy()).isInstanceOf(PostgreSqlIntrospectorStrategy.class);
        assertThat(profile.ddlDialect()).isEqualTo(DatabaseDDLDialects.HIGHGO);
    }

    @Test
    void shouldCreatePostgreSqlStrategyForKingbase() throws SQLException {
        DatabaseProfile profile = DatabaseIntrospectorFactory
                .createProfile(connection("KingbaseES", "jdbc:kingbase8://localhost:54321/demo"));

        assertThat(profile.introspectorStrategy()).isInstanceOf(PostgreSqlIntrospectorStrategy.class);
        assertThat(profile.ddlDialect()).isEqualTo(DatabaseDDLDialects.KINGBASE_ES);
    }

    @Test
    void shouldCreateGenericStrategyForXugu() throws SQLException {
        DatabaseProfile profile = DatabaseIntrospectorFactory
                .createProfile(connection("Xugu", "jdbc:xugu://localhost:5138/demo"));

        assertThat(profile.introspectorStrategy()).isInstanceOf(GenericIntrospectorStrategy.class);
        assertThat(profile.ddlDialect()).isEqualTo(DatabaseDDLDialects.XU_GU);
    }

    @Test
    void shouldUseGenericDdlProfileForOtherDatabaseTypes() throws SQLException {
        DatabaseProfile profile = DatabaseIntrospectorFactory
                .createProfile(connection("Firebird", "jdbc:firebirdsql://localhost:3050/demo"));

        assertThat(profile.introspectorStrategy()).isInstanceOf(GenericIntrospectorStrategy.class);
        assertThat(profile.ddlDialect()).isEqualTo(DatabaseDDLDialects.OTHER);
    }

    @Test
    void shouldProvideDdlProfilesForEveryDatabaseType() {
        for (com.vgosoft.core.db.query.DatabaseType databaseType
                : com.vgosoft.core.db.query.DatabaseType.values()) {
            assertThat(DatabaseIntrospectorFactory.createProfile(databaseType).ddlDialect())
                    .as(databaseType.name())
                    .isNotNull();
        }
    }

    private Connection connection(String productName, String jdbcUrl) {
        DatabaseMetaData metaData = (DatabaseMetaData) Proxy.newProxyInstance(
                DatabaseMetaData.class.getClassLoader(),
                new Class<?>[]{DatabaseMetaData.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getDatabaseProductName" -> productName;
                    case "getURL" -> jdbcUrl;
                    case "unwrap" -> null;
                    case "isWrapperFor" -> false;
                    default -> defaultValue(method.getReturnType());
                });

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getMetaData" -> metaData;
                    case "unwrap" -> null;
                    case "isWrapperFor" -> false;
                    default -> defaultValue(method.getReturnType());
                });
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        if (type == double.class) {
            return 0D;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }
}
