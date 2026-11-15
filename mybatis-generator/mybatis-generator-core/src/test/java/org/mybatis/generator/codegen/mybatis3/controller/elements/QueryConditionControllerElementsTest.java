package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.QueryColumnConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VoGeneratorConfiguration;
import org.mybatis.generator.config.VoRequestGeneratorConfiguration;
import org.mybatis.generator.config.VoViewGeneratorConfiguration;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.internal.rules.ConditionalModelRules;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * list/export 生成语句中组合条件应用块与空 Criteria 保护行的测试。
 *
 * @author vgosoft-agent
 */
class QueryConditionControllerElementsTest {

    @Test
    void listShouldGuardEmptyCriteriaAndApplyResolvedConditions() {
        ListFixture fixture = listFixture(true);
        fixture.generator.addElements(fixture.output);
        Method post = method(fixture.output, "listPostRecord");
        String body = String.join("\n", post.getBodyLines());
        int buildExample = body.indexOf("buildExample");
        assertThat(body.indexOf("if (example.getOredCriteria().isEmpty()) { example.createCriteria(); }"))
                .isGreaterThan(buildExample)
                .isLessThan(body.indexOf("getFilterParam()"));
        assertThat(body).contains(
                "List<BoundCondition> conditions = QueryConditionResolver.resolve(RecordViewVo.class, filterParam.getConditions(), \"r\")",
                "example.getOredCriteria().forEach(criteria -> conditions.forEach(bound -> {",
                "case VALUE -> criteria.andBoundCondition(bound.condition(), bound.value());",
                "case LITERAL_LIKE -> criteria.andLiteralLike(bound.condition(), (String) bound.value(), bound.sqlServerValue());",
                "case TOKEN_LIKE -> criteria.andTokenLike(bound.condition(), (String) bound.value(), bound.sqlServerValue());",
                "default -> throw new IllegalStateException(");
        assertThat(body.indexOf("andTrustedCondition(filterSql)"))
                .isLessThan(body.indexOf("QueryConditionResolver.resolve"));
        assertThat(fixture.output.getImportedTypes())
                .anySatisfy(i -> assertThat(i.getFullyQualifiedName()).isEqualTo("com.vgosoft.web.utils.QueryConditionResolver"))
                .anySatisfy(i -> assertThat(i.getFullyQualifiedName()).isEqualTo("com.vgosoft.web.utils.BoundCondition"))
                .anySatisfy(i -> assertThat(i.getFullyQualifiedName()).isEqualTo("sample.Support.RecordViewVo"));
    }

    @Test
    void listShouldNotEmitConditionsWhenViewVoDisabled() {
        ListFixture fixture = listFixture(false);
        fixture.generator.addElements(fixture.output);
        String body = String.join("\n", method(fixture.output, "listPostRecord").getBodyLines());
        assertThat(body).doesNotContain("QueryConditionResolver", "BoundCondition");
        assertThat(body).contains("if (example.getOredCriteria().isEmpty()) { example.createCriteria(); }");
    }

    @Test
    void exportShouldApplyResolvedConditionsAfterFilterParam() {
        ExportFixture fixture = exportFixture(true);
        fixture.generator.addElements(fixture.output);
        String body = String.join("\n", method(fixture.output, "exportPostRecord").getBodyLines());
        assertThat(body).contains(
                "List<BoundCondition> conditions = QueryConditionResolver.resolve(RecordViewVo.class, filterParam.getConditions(), \"r\")",
                "example.getOredCriteria().forEach(criteria -> conditions.forEach(bound -> {");
        assertThat(body.indexOf("andTrustedCondition(filterSql)"))
                .isLessThan(body.indexOf("QueryConditionResolver.resolve"));
        assertThat(body.indexOf("QueryConditionResolver.resolve"))
                .isLessThan(body.indexOf("if (idScope)"));
    }

    @Test
    void exportShouldNotEmitConditionsWhenViewVoDisabled() {
        ExportFixture fixture = exportFixture(false);
        fixture.generator.addElements(fixture.output);
        String body = String.join("\n", method(fixture.output, "exportPostRecord").getBodyLines());
        assertThat(body).doesNotContain("QueryConditionResolver", "BoundCondition");
    }

    private static Method method(TopLevelClass output, String name) {
        return output.getMethods().stream().filter(m -> m.getName().equals(name))
                .findFirst().orElseThrow();
    }

