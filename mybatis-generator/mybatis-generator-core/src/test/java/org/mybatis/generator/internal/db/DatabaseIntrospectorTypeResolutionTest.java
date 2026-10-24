package org.mybatis.generator.internal.db;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.SqlScriptRunner;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.JavaTypeResolver;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.types.MbgJavaTypeResolverDefaultImpl;

import java.sql.Types;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseIntrospectorTypeResolutionTest {
    private static final String DRIVER = "org.hsqldb.jdbcDriver";
    private static final String URL = "jdbc:hsqldb:mem:introspectorType";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    @Test
    void shouldResolveStringPrimaryKeyTypeFromDatabaseQueryMetadata() throws Exception {
        createDatabase();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Context context = new Context(ModelType.FLAT);
            List<String> warnings = new ArrayList<>();
            JavaTypeResolver javaTypeResolver = ObjectFactory.createJavaTypeResolver(context, warnings);

            DatabaseIntrospector introspector = new DatabaseIntrospector(
                    context,
                    connection.getMetaData(),
                    javaTypeResolver,
                    warnings);

            TableConfiguration tableConfiguration = new TableConfiguration(context, warnings);
            tableConfiguration.setTableName("METAADAPTERPARENT");

            List<AbstractIntrospectedTable> tables = introspector.introspectTables(tableConfiguration);
            assertThat(tables).hasSize(1);

            AbstractIntrospectedTable table = tables.get(0);
            IntrospectedColumn stringPk = findColumn(table, "PARENT_CODE");
            IntrospectedColumn numberPk = findColumn(table, "PARENT_ID");

            assertThat(stringPk.getFullyQualifiedJavaType().getFullyQualifiedName())
                    .isEqualTo("java.lang.String");
            assertThat(numberPk.getFullyQualifiedJavaType().getFullyQualifiedName())
                    .isEqualTo("java.lang.Integer");
        }
    }

    @Test
    void shouldNormalizeBinaryJdbcTypesToBlob() {
        assertThat(resolveJdbcTypeName(Types.BINARY)).isEqualTo("BLOB");
        assertThat(resolveJdbcTypeName(Types.VARBINARY)).isEqualTo("BLOB");
        assertThat(resolveJdbcTypeName(Types.LONGVARBINARY)).isEqualTo("BLOB");
    }

    @Test
    void shouldApplyByteArrayTypeHandlerOnlyWhenNotOverridden() {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setFullyQualifiedJavaType(new FullyQualifiedJavaType("byte[]"));

        DatabaseIntrospector.applyDefaultTypeHandler(column);

        assertThat(column.getTypeHandler())
                .isEqualTo("org.apache.ibatis.type.ByteArrayTypeHandler");

        IntrospectedColumn overriddenColumn = new IntrospectedColumn();
        overriddenColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType("byte[]"));
        overriddenColumn.setTypeHandler("custom.TypeHandler");

        DatabaseIntrospector.applyDefaultTypeHandler(overriddenColumn);

        assertThat(overriddenColumn.getTypeHandler()).isEqualTo("custom.TypeHandler");
    }

    private IntrospectedColumn findColumn(AbstractIntrospectedTable table, String columnName) {
        return table.getAllColumns().stream()
                .filter(column -> columnName.equalsIgnoreCase(column.getActualColumnName()))
                .findFirst()
                .orElseThrow();
    }

    private String resolveJdbcTypeName(int jdbcType) {
        MbgJavaTypeResolverDefaultImpl resolver = new MbgJavaTypeResolverDefaultImpl();
        IntrospectedColumn column = new IntrospectedColumn();
        column.setJdbcType(jdbcType);
        return resolver.calculateJdbcTypeName(column);
    }

    private void createDatabase() throws Exception {
        SqlScriptRunner scriptRunner = new SqlScriptRunner(
                getClass().getResourceAsStream("/scripts/CreateDB.sql"),
                DRIVER,
                URL,
                USER,
                PASSWORD);
        scriptRunner.executeScript();
    }
}
