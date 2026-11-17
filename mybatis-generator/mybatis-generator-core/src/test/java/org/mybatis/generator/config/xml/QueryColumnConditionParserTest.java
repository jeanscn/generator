package org.mybatis.generator.config.xml;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.config.ConditionParamConfiguration;
import org.mybatis.generator.config.ConditionOptionConfiguration;
import org.mybatis.generator.config.ConditionTermConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.QueryColumnConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Properties;

import com.vgosoft.core.constant.enums.core.QueryConditionContainsModeEnum;
import com.vgosoft.core.constant.enums.core.QueryConditionOperatorEnum;
import com.vgosoft.core.constant.enums.core.QueryConditionValueTypeEnum;
import com.vgosoft.core.constant.enums.db.FieldTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code <queryColumn queryType="condition">} 配置解析与结构校验测试。
 *
 * @author vgosoft-agent
 */
class QueryColumnConditionParserTest {

    @Test
    void shouldParseFieldQueryColumnAsBefore() throws Exception {
        QueryColumnConfiguration configuration = parse("<queryColumn column=\"state_\" remark=\"状态\" tagType=\"select\" queryMode=\"EqualTo\"/>");
        assertThat(configuration.isCondition()).isFalse();
        assertThat(configuration.getColumn()).isEqualTo("state_");
        assertThat(configuration.getRemark()).isEqualTo("状态");
    }

