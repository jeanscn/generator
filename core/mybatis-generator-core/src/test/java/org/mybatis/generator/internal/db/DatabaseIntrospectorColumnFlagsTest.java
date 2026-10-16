package org.mybatis.generator.internal.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.ObjectFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 从实际 JDBC 适配器与兜底路径验证生成列、自增及默认值，不连接外部数据库。
 *
 * @author vgosoft-agent
 */
class DatabaseIntrospectorColumnFlagsTest {
    /** 五种 MySQL 系方言的实际适配路径均应排除默认表达式，并保留真生成列与自增。 */
    @ParameterizedTest
    @ValueSource(strings = {"mysql", "mariadb", "tidb", "polardb", "tdsql"})
    void adapterReadsTypedNormalizedFlags(String dialect) throws Exception {
        Fixture fixture = new Fixture(dialect, false);
        assertFlags(fixture.readColumns());
        assertThat(fixture.warnings).isEmpty();
        assertThat(fixture.directQueries).hasValue(0);
    }

    /** 适配器失败时五种 MySQL 系方言仍通过共享 EXTRA 规则纠正驱动误报。 */
    @ParameterizedTest
    @ValueSource(strings = {"mysql", "mariadb", "tidb", "polardb", "tdsql"})
    void fallbackNormalizesMysqlFamilyFlags(String dialect) throws Exception {
        Fixture fixture = new Fixture(dialect, true);
        assertFlags(fixture.readColumns());
        assertThat(fixture.warnings).isNotEmpty();
        assertThat(fixture.directQueries).hasValue(1);
        assertThat(fixture.resultCloses.get()).isPositive();
    }

    /** 非 MySQL JDBC 兜底接受规范标志及缺失可选标志，不能擅自查询 MySQL 字典。 */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void genericJdbcFallbackPreservesOptionalFlagSemantics(boolean omitFlags) throws Exception {
        Fixture fixture = new Fixture("other", true);
        fixture.omitFlags = omitFlags;
        List<IntrospectedColumn> columns = fixture.readColumns();
        assertThat(columns.get(0).isGeneratedColumn()).isEqualTo(!omitFlags);
        assertThat(columns.get(2).isAutoIncrement()).isEqualTo(!omitFlags);
        assertThat(columns.get(0).getDefaultValue()).isEqualTo("CURRENT_TIMESTAMP");
        assertThat(fixture.directQueries).hasValue(0);
    }

    /** 真实表的归一化元数据缺失时显式失败并关闭结果集，不再采信驱动误报。 */
    @Test
    void missingNormalizedColumnFailsClosed() {
        Fixture fixture = new Fixture("mysql", true);
        fixture.missingColumn = true;
        SQLException error = assertThrows(SQLException.class, fixture::readColumns);
        assertThat(error.getMessage()).contains("created_");
        assertThat(fixture.resultCloses.get()).isPositive();
    }

    /** 信息模式读取被拒绝时传播查询异常，不能将错误元数据当作成功内省。 */
    @Test
    void failedNormalizationPropagatesSqlError() {
        Fixture fixture = new Fixture("mysql", true);
        fixture.denyNormalization = true;
        SQLException error = assertThrows(SQLException.class, fixture::readColumns);
        assertThat(error.getMessage()).contains("denied");
        assertThat(fixture.resultCloses.get()).isPositive();
    }

    private static void assertFlags(List<IntrospectedColumn> columns) {
        assertThat(columns).hasSize(3);
        assertThat(columns.get(0).getActualColumnName()).isEqualTo("created_");
        assertThat(columns.get(0).isGeneratedColumn()).isFalse();
        assertThat(columns.get(0).getDefaultValue()).isEqualTo("CURRENT_TIMESTAMP");
        assertThat(columns.get(1).isGeneratedColumn()).isTrue();
        assertThat(columns.get(2).isAutoIncrement()).isTrue();
        assertThat(columns.get(2).isGeneratedColumn()).isFalse();
    }

    /**
     * 用 JDBC 接口代理复现驱动元数据形状，保留真实工厂、映射和内省调用链。
     *
     * @author vgosoft-agent
     */
    private static class Fixture {
        private final String dialect;
        private final boolean failAdapter;
        private final List<String> warnings = new ArrayList<>();
        private final AtomicInteger directQueries = new AtomicInteger();
        private final AtomicInteger resultCloses = new AtomicInteger();
        private boolean omitFlags;
        private boolean missingColumn;
        private boolean denyNormalization;
        private Connection connection;

        Fixture(String dialect, boolean failAdapter) {
            this.dialect = dialect;
            this.failAdapter = failAdapter;
        }

