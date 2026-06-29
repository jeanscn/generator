package org.mybatis.generator.dbstrategy;

import org.junit.jupiter.api.Test;
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
        DatabaseIntrospectorStrategy strategy = DatabaseIntrospectorFactory
                .createIntrospectorStrategy(connection("MySQL", "jdbc:mysql://localhost:3306/demo"));

        assertThat(strategy).isInstanceOf(MysqlIntrospectorStrategy.class);
    }

    @Test
    void shouldCreateMysqlStrategyForMariaDbUrl() throws SQLException {
        DatabaseIntrospectorStrategy strategy = DatabaseIntrospectorFactory
                .createIntrospectorStrategy(connection("MySQL", "jdbc:mariadb://localhost:3306/demo"));

        assertThat(strategy).isInstanceOf(MysqlIntrospectorStrategy.class);
    }

    @Test
    void shouldCreateSqlServerStrategy() throws SQLException {
        DatabaseIntrospectorStrategy strategy = DatabaseIntrospectorFactory
                .createIntrospectorStrategy(connection("Microsoft SQL Server", "jdbc:sqlserver://localhost:1433;databaseName=demo"));

        assertThat(strategy).isInstanceOf(SqlServerIntrospectorStrategy.class);
    }

    @Test
    void shouldCreateOracleStrategy() throws SQLException {
        DatabaseIntrospectorStrategy strategy = DatabaseIntrospectorFactory
                .createIntrospectorStrategy(connection("Oracle", "jdbc:oracle:thin:@localhost:1521:xe"));

        assertThat(strategy).isInstanceOf(OracleIntrospectorStrategy.class);
    }

    @Test
    void shouldCreateDamengStrategy() throws SQLException {
        DatabaseIntrospectorStrategy strategy = DatabaseIntrospectorFactory
                .createIntrospectorStrategy(connection("DM DBMS", "jdbc:dm://localhost:5236"));

        assertThat(strategy).isInstanceOf(DamengIntrospectorStrategy.class);
    }

    @Test
    void shouldCreatePostgreSqlStrategy() throws SQLException {
        DatabaseIntrospectorStrategy strategy = DatabaseIntrospectorFactory
                .createIntrospectorStrategy(connection("PostgreSQL", "jdbc:postgresql://localhost:5432/demo"));

        assertThat(strategy).isInstanceOf(PostgreSqlIntrospectorStrategy.class);
    }

    @Test
    void shouldFallbackToGenericStrategyForUnsupportedDatabase() throws SQLException {
        DatabaseIntrospectorStrategy strategy = DatabaseIntrospectorFactory
                .createIntrospectorStrategy(connection("Firebird", "jdbc:firebirdsql://localhost:3050/demo"));

        assertThat(strategy).isInstanceOf(GenericIntrospectorStrategy.class);
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
