package org.mybatis.generator.offline;

import com.vgosoft.core.db.query.DatabaseType;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlSchemaScriptGenerator;

import java.sql.JDBCType;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证数据库结构脚本按目标方言生成正确的列类型。
 *
 * @author vgosoft-agent
 */
class SqlSchemaScriptGeneratorTest {

    /**
     * 验证 MySQL 与 MariaDB 保留原生大字段类型，同时普通类型映射保持不变。
     */
    @Test
    void preservesMysqlLobTypesAndOtherTypeMappings() {
        IntrospectedTableMyBatis3Impl table = tableWithLobColumns();

        for (DatabaseType database : List.of(DatabaseType.MYSQL, DatabaseType.MARIADB)) {
            String ddl = new SqlSchemaScriptGenerator(table, database).getSqlScript();

            assertThat(ddl)
                    .contains("bytes_ LONGBLOB", "content_md LONGTEXT")
                    .contains("title_ VARCHAR(64)", "status_ INT");
        }
    }

    /**
     * 验证 PostgreSQL 与达梦仍按目标方言映射大字段类型，而不复用 MySQL 类型名。
     */
    @Test
    void keepsTargetDialectMappingsForNonMysqlDatabases() {
        IntrospectedTableMyBatis3Impl table = tableWithLobColumns();

        String postgresqlDdl = new SqlSchemaScriptGenerator(table, DatabaseType.POSTGRE_SQL).getSqlScript();
        assertThat(postgresqlDdl)
                .contains("bytes_ BYTEA", "content_md TEXT")
                .doesNotContain("LONGBLOB", "LONGTEXT");

        String dmDdl = new SqlSchemaScriptGenerator(table, DatabaseType.DM).getSqlScript();
        assertThat(dmDdl)
                .contains("bytes_ BLOB", "content_md CLOB")
                .doesNotContain("LONGBLOB", "LONGTEXT");
    }

    private IntrospectedTableMyBatis3Impl tableWithLobColumns() {
        IntrospectedTableMyBatis3Impl table = ManagedRelationOfflineRegenerator.fixture(
                "sample", "ManualDoc", "vbiz_manual_doc", null);
        table.addColumn(column("bytes_", JDBCType.LONGVARBINARY, "longblob", 0));
        table.addColumn(column("content_md", JDBCType.LONGVARCHAR, "longtext", 0));
        table.addColumn(column("title_", JDBCType.VARCHAR, "varchar", 64));
        table.addColumn(column("status_", JDBCType.INTEGER, "integer", 0));
        return table;
    }

    private IntrospectedColumn column(String name, JDBCType jdbcType, String actualTypeName, int length) {
        IntrospectedColumn column = ManagedRelationOfflineRegenerator.column(name, name, "测试字段", true);
        column.setJdbcType(jdbcType.getVendorTypeNumber());
        column.setJdbcTypeName(jdbcType.getName());
        column.setActualTypeName(actualTypeName);
        column.setLength(length);
        return column;
    }
}
