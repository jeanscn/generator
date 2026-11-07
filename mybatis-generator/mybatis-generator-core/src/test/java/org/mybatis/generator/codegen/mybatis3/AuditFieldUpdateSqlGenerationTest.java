package org.mybatis.generator.codegen.mybatis3;

import com.vgosoft.core.db.query.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.XmlFormatter;
import org.mybatis.generator.api.dom.DefaultXmlFormatter;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.InsertOrUpdateSelectiveElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.UpdateBatchByPrimaryKeySelectiveElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.UpdateByExampleSelectiveElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.UpdateByExampleWithBLOBsElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.UpdateByExampleWithoutBLOBsElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.UpdateByPrimaryKeySelectiveElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.UpdateByPrimaryKeyWithBLOBsElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.UpdateByPrimaryKeyWithoutBLOBsElementGenerator;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.internal.rules.FlatModelRules;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AuditFieldUpdateSqlGenerationTest {

    @Test
    void updateStatementsExcludeImmutableAuditFieldsAndPreserveDeleteFlag() throws Exception {
        var table = fixture(DatabaseType.MYSQL);
        var generators = List.of(
                new UpdateByPrimaryKeyWithoutBLOBsElementGenerator(false),
                new UpdateByPrimaryKeyWithBLOBsElementGenerator(),
                new UpdateByPrimaryKeySelectiveElementGenerator(),
                new UpdateByExampleWithoutBLOBsElementGenerator(),
                new UpdateByExampleWithBLOBsElementGenerator(),
                new UpdateByExampleSelectiveElementGenerator());

        for (AbstractXmlElementGenerator generator : generators) {
            String xml = render(table, generator);
            assertThat(xml).contains(
                    "modified_ =",
                    "modified_id =",
                    "version_ = version_ + 1",
                    "delete_flag =");
            assertThat(xml).doesNotContain("created_ =", "created_id =", "tenant_id =");
        }

        String batch = render(table, new UpdateBatchByPrimaryKeySelectiveElementGenerator());
        assertThat(batch).contains(
                "modified_ = case id_",
                "modified_id = case id_",
                "version_ = case id_",
                "then version_ + 1",
                "delete_flag = case id_");
        assertThat(batch).doesNotContain("created_ =", "created_id =", "tenant_id =");

        String selectivePrimaryKey = render(table, new UpdateByPrimaryKeySelectiveElementGenerator());
        assertThat(selectivePrimaryKey)
                .doesNotContain("test=\"modified != null\"", "test=\"modifiedId != null\"",
                        "test=\"version != null\"");

        String selectiveExample = render(table, new UpdateByExampleSelectiveElementGenerator());
        assertThat(selectiveExample)
                .doesNotContain("test=\"record.modified != null\"", "test=\"record.modifiedId != null\"",
                        "test=\"record.version != null\"")
                .contains("version_ = version_ + 1");
    }

    @Test
    void auditFieldRecognitionHandlesUppercaseDatabaseMetadata() {
        List<IntrospectedColumn> columns = List.of(
                column("ID_", "id"),
                column("CREATED_", "created"),
                column("CREATED_ID", "createdId"),
                column("MODIFIED_", "modified"),
                column("MODIFIED_ID", "modifiedId"),
                column("TENANT_ID", "tenantId"),
                column("VERSION_", "version"),
                column("DELETE_FLAG", "deleteFlag"),
                column("STATE_", "state"));

        assertThat(ListUtilities.removeImmutableAuditColumns(columns))
                .extracting(IntrospectedColumn::getActualColumnName)
                .containsExactly("MODIFIED_", "MODIFIED_ID", "VERSION_", "DELETE_FLAG", "STATE_");
        assertThat(ListUtilities.isModifiedAuditColumn(columns.get(3))).isTrue();
        assertThat(ListUtilities.isModifiedAuditColumn(columns.get(4))).isTrue();
        assertThat(ListUtilities.isVersionColumn(columns.get(6))).isTrue();
    }

    @ParameterizedTest
    @MethodSource("supportedUpsertDialectGroups")
    void upsertDialectSnapshotsProtectUpdateFieldsAndKeepInsertFields(
            DatabaseType dbType, String updateStart, String updateEnd) throws Exception {
        var table = fixture(dbType);
        String xml = render(table, new InsertOrUpdateSelectiveElementGenerator());

        assertThat(xml).contains("created_", "created_id", "tenant_id",
                "<if test=\"created != null\">",
                "<if test=\"createdId != null\">",
                "<if test=\"tenantId != null\">",
                "<if test=\"version != null\">");
        String updateClause = xml.substring(xml.indexOf(updateStart));
        if (updateEnd != null) {
            updateClause = updateClause.substring(0, updateClause.indexOf(updateEnd));
        }
        boolean merge = dbType == DatabaseType.DM || dbType == DatabaseType.H2;
        String targetPrefix = merge ? "t." : "";
        assertThat(updateClause).contains(
                targetPrefix + "modified_ = " + (merge ? "s.modified" : "#{modified,jdbcType=VARCHAR}") + ",",
                targetPrefix + "modified_id = " + (merge ? "s.modifiedId" : "#{modifiedId,jdbcType=VARCHAR}") + ",",
                targetPrefix + "version_ = " + targetPrefix + "version_ + 1,",
                "<if test=\"deleteFlag != null\">",
                targetPrefix + "delete_flag = ");
        assertThat(updateClause).doesNotContain("created_", "created_id", "tenant_id");
    }

    @Test
    void phoenixUpsertFailsExplicitlyBecauseItCannotSeparateInsertAndUpdateAssignments() throws Exception {
        var table = fixture(DatabaseType.PHOENIX);

        assertThatThrownBy(() -> render(table, new InsertOrUpdateSelectiveElementGenerator()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Phoenix")
                .hasMessageContaining("无法区分插入与更新");
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> supportedUpsertDialectGroups() {
        return Stream.of(
                arguments(DatabaseType.MYSQL, "ON duplicate KEY UPDATE", null),
                arguments(DatabaseType.POSTGRE_SQL, "do update", null),
                arguments(DatabaseType.DM, "update set", "when not matched then insert"),
                arguments(DatabaseType.H2, "update set", "when not matched then insert"));
    }

    private static String render(IntrospectedTableMyBatis3Impl table, AbstractXmlElementGenerator generator) {
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        generator.setProgressCallback(new ProgressCallback() { });
        generator.setWarnings(new ArrayList<>());
        XmlElement root = new XmlElement("mapper");
        generator.addElements(root);
        Document document = new Document();
        document.setRootElement(root);
        XmlFormatter formatter = new DefaultXmlFormatter();
        formatter.setContext(table.getContext());
        return formatter.getFormattedContent(document);
    }

    private IntrospectedTableMyBatis3Impl fixture(DatabaseType dbType) throws Exception {
        String xml;
        try (var stream = getClass().getResourceAsStream("/dict-prefetch-generator.xml")) {
            assertThat(stream).as("离线生成配置资源").isNotNull();
            xml = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        Context context = new ConfigurationParser(new ArrayList<>()).parseConfiguration(new StringReader(xml))
                .getContexts().get(0);
        var pluginAggregatorField = Context.class.getDeclaredField("pluginAggregator");
        pluginAggregatorField.setAccessible(true);
        pluginAggregatorField.set(context, new PluginAggregator());
        var tableConfiguration = context.getTableConfigurations().get(0);
        tableConfiguration.getAdditionalDbTypes().clear();
        tableConfiguration.getAdditionalDbTypes().add(dbType);
        var table = (IntrospectedTableMyBatis3Impl) ObjectFactory.createIntrospectedTable(
                tableConfiguration, new FullyQualifiedTable(null, null, "article_", "Article",
                        null, true, null, null, null, false, null, context), context);
        table.setRemarks("文章");
        table.addColumn(column("id_", "id"));
        table.addColumn(column("created_", "created"));
        table.addColumn(column("created_id", "createdId"));
        table.addColumn(column("modified_", "modified"));
        table.addColumn(column("modified_id", "modifiedId"));
        table.addColumn(column("tenant_id", "tenantId"));
        table.addColumn(column("version_", "version"));
        table.addColumn(column("delete_flag", "deleteFlag"));
        table.addColumn(column("state_", "state"));
        table.addColumn(column("name_", "name"));
        table.addPrimaryKeyColumn("id_");
        table.initialize();
        table.setRules(new FlatModelRules(table));
        return table;
    }

    private static IntrospectedColumn column(String columnName, String property) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setActualColumnName(columnName);
        column.setJavaProperty(property);
        column.setFullyQualifiedJavaType(org.mybatis.generator.api.dom.java.FullyQualifiedJavaType.getStringInstance());
        column.setJdbcType(Types.VARCHAR);
        column.setJdbcTypeName("VARCHAR");
        column.setRemarks(property);
        return column;
    }
}
