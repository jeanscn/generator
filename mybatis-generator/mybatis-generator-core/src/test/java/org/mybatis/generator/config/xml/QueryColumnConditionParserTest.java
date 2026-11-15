package org.mybatis.generator.config.xml;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.config.ConditionParamConfiguration;
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