    @Test
    void shouldRejectFieldQueryColumnWithoutColumn() {
        assertThatThrownBy(() -> parse("<queryColumn remark=\"状态\"/>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("column 必填");
    }

    @Test
    void shouldRejectUnknownQueryType() {
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"group\" column=\"state_\"/>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("queryType");
    }

    @Test
    void shouldRejectFieldWithKeyOrConditionChildren() {
        assertThatThrownBy(() -> parse("<queryColumn column=\"state_\" key=\"k\"/>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key");
        assertThatThrownBy(() -> parse("<queryColumn column=\"state_\">"
                + "<conditionTerm column=\"score_a\" operator=\"EqualTo\" value=\"1\" valueType=\"number\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("conditionParam/conditionTerm");
    }

    @Test
    void shouldParseConditionWithParamsAndTerms() throws Exception {
        QueryColumnConfiguration configuration = parse("<queryColumn queryType=\"condition\" key=\"score_pass\" remark=\"成绩达标\">"
                + "<conditionParam name=\"minA\" remark=\"A 下限\" fieldType=\"number\"/>"
                + "<conditionParam name=\"tags\" column=\"tags_\" multiple=\"true\" required=\"false\"/>"
                + "<conditionTerm column=\"score_a\" operator=\"GreaterThan\" param=\"minA\"/>"
                + "<conditionTerm column=\"tags_\" operator=\"ContainsAll\" param=\"tags\" containsMode=\"Text\"/>"
                + "<conditionTerm column=\"score_b\" operator=\"GreaterThanOrEqualTo\" value=\"0\" valueType=\"number\"/>"
                + "</queryColumn>");
        assertThat(configuration.isCondition()).isTrue();
        assertThat(configuration.getKey()).isEqualTo("score_pass");
        assertThat(configuration.getConditionParamConfigurations()).hasSize(2);
        ConditionParamConfiguration tags = configuration.getConditionParamConfigurations().get(1);
        assertThat(tags.getColumn()).isEqualTo("tags_");
        assertThat(tags.isMultiple()).isTrue();
        assertThat(tags.isRequired()).isFalse();
        assertThat(configuration.getConditionTermConfigurations()).hasSize(3);
        ConditionTermConfiguration token = configuration.getConditionTermConfigurations().get(1);
        assertThat(token.getOperator()).isEqualTo(QueryConditionOperatorEnum.CONTAINS_ALL);
        assertThat(token.getContainsMode()).isEqualTo(QueryConditionContainsModeEnum.TEXT);
        ConditionTermConfiguration constant = configuration.getConditionTermConfigurations().get(2);
        assertThat(constant.getValue()).isEqualTo("0");
        assertThat(constant.getValueType()).isEqualTo(QueryConditionValueTypeEnum.NUMBER);
    }

    @Test
    void shouldRejectConditionWithoutKeyOrTerms() {
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\">"
                + "<conditionTerm column=\"score_a\" operator=\"EqualTo\" value=\"1\" valueType=\"number\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key 必填");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\"/>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("conditionTerm");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\" column=\"state_\">"
                + "<conditionTerm column=\"score_a\" operator=\"EqualTo\" value=\"1\" valueType=\"number\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不配置 column");
    }

    @Test
    void shouldRejectTermWithBothOrNeitherParamAndValue() {
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionParam name=\"p\"/>"
                + "<conditionTerm column=\"a\" operator=\"EqualTo\" param=\"p\" value=\"1\" valueType=\"number\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("必须且只能配置一个");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionTerm column=\"a\" operator=\"EqualTo\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("必须且只能配置一个");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionTerm column=\"a\" operator=\"EqualTo\" param=\"ghost\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未声明的参数");
    }

    @Test
    void shouldRejectConstantMisuse() {
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionTerm column=\"a\" operator=\"EqualTo\" value=\"1\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valueType");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionParam name=\"p\" multiple=\"true\"/>"
                + "<conditionTerm column=\"a\" operator=\"In\" value=\"1\" valueType=\"number\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("常量值不支持多值操作符");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionTerm column=\"a\" operator=\"EqualTo\" value=\"abc\" valueType=\"number\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不是合法数值");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionParam name=\"p\"/>"
                + "<conditionTerm column=\"a\" operator=\"EqualTo\" param=\"p\" valueType=\"string\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不得配置 valueType");
    }

    @Test
    void shouldRejectMultipleMismatchAndNonTextLikeParam() {
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionParam name=\"p\"/>"
                + "<conditionTerm column=\"a\" operator=\"In\" param=\"p\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("多值设置与操作符不匹配");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionParam name=\"p\" multiple=\"true\"/>"
                + "<conditionTerm column=\"a\" operator=\"EqualTo\" param=\"p\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("多值设置与操作符不匹配");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionParam name=\"p\" fieldType=\"number\"/>"
                + "<conditionTerm column=\"a\" operator=\"LikeAny\" param=\"p\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("只允许文本参数");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionParam name=\"p\" fieldType=\"datetime\"/>"
                + "<conditionTerm column=\"a\" operator=\"LikeAny\" param=\"p\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("只允许文本参数");
    }

    @Test
    void shouldRejectContainsModeOnNonContainsAll() {
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionParam name=\"p\"/>"
                + "<conditionTerm column=\"a\" operator=\"LikeAny\" param=\"p\" containsMode=\"Text\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("containsMode");
    }

    @Test
    void shouldRejectUnknownOperatorContainsModeAndValueType() {
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionParam name=\"p\"/>"
                + "<conditionTerm column=\"a\" operator=\"Regexp\" param=\"p\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("operator");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionParam name=\"p\" multiple=\"true\"/>"
                + "<conditionTerm column=\"a\" operator=\"ContainsAll\" param=\"p\" containsMode=\"Fuzzy\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("containsMode");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionTerm column=\"a\" operator=\"EqualTo\" value=\"1\" valueType=\"date\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valueType");
    }

    @Test
    void shouldAcceptTextParamAndNonMultipleFieldType() throws Exception {
        QueryColumnConfiguration configuration = parse("<queryColumn queryType=\"condition\" key=\"k\">"
                + "<conditionParam name=\"p\" fieldType=\"select\"/>"
                + "<conditionTerm column=\"a\" operator=\"LikeAny\" param=\"p\"/>"
                + "</queryColumn>");
        assertThat(configuration.getConditionParamConfigurations().get(0).getFieldType())
                .isEqualTo(FieldTypeEnum.SELECT);
    }

    // 验证选项型条件能够解析清除选项、常量谓词和默认控件配置。
    @Test
    void shouldParseConditionOptionsAndApplySelectDefaults() throws Exception {
        QueryColumnConfiguration configuration = parse("<queryColumn queryType=\"condition\" key=\"state_filter\">"
                + "<conditionOption value=\"all\" remark=\"全部\" clear=\"true\"/>"
                + "<conditionOption value=\"active\" remark=\"启用\">"
                + "<conditionTerm column=\"state_\" operator=\"EqualTo\" value=\"1\" valueType=\"number\"/>"
                + "</conditionOption></queryColumn>");

        assertThat(configuration.getTagName()).isEqualTo(HtmlElementTagTypeEnum.SELECT);
        assertThat(configuration.isMultiple()).isFalse();
        assertThat(configuration.getConditionOptionConfigurations()).hasSize(2);
        ConditionOptionConfiguration clear = configuration.getConditionOptionConfigurations().get(0);
        assertThat(clear.getValue()).isEqualTo("all");
        assertThat(clear.getRemark()).isEqualTo("全部");
        assertThat(clear.isClear()).isTrue();
        assertThat(clear.getConditionTermConfigurations()).isEmpty();
        ConditionOptionConfiguration active = configuration.getConditionOptionConfigurations().get(1);
        assertThat(active.getConditionTermConfigurations()).singleElement()
                .extracting(ConditionTermConfiguration::getValue).isEqualTo("1");
    }

    // 验证选项值、清除选项、谓词内容及配置模式互斥规则。
    @Test
    void shouldRejectInvalidConditionOptionStructure() {
        assertInvalidOptions("<conditionOption value=\"active\"><conditionTerm column=\"state_\" operator=\"EqualTo\" value=\"1\" valueType=\"number\"/></conditionOption>",
                "恰好配置一个 clear=true");
        assertInvalidOptions("<conditionOption value=\"all\" clear=\"true\"/><conditionOption value=\"all2\" clear=\"true\"/>"
                        + "<conditionOption value=\"active\"><conditionTerm column=\"state_\" operator=\"EqualTo\" value=\"1\" valueType=\"number\"/></conditionOption>",
                "恰好配置一个 clear=true");
        assertInvalidOptions("<conditionOption value=\"all\" clear=\"true\"/><conditionOption value=\"\"/>",
                "value 必填且不能为空");
        assertInvalidOptions("<conditionOption value=\"all\" clear=\"true\"/><conditionOption value=\"active\">"
                        + "<conditionTerm column=\"state_\" operator=\"EqualTo\" value=\"1\" valueType=\"number\"/>"
                        + "</conditionOption><conditionOption value=\"active\">"
                        + "<conditionTerm column=\"state_\" operator=\"EqualTo\" value=\"0\" valueType=\"number\"/>"
                        + "</conditionOption>",
                "value 重复");
        assertInvalidOptions("<conditionOption value=\"all\" clear=\"true\"><conditionTerm column=\"state_\" operator=\"EqualTo\" value=\"1\" valueType=\"number\"/></conditionOption>"
                        + "<conditionOption value=\"active\"><conditionTerm column=\"state_\" operator=\"EqualTo\" value=\"1\" valueType=\"number\"/></conditionOption>",
                "clear=true");
        assertInvalidOptions("<conditionOption value=\"all\" clear=\"true\"/><conditionOption value=\"active\"/>",
                "普通 conditionOption 至少需要一个 conditionTerm");
        assertInvalidOptions("<conditionOption value=\"all\" clear=\"true\"/><conditionOption value=\"active\">"
                        + "<conditionTerm column=\"state_\" operator=\"EqualTo\" param=\"state\"/>"
                        + "</conditionOption>",
                "不能使用 param");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"state_filter\">"
                + "<conditionOption value=\"all\" clear=\"true\"/><conditionOption value=\"active\">"
                + "<conditionTerm column=\"state_\" operator=\"EqualTo\" value=\"1\" valueType=\"number\"/>"
                + "</conditionOption><conditionTerm column=\"state_\" operator=\"EqualTo\" value=\"0\" valueType=\"number\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("互斥");
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"state_filter\">"
                + "<conditionParam name=\"state\"/><conditionOption value=\"all\" clear=\"true\"/>"
                + "<conditionOption value=\"active\"><conditionTerm column=\"state_\" operator=\"EqualTo\" value=\"1\" valueType=\"number\"/>"
                + "</conditionOption></queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("互斥");

        StringBuilder options = new StringBuilder("<conditionOption value=\"all\" clear=\"true\"/>");
        for (int i = 0; i < 21; i++) {
            options.append("<conditionOption value=\"v").append(i).append("\">")
                    .append("<conditionTerm column=\"state_\" operator=\"EqualTo\" value=\"")
                    .append(i).append("\" valueType=\"number\"/></conditionOption>");
        }
        assertInvalidOptions(options.toString(), "最多允许 20 个");
    }

    // 验证选项控件对单选、多选及 switch 选项数量的约束。
    @Test
    void shouldApplyConditionOptionControlCardinalityRules() throws Exception {
        QueryColumnConfiguration radio = parse(optionQuery("tagType=\"radio\"", ""));
        assertThat(radio.getTagName()).isEqualTo(HtmlElementTagTypeEnum.RADIO);
        assertThat(radio.isMultiple()).isFalse();
        assertThat(parse(optionQuery("tagType=\"radio\" multiple=\"false\"", "")).isMultiple()).isFalse();
        assertThatThrownBy(() -> parse(optionQuery("tagType=\"radio\" multiple=\"true\"", "")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("radio");

        assertThat(parse(optionQuery("tagType=\"select\" multiple=\"true\"", "")).isMultiple()).isTrue();
        assertThat(parse(optionQuery("tagType=\"dropdownlist\" multiple=\"true\"", "")).isMultiple()).isTrue();
        assertThat(parse(optionQuery("tagType=\"checkbox\"", "")).isMultiple()).isTrue();
        assertThat(parse(optionQuery("tagType=\"checkbox\" multiple=\"true\"", "")).isMultiple()).isTrue();
        assertThatThrownBy(() -> parse(optionQuery("tagType=\"checkbox\" multiple=\"false\"", "")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checkbox");

        QueryColumnConfiguration switchConfiguration = parse(optionQuery("tagType=\"switch\"", ""));
        assertThat(switchConfiguration.getConditionOptionConfigurations()).hasSize(2);
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"k\" tagType=\"switch\">"
                + "<conditionOption value=\"all\" clear=\"true\"/></queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("switch");
        assertThatThrownBy(() -> parse(optionQuery("tagType=\"switch\" multiple=\"true\"", "")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("multiple=true");
    }

    // 验证选项型条件不接受渲染控件集合之外的 tagType，与后端 resolveOptionControl 支持集合一致。
    @Test
    void shouldRejectUnsupportedOptionControlTagType() {
        for (String tag : new String[]{"input", "date", "number", "title", "cascader", "checkboxGroup"}) {
            assertThatThrownBy(() -> parse(optionQuery("tagType=\"" + tag + "\"", "")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("不支持的 tagType");
        }
    }

    // 验证选项型条件不能用于普通字段查询项。
    @Test
    void shouldRejectConditionOptionOnFieldQueryColumn() {
        assertThatThrownBy(() -> parse("<queryColumn column=\"state_\"><conditionOption value=\"all\" clear=\"true\"/>"
                + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field 项不支持");
    }

    private void assertInvalidOptions(String optionXml, String message) {
        assertThatThrownBy(() -> parse("<queryColumn queryType=\"condition\" key=\"state_filter\">"
                + optionXml + "</queryColumn>"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(message);
    }

    private String optionQuery(String attributes, String ignored) {
        return "<queryColumn queryType=\"condition\" key=\"k\" " + attributes + ">"
                + "<conditionOption value=\"all\" clear=\"true\"/>"
                + "<conditionOption value=\"active\"><conditionTerm column=\"state_\" operator=\"EqualTo\" "
                + "value=\"1\" valueType=\"number\"/></conditionOption></queryColumn>";
    }

    private QueryColumnConfiguration parse(String xml) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
        Element element = document.getDocumentElement();
        MyBatisGeneratorConfigurationParser parser =
                new MyBatisGeneratorConfigurationParser(new Properties(), new ArrayList<>());
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration tc = new TableConfiguration(context, new ArrayList<>());
        tc.setTableName("record_");
        return parser.parseQueryColumn(tc, element);
    }
}