        @SuppressWarnings("unchecked")
        List<IntrospectedColumn> readColumns() throws Exception {
            DatabaseMetaData metadata = proxy(DatabaseMetaData.class, (target, method, args) -> switch (method.getName()) {
                case "getDatabaseProductName" -> dialect;
                case "getURL" -> "jdbc:" + dialect + ":fixture";
                case "getConnection" -> connection;
                case "getImportedKeys" -> rows(new String[]{"FKCOLUMN_NAME"}, new String[0][]);
                case "getColumns" -> jdbcColumns();
                default -> defaultValue(method);
            });
            connection = proxy(Connection.class, (target, method, args) -> switch (method.getName()) {
                case "getMetaData" -> metadata;
                case "getCatalog" -> "fixture";
                case "prepareStatement" -> statement((String) args[0]);
                default -> defaultValue(method);
            });
            Context context = new Context(ModelType.FLAT);
            DatabaseIntrospector introspector = new DatabaseIntrospector(context, metadata,
                    ObjectFactory.createJavaTypeResolver(context, warnings), warnings);
            TableConfiguration configuration = new TableConfiguration(context, warnings);
            configuration.setTableName("sensitive_category");
            Method read = DatabaseIntrospector.class.getDeclaredMethod("getColumns", TableConfiguration.class);
            read.setAccessible(true);
            try {
                Map<?, List<IntrospectedColumn>> tables =
                        (Map<?, List<IntrospectedColumn>>) read.invoke(introspector, configuration);
                return tables.values().stream().flatMap(List::stream).toList();
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception exception) {
                    throw exception;
                }
                throw e;
            }
        }

        private PreparedStatement statement(String sql) throws SQLException {
            boolean direct = sql.startsWith("SELECT COLUMN_NAME, EXTRA");
            if (!direct && failAdapter) {
                throw new SQLException("adapter fixture failure");
            }
            if (direct) {
                directQueries.incrementAndGet();
                if (denyNormalization) {
                    throw new SQLException("normalization denied");
                }
            }
            return proxy(PreparedStatement.class, (target, method, args) -> {
                if ("executeQuery".equals(method.getName())) {
                    if (direct) {
                        return rows(new String[]{"COLUMN_NAME", "EXTRA"}, missingColumn ? new String[0][]
                                : new String[][]{{"created_", "DEFAULT_GENERATED on update CURRENT_TIMESTAMP"},
                                        {"computed_", "STORED GENERATED"}, {"id_", "auto_increment"}});
                    }
                    return rows(new String[]{"TABLE_NAME", "COLUMN_NAME", "EXTRA", "COLUMN_TYPE", "COLUMN_LENGTH"},
                            new String[][]{{"sensitive_category", "created_", "DEFAULT_GENERATED", "timestamp", null},
                                    {"sensitive_category", "computed_", "VIRTUAL GENERATED", "int", "11"},
                                    {"sensitive_category", "id_", "auto_increment", "bigint", "20"}});
                }
                return defaultValue(method);
            });
        }

        private ResultSet jdbcColumns() {
            List<String> names = new ArrayList<>(List.of("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME",
                    "DATA_TYPE", "TYPE_NAME", "COLUMN_SIZE", "DECIMAL_DIGITS", "NULLABLE", "COLUMN_DEF", "REMARKS"));
            if (!omitFlags) {
                names.addAll(List.of("IS_GENERATEDCOLUMN", "IS_AUTOINCREMENT"));
            }
            return rows(names.toArray(String[]::new), new String[][]{
                    {"fixture", null, "sensitive_category", "created_", "93", "TIMESTAMP", "26", "0", "1",
                            "CURRENT_TIMESTAMP", "创建时间", "YES", "NO"},
                    {"fixture", null, "sensitive_category", "computed_", "4", "INTEGER", "11", "0", "1",
                            null, "计算列", "YES", "NO"},
                    {"fixture", null, "sensitive_category", "id_", "-5", "BIGINT", "20", "0", "0",
                            null, "标识", "NO", "YES"}});
        }

        private ResultSet rows(String[] names, String[][] values) {
            AtomicInteger row = new AtomicInteger(-1);
            ResultSetMetaData metadata = proxy(ResultSetMetaData.class, (target, method, args) -> switch (method.getName()) {
                case "getColumnCount" -> names.length;
                case "getColumnName" -> names[(Integer) args[0] - 1];
                case "getColumnLabel" -> {
                    String name = names[(Integer) args[0] - 1];
                    yield name.startsWith("IS_") ? name.toLowerCase(java.util.Locale.ROOT) : name;
                }
                default -> defaultValue(method);
            });
            return proxy(ResultSet.class, (target, method, args) -> {
                return switch (method.getName()) {
                    case "next" -> row.incrementAndGet() < values.length;
                    case "getMetaData" -> metadata;
                    case "getString", "getInt" -> {
                        int index = args[0] instanceof Integer number ? number - 1
                                : Arrays.asList(names).indexOf(args[0].toString().toUpperCase());
                        String value = index < 0 ? null : values[row.get()][index];
                        yield "getInt".equals(method.getName()) ? value == null ? 0 : Integer.parseInt(value) : value;
                    }
                    case "close" -> {
                        resultCloses.incrementAndGet();
                        yield null;
                    }
                    default -> defaultValue(method);
                };
            });
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValue(Method method) {
        if (method.getReturnType() == boolean.class) {
            return false;
        }
        if (method.getReturnType() == int.class) {
            return 0;
        }
        return null;
    }
}
