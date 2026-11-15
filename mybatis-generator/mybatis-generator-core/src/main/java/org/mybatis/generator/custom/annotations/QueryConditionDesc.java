package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.QueryCondition;
import com.vgosoft.core.annotation.QueryConditionParam;
import com.vgosoft.core.annotation.QueryConditionTerm;
import com.vgosoft.core.constant.enums.core.QueryConditionContainsModeEnum;
import com.vgosoft.core.constant.enums.core.QueryConditionOperatorEnum;
import com.vgosoft.core.constant.enums.core.QueryConditionValueTypeEnum;
import com.vgosoft.core.constant.enums.db.FieldTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.config.ConditionParamConfiguration;
import org.mybatis.generator.config.ConditionTermConfiguration;
import org.mybatis.generator.config.QueryColumnConfiguration;

import java.util.List;
import java.util.stream.Collectors;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * {@code <queryColumn queryType="condition">} 对应的 {@code @QueryCondition} 注解描述，
 * 生成时负责依赖真实列的校验：key 不得与列名冲突、term 列必须存在且未配置自定义 typeHandler。
 *
 * @author vgosoft-agent
 */
@Getter
@Setter
public class QueryConditionDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@QueryCondition";

    private String key;
    private String remark;
    private String listKey;
    private int order;
    private List<ConditionParamConfiguration> params = List.of();
    private List<ConditionTermConfiguration> terms = List.of();

    /**
     * 由配置构建组合条件注解描述；结构约束已在 Parser 校验，此处校验依赖表元数据的约束。
     *
     * @param configuration          组合条件配置项
     * @param introspectedTable      当前内省表
     * @throws IllegalStateException key 与列名冲突、term 列不存在或配置了自定义 typeHandler
     */
    public static QueryConditionDesc create(QueryColumnConfiguration configuration, AbstractIntrospectedTable introspectedTable) {
        QueryConditionDesc desc = new QueryConditionDesc();
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        String key = configuration.getKey();
        if (introspectedTable.getColumn(key).isPresent()) {
            throw new IllegalStateException("表 " + tableName + " 的 queryColumn[" + key + "]：组合条件 key 不得与真实列名冲突");
        }
        for (ConditionTermConfiguration term : configuration.getConditionTermConfigurations()) {
            IntrospectedColumn column = introspectedTable.getColumn(term.getColumn())
                    .orElseThrow(() -> new IllegalStateException("表 " + tableName + " 的 queryColumn[" + key
                            + "]：conditionTerm 的 column 在表中未找到: " + term.getColumn()));
            if (org.mybatis.generator.internal.util.StringUtility.stringHasValue(column.getTypeHandler())) {
                throw new IllegalStateException("表 " + tableName + " 的 queryColumn[" + key
                        + "]：conditionTerm 的列配置了自定义 typeHandler，第一期不支持组合条件: " + term.getColumn());
            }
        }
        for (ConditionParamConfiguration param : configuration.getConditionParamConfigurations()) {
            if (stringHasValue(param.getColumn()) && introspectedTable.getColumn(param.getColumn()).isEmpty()) {
                throw new IllegalStateException("表 " + tableName + " 的 queryColumn[" + key
                        + "]：conditionParam 借用的 column 在表中未找到: " + param.getColumn());
            }
        }
        desc.key = key;
        desc.remark = configuration.getRemark();
        desc.listKey = configuration.getListKey();
        desc.order = configuration.getOrder();
        desc.params = configuration.getConditionParamConfigurations();
        desc.terms = configuration.getConditionTermConfigurations();
        desc.addImports(QueryCondition.class.getCanonicalName());
        return desc;
    }

    @Override
    public String toAnnotation() {
        this.items.add(VStringUtil.format("key = \"{0}\"", this.key));
        if (stringHasValue(this.listKey)) {
            this.items.add(VStringUtil.format("listKey = \"{0}\"", this.listKey));
        }
        if (stringHasValue(this.remark)) {
            this.items.add(VStringUtil.format("remark = \"{0}\"", this.remark));
        }
        if (this.order != 10) {
            this.items.add(VStringUtil.format("order = {0}", this.order));
        }
        if (!this.params.isEmpty()) {
            this.items.add(VStringUtil.format("params = '{'\n                {0}\n        '}'}'",
                    this.params.stream().map(this::paramToAnnotation).collect(Collectors.joining(",\n                "))));
            this.addImports(QueryConditionParam.class.getCanonicalName());
        }
        this.items.add(VStringUtil.format("terms = '{'\n                {0}\n        '}'}'",
                this.terms.stream().map(this::termToAnnotation).collect(Collectors.joining(",\n                "))));
        this.addImports(QueryConditionTerm.class.getCanonicalName());
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    private String paramToAnnotation(ConditionParamConfiguration param) {
        java.util.List<String> members = new java.util.ArrayList<>();
        members.add(VStringUtil.format("name = \"{0}\"", param.getName()));
        if (stringHasValue(param.getRemark())) {
            members.add(VStringUtil.format("remark = \"{0}\"", param.getRemark()));
        }
        if (stringHasValue(param.getColumn())) {
            members.add(VStringUtil.format("column = \"{0}\"", param.getColumn()));
        }
        if (param.getTagName() != null && param.getTagName() != HtmlElementTagTypeEnum.UNKNOWN) {
            members.add(VStringUtil.format("tagName = HtmlElementTagTypeEnum.{0}", param.getTagName().name()));
            this.addImports(HtmlElementTagTypeEnum.class.getCanonicalName());
        }
        if (param.getFieldType() != null && param.getFieldType() != FieldTypeEnum.TEXT) {
            members.add(VStringUtil.format("fieldType = FieldTypeEnum.{0}", param.getFieldType().name()));
            this.addImports(FieldTypeEnum.class.getCanonicalName());
        }
        if (stringHasValue(param.getDataUrl())) {
            members.add(VStringUtil.format("dataUrl = \"{0}\"", param.getDataUrl()));
        }
        if (param.isMultiple()) {
            members.add("multiple = true");
        }
        if (!param.isRequired()) {
            members.add("required = false");
        }
        return "@QueryConditionParam(" + String.join(", ", members) + ")";
    }

    private String termToAnnotation(ConditionTermConfiguration term) {
        java.util.List<String> members = new java.util.ArrayList<>();
        members.add(VStringUtil.format("column = \"{0}\"", term.getColumn()));
        members.add(VStringUtil.format("operator = QueryConditionOperatorEnum.{0}", term.getOperator().name()));
        this.addImports(QueryConditionOperatorEnum.class.getCanonicalName());
        if (term.hasParam()) {
            members.add(VStringUtil.format("param = \"{0}\"", term.getParam()));
        }
        if (term.hasValue()) {
            members.add(VStringUtil.format("value = \"{0}\"", term.getValue()));
            members.add(VStringUtil.format("valueType = QueryConditionValueTypeEnum.{0}", term.getValueType().name()));
            this.addImports(QueryConditionValueTypeEnum.class.getCanonicalName());
        }
        if (term.getContainsMode() != null && term.getContainsMode() != QueryConditionContainsModeEnum.TOKEN) {
            members.add(VStringUtil.format("containsMode = QueryConditionContainsModeEnum.{0}", term.getContainsMode().name()));
            this.addImports(QueryConditionContainsModeEnum.class.getCanonicalName());
        }
        return "@QueryConditionTerm(" + String.join(", ", members) + ")";
    }
}
