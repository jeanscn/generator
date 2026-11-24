package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.constant.enums.view.ValidatorRuleTypeEnum;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.codegen.mybatis3.vue.VueFormGenerateUtil;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlValidatorElementConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link VueFormItemRuleDesc} 校验类型派生测试。
 * <p>
 * checkbox/checkboxGroup 在前端表单模型中固定以数组存值（perOnload 拆分数组、
 * preSubmit 合并逗号串），其校验类型必须派生为 array，不能沿用列 Java 类型；
 * 否则生成 type="string" 规则会使数组取值永远无法通过表单校验。
 *
 * @author vgosoft-agent
 */
class VueFormItemRuleDescTest {

    /**
     * checkboxGroup 元素：TYPE 规则应派生为数组类型及对应提示。
     */
    @Test
    void shouldDeriveArrayTypeForCheckboxGroup() {
        String annotation = typeRule(stringColumn(), descriptor("checkboxGroup"));

        assertThat(annotation).isEqualTo(
                "@VueFormItemRule(type = \"array\", message = \"授权内容类型错误，应该是一组数据\", trigger = \"blur\")");
    }

    /**
     * checkbox 元素与 checkboxGroup 同为数组取值，校验类型同样派生为 array。
     */
    @Test
    void shouldDeriveArrayTypeForCheckbox() {
        String annotation = typeRule(stringColumn(), descriptor("checkbox"));

        assertThat(annotation).contains("type = \"array\"");
    }

    /**
     * 输入框元素：校验类型仍按列 Java 类型派生。
     */
    @Test
    void shouldKeepColumnTypeForInput() {
        String annotation = typeRule(stringColumn(), descriptor("input"));

        assertThat(annotation).contains("type = \"string\"")
                .contains("授权内容类型错误，存在无效文本");
    }

    /**
     * 无元素描述时保持列类型派生。
     */
    @Test
    void shouldKeepColumnTypeWithoutDescriptor() {
        String annotation = typeRule(stringColumn(), null);

        assertThat(annotation).contains("type = \"string\"");
    }

    /**
     * 显式 validator type 配置优先于组件派生。
     */
    @Test
    void shouldHonorExplicitValidatorType() {
        HtmlElementDescriptor descriptor = descriptor("checkboxGroup");
        HtmlValidatorElementConfiguration config = new HtmlValidatorElementConfiguration();
        config.setType("string");
        descriptor.getHtmlValidatorElementConfigurations().add(config);

        String annotation = typeRule(stringColumn(), descriptor);

        assertThat(annotation).contains("type = \"string\"");
    }

    /**
     * 端到端断言：checkboxGroup 字符串列只输出 array 类型规则，
     * 不再生成按列长度的 string max 规则（数组取值下该规则必然校验失败）。
     */
    @Test
    void shouldEmitOnlyArrayRuleForCheckboxGroupColumn() {
        String rules = VueFormGenerateUtil.getRules(stringColumn(), descriptor("checkboxGroup"), null, 1);

        assertThat(rules).contains("type = \"array\"")
                .doesNotContain("type = \"string\"", "max = \"2000\"");
    }

    private String typeRule(IntrospectedColumn column, HtmlElementDescriptor descriptor) {
        return new VueFormItemRuleDesc(ValidatorRuleTypeEnum.TYPE, 1, column, descriptor, null).toAnnotation();
    }

    private IntrospectedColumn stringColumn() {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setActualColumnName("delegate_content");
        column.setJavaProperty("delegateContent");
        column.setRemarks("授权内容");
        column.setNullable(true);
        column.setLength(2000);
        column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
        return column;
    }

    private HtmlElementDescriptor descriptor(String tagType) {
        HtmlElementDescriptor descriptor = new HtmlElementDescriptor();
        descriptor.setTagType(tagType);
        return descriptor;
    }
}
