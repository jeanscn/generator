package org.mybatis.generator.plugins;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.QueryColumnConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VoGeneratorConfiguration;
import org.mybatis.generator.config.VoViewGeneratorConfiguration;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.internal.rules.ConditionalModelRules;

import java.sql.Types;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证普通查询项的注解输出兼容性。
 *
 * @author vgosoft-agent
 */
class ViewMetaAnnotationPluginTest {

    @Test
    void ordinaryQueryDoesNotReceiveConditionOrdering() {
        Context context = new Context(ModelType.CONDITIONAL);
        context.setId("test");
        context.setAppKeyword("test");
        try {
            var plugins = Context.class.getDeclaredField("pluginAggregator");
            plugins.setAccessible(true);
            plugins.set(context, new PluginAggregator());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        JavaModelGeneratorConfiguration modelConfiguration = new JavaModelGeneratorConfiguration(context);
        modelConfiguration.setTargetPackage("sample.pojo");
        modelConfiguration.setTargetProject("target/test-project");
        modelConfiguration.getProperties().setProperty("rootClass", "sample.AbstractRecord");
        context.setJavaModelGeneratorConfiguration(modelConfiguration);

        TableConfiguration tableConfiguration = new TableConfiguration(context, new ArrayList<>());
        tableConfiguration.setTableName("record_");
        tableConfiguration.setDomainObjectName("Record");
        tableConfiguration.setAlias("r");
        VoGeneratorConfiguration voGenerator = new VoGeneratorConfiguration(context, tableConfiguration);
        VoViewGeneratorConfiguration viewConfiguration =
                new VoViewGeneratorConfiguration(context, tableConfiguration);
        viewConfiguration.getQueryColumns().add("name_");
        QueryColumnConfiguration queryColumn = new QueryColumnConfiguration(tableConfiguration);
        queryColumn.setColumn("name_");
        viewConfiguration.addQueryColumnConfigurations(queryColumn);
        voGenerator.setVoViewConfiguration(viewConfiguration);
        tableConfiguration.setVoGeneratorConfiguration(voGenerator);

        IntrospectedTableMyBatis3Impl table = (IntrospectedTableMyBatis3Impl) ObjectFactory.createIntrospectedTable(
                tableConfiguration,
                new FullyQualifiedTable(null, null, "record_", "Record", null, true, null, null, null, false, null,
                        context),
                context);
        table.setBaseRecordType("sample.Record");
        table.setRemarks("记录");
        IntrospectedColumn column = new IntrospectedColumn();
        column.setContext(context);
        column.setActualColumnName("name_");
        column.setJavaProperty("name");
        column.setJdbcType(Types.VARCHAR);
        column.setJdbcTypeName("VARCHAR");
        column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
        column.setRemarks("名称");
        table.addColumn(column);
        table.initialize();
        table.setRules(new ConditionalModelRules(table) {
            @Override
            public boolean isGenerateViewVo() {
                return true;
            }
        });

        ViewMetaAnnotationPlugin plugin = new ViewMetaAnnotationPlugin();
        plugin.setContext(context);
        TopLevelClass viewVo = new TopLevelClass("sample.RecordViewVo");

        plugin.voModelViewClassGenerated(viewVo, table);

        assertThat(String.join("\n", viewVo.getAnnotations()))
                .contains("querys =")
                .doesNotContain("order = 1");
    }
}
