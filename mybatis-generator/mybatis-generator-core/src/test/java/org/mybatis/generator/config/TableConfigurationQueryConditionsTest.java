package org.mybatis.generator.config;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class TableConfigurationQueryConditionsTest {

    @Test
    void shouldKeepSelectedConditionQueryColumnDuringMetadataResolution() {
        Context context = new Context(ModelType.CONDITIONAL);
        JavaModelGeneratorConfiguration model = new JavaModelGeneratorConfiguration(context);
        model.setTargetPackage("sample.pojo");
        model.setTargetProject("target/test-project");
        context.setJavaModelGeneratorConfiguration(model);
        TableConfiguration tableConfiguration = new TableConfiguration(context, new ArrayList<>());
        tableConfiguration.setTableName("record_");

        VoGeneratorConfiguration vo = new VoGeneratorConfiguration(context, tableConfiguration);
        VoViewGeneratorConfiguration view = new VoViewGeneratorConfiguration(context, tableConfiguration);
        view.getQueryColumns().add("score_pass");

        QueryColumnConfiguration field = new QueryColumnConfiguration(tableConfiguration);
        field.setColumn("name_");
        QueryColumnConfiguration condition = new QueryColumnConfiguration(tableConfiguration);
        condition.setQueryType("condition");
        condition.setKey("score_pass");
        view.addQueryColumnConfigurations(field);
        view.addQueryColumnConfigurations(condition);

        vo.setVoViewConfiguration(view);
        tableConfiguration.setVoGeneratorConfiguration(vo);

        IntrospectedColumn name = new IntrospectedColumn();
        name.setContext(context);
        name.setActualColumnName("name_");
        name.setJavaProperty("name");
        name.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());

        IntrospectedTableMyBatis3Impl introspectedTable = new IntrospectedTableMyBatis3Impl();
        introspectedTable.setContext(context);
        introspectedTable.setTableConfiguration(tableConfiguration);
        introspectedTable.getBaseColumns().add(name);

        tableConfiguration.updateQueryColumnConfiguration(introspectedTable);

        assertThat(view.getQueryColumnConfigurations()).containsExactly(field, condition);
        assertThat(field.getIntrospectedColumn()).isSameAs(name);
        assertThat(condition.getIntrospectedColumn()).isNull();
        assertThat(tableConfiguration.hasQueryConditions()).isTrue();
    }
}