    private static ListFixture listFixture(boolean viewVo) {
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration configuration = tableConfiguration(context, viewVo);
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(configuration);
        table.setBaseRecordType("sample.Support.Record");
        table.setExampleType("sample.Support.RecordExample");
        table.setRules(new ConditionalModelRules(table) {
            @Override
            public boolean isGenerateRequestVo() { return true; }

            @Override
            public boolean isGenerateViewVo() { return viewVo; }

            @Override
            public boolean isIntegrateSpringSecurity() { return false; }
        });
        table.getBaseColumns().add(column(context, "name_", "name"));
        ListElementGenerator generator = new ListElementGenerator();
        generator.setContext(context);
        generator.setIntrospectedTable(table);
        generator.setEntityType(new FullyQualifiedJavaType("sample.Support.Record"));
        generator.setExampleType(new FullyQualifiedJavaType("sample.Support.RecordExample"));
        generator.setEntityVoType(new FullyQualifiedJavaType("sample.Support.RecordVo"));
        generator.setEntityViewVoType(new FullyQualifiedJavaType("sample.Support.RecordViewVo"));
        generator.setEntityRequestVoType(new FullyQualifiedJavaType("sample.Support.RecordRequestVo"));
        generator.setEntityMappings(new FullyQualifiedJavaType("sample.Support.RecordMappings"));
        generator.setServiceBeanName("service");
        generator.setCommentGenerator(new DefaultCommentGenerator());
        return new ListFixture(context, table, generator, new TopLevelClass("sample.Controller"));
    }

    private static ExportFixture exportFixture(boolean viewVo) {
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration configuration = tableConfiguration(context, viewVo);
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(configuration);
        table.setBaseRecordType("sample.Support.Record");
        table.setExampleType("sample.Support.RecordExample");
        table.setRules(new ConditionalModelRules(table) {
            @Override
            public boolean isGenerateRequestVo() { return true; }

            @Override
            public boolean isGenerateViewVo() { return viewVo; }

            @Override
            public boolean isIntegrateSpringSecurity() { return false; }
        });
        IntrospectedColumn key = column(context, "record_key", "recordKey");
        table.getPrimaryKeyColumns().add(key);
        table.getBaseColumns().add(column(context, "name_", "name"));
        table.getTopLevelClassExampleFields().put("RecordRequestVo", List.of("ignorePermissionAnnotation",
                "ignoreDeleteFlag", "anyWhereCondition", "orderByClause", "filterParam"));
        ExportElementGenerator generator = new ExportElementGenerator();
        generator.setContext(context);
        generator.setIntrospectedTable(table);
        generator.setEntityType(new FullyQualifiedJavaType("sample.Support.Record"));
        generator.setExampleType(new FullyQualifiedJavaType("sample.Support.RecordExample"));
        generator.setEntityExcelVoType(new FullyQualifiedJavaType("sample.Support.RecordExcelVo"));
        generator.setEntityViewVoType(new FullyQualifiedJavaType("sample.Support.RecordViewVo"));
        generator.setEntityRequestVoType(new FullyQualifiedJavaType("sample.Support.RecordRequestVo"));
        generator.setServiceBeanName("service");
        generator.setCommentGenerator(new DefaultCommentGenerator());
        generator.setWarnings(new ArrayList<>());
        return new ExportFixture(context, table, generator, new TopLevelClass("sample.Controller"));
    }

    private static TableConfiguration tableConfiguration(Context context, boolean viewVo) {
        try {
            var plugins = Context.class.getDeclaredField("pluginAggregator");
            plugins.setAccessible(true);
            plugins.set(context, new PluginAggregator());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        JavaModelGeneratorConfiguration model = new JavaModelGeneratorConfiguration(context);
        model.setTargetPackage("sample.pojo");
        model.setTargetProject("target/test-project");
        context.setJavaModelGeneratorConfiguration(model);
        TableConfiguration configuration = new TableConfiguration(context, new ArrayList<>());
        configuration.setTableName("record_");
        configuration.setDomainObjectName("Record");
        configuration.setAlias("r");
        VoGeneratorConfiguration voGenerator = new VoGeneratorConfiguration(context, configuration);
        VoRequestGeneratorConfiguration request = new VoRequestGeneratorConfiguration(context, configuration);
        request.setIncludePageParam(false);
        voGenerator.setVoRequestConfiguration(request);
        if (viewVo) {
            VoViewGeneratorConfiguration view = new VoViewGeneratorConfiguration(context, configuration);
            view.getQueryColumns().add("score_pass");
            QueryColumnConfiguration condition = new QueryColumnConfiguration(configuration);
            condition.setQueryType("condition");
            condition.setKey("score_pass");
            view.addQueryColumnConfigurations(condition);
            voGenerator.setVoViewConfiguration(view);
        }
        configuration.setVoGeneratorConfiguration(voGenerator);
        return configuration;
    }

    private static IntrospectedColumn column(Context context, String physical, String property) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setContext(context);
        column.setActualColumnName(physical);
        column.setJavaProperty(property);
        column.setTableAlias("r");
        column.setColumnNameDelimited(true);
        column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
        column.setJdbcTypeName("VARCHAR");
        return column;
    }

    private record ListFixture(Context context, IntrospectedTableMyBatis3Impl table,
                               ListElementGenerator generator, TopLevelClass output) {}

    private record ExportFixture(Context context, IntrospectedTableMyBatis3Impl table,
                                 ExportElementGenerator generator, TopLevelClass output) {}
}
