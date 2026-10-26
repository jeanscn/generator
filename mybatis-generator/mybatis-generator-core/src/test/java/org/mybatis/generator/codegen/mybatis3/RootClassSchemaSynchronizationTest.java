package org.mybatis.generator.codegen.mybatis3;

import com.vgosoft.core.annotation.ColumnMeta;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.ConnectionFactory;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class RootClassSchemaSynchronizationTest {
    @Test
    void ignoresNullableMismatchForPrimaryKeyWhenNoOtherSchemaChangeExists() throws Exception {
        var result = generate(NullablePrimaryKeyRoot.class);

        assertThat(SchemaRecordingFactory.statements).isEmpty();
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("标注为可空主键")
                && warning.contains("按 NOT NULL 处理"));
        assertThat(result.warnings()).noneMatch(warning -> warning.contains("更新数据库Sql"));
    }

    @Test
    void keepsPrimaryKeyNotNullWhenAnotherMetadataChangeRequiresModify() throws Exception {
        var result = generate(ModifiedNullablePrimaryKeyRoot.class);

        assertThat(SchemaRecordingFactory.statements).hasSize(1);
        assertThat(SchemaRecordingFactory.statements.get(0))
                .contains("MODIFY COLUMN id_ VARCHAR(64)")
                .contains("NOT NULL")
                .doesNotContain(") NULL");
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("更新数据库时发生错误")
                && warning.contains("阻止结构写入"));
    }

    private static GenerationResult generate(Class<?> rootClass) throws Exception {
        var parser = new ConfigurationParser(new ArrayList<>());
        Configuration configuration = parser.parseConfiguration(
                new StringReader(CasConsumerBaselineTest.consumerSnapshotXml(null)));
        parser.customConfig(configuration);
        var context = configuration.getContexts().get(0);
        context.getTableConfigurations().removeIf(table -> !table.getTableName().equals("sensitive_rule_revision"));
        TableConfiguration table = context.getTableConfigurations().get(0);
        table.addProperty(PropertyRegistry.ROOT_CLASS, rootClass.getName());
        context.getConnectionFactoryConfiguration().setConfigurationType(SchemaRecordingFactory.class.getName());

        SchemaRecordingFactory.statements.clear();
        List<String> warnings = new ArrayList<>();
        var generator = new MyBatisGenerator(configuration, new DefaultShellCallback(true), warnings);
        generator.generate(null, null, null, false);
        return new GenerationResult(List.copyOf(warnings));
    }

    private record GenerationResult(List<String> warnings) { }

    public static class NullablePrimaryKeyRoot {
        @ColumnMeta(value = "id_", description = "编码（固定为00000000-0000-0000-0000-000000000001，仅保留单行）",
                type = JDBCType.VARCHAR, size = 36, pkid = true, nullable = true)
        private String id_;
    }

    public static class ModifiedNullablePrimaryKeyRoot {
        @ColumnMeta(value = "id_", description = "编码（固定为00000000-0000-0000-0000-000000000001，仅保留单行）",
                type = JDBCType.VARCHAR, size = 64, pkid = true, nullable = true)
        private String id_;
    }

    public static final class SchemaRecordingFactory implements ConnectionFactory {
        private static final List<String> statements = new ArrayList<>();

        @Override
        public void addConfigurationProperties(Properties properties) { }

        @Override
        public Connection getConnection() throws SQLException {
            Connection delegate = new CasConsumerBaselineTest.MetadataFactory().getConnection();
            return proxy(Connection.class, (proxy, method, arguments) -> {
                if (method.getName().equals("createStatement")) {
                    return proxy(Statement.class, (statement, statementMethod, statementArguments) -> {
                        if (statementMethod.getName().startsWith("execute")) {
                            if (statementArguments != null && statementArguments.length > 0
                                    && statementArguments[0] instanceof String sql) {
                                statements.add(sql);
                            }
                            throw new SQLException("阻止结构写入");
                        }
                        return primitive(statementMethod.getReturnType());
                    });
                }
                try {
                    return method.invoke(delegate, arguments);
                } catch (InvocationTargetException exception) {
                    throw exception.getCause();
                }
            });
        }

        private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
        }

        private static Object primitive(Class<?> type) {
            if (type == boolean.class) { return false; }
            if (type == int.class) { return 0; }
            if (type == long.class) { return 0L; }
            return null;
        }
    }
}
