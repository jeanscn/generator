package org.mybatis.generator.dbstrategy.introspectorstrategy;

import com.vgosoft.core.db.model.metadata.Column;
import com.vgosoft.core.db.model.metadata.Database;
import com.vgosoft.core.db.model.metadata.PrimaryKey;
import com.vgosoft.core.db.model.metadata.Table;
import com.vgosoft.core.db.query.DatabaseQuery;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedKotlinFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GeneratedHtmlFile;
import org.mybatis.generator.codegen.mybatis3.sqlschema.GeneratedSqlSchemaFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RemarksIntrospectorStrategyTest {

    @Test
    void shouldUseQueryBackedRemarksForMysql() throws Exception {
        assertRemarkFlow(new TestMysqlStrategy(new StubDatabaseQuery()),
                "order_id", "订单编号", "订单表");
    }

    @Test
    void shouldUseQueryBackedRemarksForOracle() throws Exception {
        assertRemarkFlow(new TestOracleStrategy(new StubDatabaseQuery()),
                "order_id", "订单编号", "订单表");
    }

    @Test
    void shouldUseQueryBackedRemarksForPostgreSql() throws Exception {
        assertRemarkFlow(new TestPostgreSqlStrategy(new StubDatabaseQuery()),
                "order_id", "订单编号", "订单表");
    }

    @Test
    void shouldUseQueryBackedRemarksForGeneric() throws Exception {
        assertRemarkFlow(new TestGenericStrategy(new StubDatabaseQuery()),
                "order_id", "订单编号", "订单表");
    }

    @Test
    void shouldUseQueryBackedRemarksForDameng() throws Exception {
        assertRemarkFlow(new TestDamengStrategy(new StubDatabaseQuery()),
                "order_id", "订单编号", "订单表");
    }

    private void assertRemarkFlow(QueryBackedRemarksIntrospectorStrategy strategy,
                                  String expectedColumnName,
                                  String expectedColumnRemark,
                                  String expectedTableRemark) throws Exception {
        Map<String, String> remarks = strategy.getColumnRemarks(null, "orders");
        assertThat(remarks).containsEntry(expectedColumnName, expectedColumnRemark);

        TestIntrospectedTable introspectedTable = new TestIntrospectedTable();
        introspectedTable.setFullyQualifiedTable(new FullyQualifiedTable(
                null, null, "orders", "Order", null,
                false, null, null, null, false, null, null));
        introspectedTable.setRemarks("old");

        strategy.updateTableRemark(introspectedTable, null);
        assertThat(introspectedTable.getRemarks(false)).isEqualTo(expectedTableRemark);
    }

    private static final class StubDatabaseQuery implements DatabaseQuery {
        @Override
        public Database getDataBase() {
            return () -> "demo";
        }

        @Override
        public List<? extends Table> getTables() {
            return Collections.singletonList(new Table() {
                @Override
                public String getTableName() {
                    return "orders";
                }

                @Override
                public String getRemarks() {
                    return "订单表";
                }
            });
        }

        @Override
        public List<? extends Column> getTableColumns(String tableName) {
            return Collections.singletonList(new Column() {
                @Override
                public String getTableName() {
                    return "orders";
                }

                @Override
                public String getOrdinalPosition() {
                    return "1";
                }

                @Override
                public String getColumnName() {
                    return "order_id";
                }

                @Override
                public String getTypeName() {
                    return "VARCHAR";
                }

                @Override
                public String getColumnSize() {
                    return "64";
                }

                @Override
                public String getDecimalDigits() {
                    return null;
                }

                @Override
                public String getNullable() {
                    return "N";
                }

                @Override
                public String getPrimaryKey() {
                    return "Y";
                }

                @Override
                public String getColumnDef() {
                    return null;
                }

                @Override
                public String getRemarks() {
                    return "订单编号";
                }

                @Override
                public String getColumnType() {
                    return "VARCHAR(64)";
                }

                @Override
                public String getColumnLength() {
                    return "64";
                }
            });
        }

        @Override
        public List<? extends Column> getTableColumns() {
            return getTableColumns("orders");
        }

        @Override
        public List<? extends PrimaryKey> getPrimaryKeys(String tableName) {
            return Collections.emptyList();
        }

        @Override
        public List<? extends PrimaryKey> getPrimaryKeys() {
            return Collections.emptyList();
        }
    }

    private static final class TestMysqlStrategy extends MysqlIntrospectorStrategy {
        private final DatabaseQuery query;

        private TestMysqlStrategy(DatabaseQuery query) {
            this.query = query;
        }

        @Override
        protected DatabaseQuery createDatabaseQuery(java.sql.Connection conn) {
            return query;
        }
    }

    private static final class TestOracleStrategy extends OracleIntrospectorStrategy {
        private final DatabaseQuery query;

        private TestOracleStrategy(DatabaseQuery query) {
            this.query = query;
        }

        @Override
        protected DatabaseQuery createDatabaseQuery(java.sql.Connection conn) {
            return query;
        }
    }

    private static final class TestPostgreSqlStrategy extends PostgreSqlIntrospectorStrategy {
        private final DatabaseQuery query;

        private TestPostgreSqlStrategy(DatabaseQuery query) {
            this.query = query;
        }

        @Override
        protected DatabaseQuery createDatabaseQuery(java.sql.Connection conn) {
            return query;
        }
    }

    private static final class TestGenericStrategy extends GenericIntrospectorStrategy {
        private final DatabaseQuery query;

        private TestGenericStrategy(DatabaseQuery query) {
            this.query = query;
        }

        @Override
        protected DatabaseQuery createDatabaseQuery(java.sql.Connection conn) {
            return query;
        }
    }

    private static final class TestDamengStrategy extends DamengIntrospectorStrategy {
        private final DatabaseQuery query;

        private TestDamengStrategy(DatabaseQuery query) {
            this.query = query;
        }

        @Override
        protected DatabaseQuery createDatabaseQuery(java.sql.Connection conn) {
            return query;
        }
    }

    private static final class TestIntrospectedTable extends AbstractIntrospectedTable {
        private TestIntrospectedTable() {
            super(TargetRuntime.MYBATIS3);
        }

        @Override
        public void calculateCustom(List<String> warnings, ProgressCallback callback) {
        }

        @Override
        public void calculateGenerators(List<String> warnings, ProgressCallback progressCallback) {
        }

        @Override
        public List<GeneratedJavaFile> getGeneratedJavaFiles() {
            return Collections.emptyList();
        }

        @Override
        public List<GeneratedXmlFile> getGeneratedXmlFiles() {
            return Collections.emptyList();
        }

        @Override
        public List<GeneratedHtmlFile> getGeneratedHtmlFiles() {
            return Collections.emptyList();
        }

        @Override
        public List<GeneratedSqlSchemaFile> getGeneratedSqlSchemaFiles() {
            return Collections.emptyList();
        }

        @Override
        public List<GeneratedSqlSchemaFile> getGeneratedPermissionSqlDataFiles() {
            return Collections.emptyList();
        }

        @Override
        public List<GeneratedSqlSchemaFile> getGeneratedPermissionActionSqlDataFiles() {
            return Collections.emptyList();
        }

        @Override
        public List<GeneratedKotlinFile> getGeneratedKotlinFiles() {
            return Collections.emptyList();
        }

        @Override
        public int getGenerationSteps() {
            return 0;
        }

        @Override
        public boolean requiresXmlGenerator() {
            return false;
        }
    }
}
