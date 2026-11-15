package org.mybatis.generator.custom.annotations;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.ConditionParamConfiguration;
import org.mybatis.generator.config.ConditionTermConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.QueryColumnConfiguration;
import org.mybatis.generator.config.TableConfiguration;

import java.util.ArrayList;

import com.vgosoft.core.constant.enums.core.QueryConditionContainsModeEnum;
import com.vgosoft.core.constant.enums.core.QueryConditionOperatorEnum;
import com.vgosoft.core.constant.enums.core.QueryConditionValueTypeEnum;
import com.vgosoft.core.constant.enums.db.FieldTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link QueryConditionDesc} 注解输出与依赖表元数据的校验测试。
 *
 * @author vgosoft-agent
 */
class QueryConditionDescTest {

    @Test
    void shouldRenderConditionAnnotationWithParamsAndTerms() {
        QueryColumnConfiguration configuration = conditionConfig();
        QueryConditionDesc desc = QueryConditionDesc.create(configuration, table());
        desc.setOrder(3);
        String annotation = desc.toAnnotation();
        assertThat(annotation).contains("@QueryCondition(", "key = \"score_pass\"", "remark = \"成绩达标\"", "order = 3");
        assertThat(annotation).contains("@QueryConditionParam(name = \"minA\"",
                "fieldType = FieldTypeEnum.NUMBER", "tagName = HtmlElementTagTypeEnum.NUMBER");
        assertThat(annotation).contains("@QueryConditionParam(name = \"tags\"",
                "column = \"tags_\"", "multiple = true", "required = false");
        assertThat(annotation).contains("@QueryConditionTerm(column = \"score_a\", operator = QueryConditionOperatorEnum.GT, param = \"minA\")");
        assertThat(annotation).contains("operator = QueryConditionOperatorEnum.CONTAINS_ALL", "containsMode = QueryConditionContainsModeEnum.TEXT");
        assertThat(annotation).contains("value = \"0\", valueType = QueryConditionValueTypeEnum.NUMBER");
        assertThat(desc.getImportedTypes().stream().map(Object::toString).toList())
                .anySatisfy(i -> assertThat(i).contains("QueryCondition"));
    }

    @Test
    void shouldRejectKeyConflictingWithRealColumn() {
        QueryColumnConfiguration configuration = conditionConfig();
        configuration.setKey("score_a");
        assertThatThrownBy(() -> QueryConditionDesc.create(configuration, table()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("与真实列名冲突");
    }

    @Test
    void shouldRejectTermColumnMissingOrTypeHandled() {
        QueryColumnConfiguration configuration = conditionConfig();
        configuration.getConditionTermConfigurations().get(0).setColumn("ghost_");
        assertThatThrownBy(() -> QueryConditionDesc.create(configuration, table()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("未找到");

        QueryColumnConfiguration withHandler = conditionConfig();
        IntrospectedTableMyBatis3Impl table = table();
        table.getColumn("score_a").orElseThrow().setTypeHandler("com.example.MyHandler");
        assertThatThrownBy(() -> QueryConditionDesc.create(withHandler, table))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("typeHandler");

        QueryColumnConfiguration badBorrow = conditionConfig();
        badBorrow.getConditionParamConfigurations().get(0).setColumn("ghost_");
        assertThatThrownBy(() -> QueryConditionDesc.create(badBorrow, table()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("未找到");
    }

    private QueryColumnConfiguration conditionConfig() {
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration tc = new TableConfiguration(context, new ArrayList<>());
        tc.setTableName("record_");
        QueryColumnConfiguration configuration = new QueryColumnConfiguration(tc);
        configuration.setQueryType("condition");
        configuration.setKey("score_pass");
        configuration.setRemark("成绩达标");

        ConditionParamConfiguration minA = new ConditionParamConfiguration();
        minA.setName("minA");
        minA.setRemark("A 下限");
        minA.setFieldType(FieldTypeEnum.NUMBER);
        minA.setTagName(HtmlElementTagTypeEnum.NUMBER);
        ConditionParamConfiguration tags = new ConditionParamConfiguration();
        tags.setName("tags");
        tags.setColumn("tags_");
        tags.setMultiple(true);
        tags.setRequired(false);
        configuration.getConditionParamConfigurations().add(minA);
        configuration.getConditionParamConfigurations().add(tags);

        ConditionTermConfiguration gt = new ConditionTermConfiguration();
        gt.setColumn("score_a");
        gt.setOperator(QueryConditionOperatorEnum.GT);
        gt.setParam("minA");
        ConditionTermConfiguration contains = new ConditionTermConfiguration();
        contains.setColumn("tags_");
        contains.setOperator(QueryConditionOperatorEnum.CONTAINS_ALL);
        contains.setParam("tags");
        contains.setContainsMode(QueryConditionContainsModeEnum.TEXT);
        ConditionTermConfiguration constant = new ConditionTermConfiguration();
        constant.setColumn("score_b");
        constant.setOperator(QueryConditionOperatorEnum.GE);
        constant.setValue("0");
        constant.setValueType(QueryConditionValueTypeEnum.NUMBER);
        configuration.getConditionTermConfigurations().add(gt);
        configuration.getConditionTermConfigurations().add(contains);
        configuration.getConditionTermConfigurations().add(constant);
        return configuration;
    }

    private IntrospectedTableMyBatis3Impl table() {
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration tc = new TableConfiguration(context, new ArrayList<>());
        tc.setTableName("record_");
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(tc);
        table.getBaseColumns().add(column(context, "score_a", "scoreA"));
        table.getBaseColumns().add(column(context, "score_b", "scoreB"));
        table.getBaseColumns().add(column(context, "tags_", "tags"));
        return table;
    }

    private IntrospectedColumn column(Context context, String physical, String property) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setContext(context);
        column.setActualColumnName(physical);
        column.setJavaProperty(property);
        column.setColumnNameDelimited(true);
        column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
        column.setJdbcTypeName("VARCHAR");
        return column;
    }
}
