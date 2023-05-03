package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.AbstractHtmlElementGenerator;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.StringUtility.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 13:47
 * @version 3.0
 */
public abstract class AbstractLayuiElementGenerator extends AbstractHtmlElementGenerator {

    protected AbstractLayuiElementGenerator() {
    }

    protected AbstractLayuiElementGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        super(context, introspectedTable, warnings, progressCallback);
    }

    protected AbstractLayuiElementGenerator(GeneratorInitialParameters generatorInitialParameters) {
        super(generatorInitialParameters);
    }

    @Override
    public abstract void addHtmlElement(IntrospectedColumn introspectedColumn, HtmlElement parent);

    @Override
    public abstract String getFieldValueFormatPattern(IntrospectedColumn introspectedColumn);

    protected void addElementRequired(String columnName, HtmlElement element, HtmlElementDescriptor htmlElementDescriptor) {
        IntrospectedColumn column = introspectedTable.getColumn(columnName).orElse(null);
        if (column == null ||
                (htmlElementDescriptor != null && htmlElementDescriptor.getVerify() != null && htmlElementDescriptor.getVerify().contains("none"))) {
            return;
        }
        String verify = "";
        //生成指定验证
        if (htmlElementDescriptor != null) {
            verify = stringHasValue(htmlElementDescriptor.getVerify()) ? htmlElementDescriptor.getVerify() : "";
        }
        //非空
        if (htmlGeneratorConfiguration != null) {
            List<String> htmlElementInputRequired = htmlGeneratorConfiguration.getElementRequired();
            if (htmlElementInputRequired.contains(columnName) || (!column.isNullable())) {
                verify += stringHasValue(verify) ? "|required" : "required";
            }
        } else {
            if (!column.isNullable()) {
                verify += stringHasValue(verify) ? "|required" : "required";
            }
        }
        //长度
        if (column.getLength() > 0) {
            verify += stringHasValue(verify) ? "|limit" : "limit";
        }
        //日期类型
        if (column.getJdbcTypeName().equalsIgnoreCase("DATE")) {
            verify += stringHasValue(verify) ? "|date" : "date";
        }
        //时间类型
        if (column.getJdbcTypeName().equalsIgnoreCase("TIME")) {
            verify += stringHasValue(verify) ? "|time" : "time";
        }
        //日期时间类型
        if (column.getJdbcTypeName().equalsIgnoreCase("TIMESTAMP")) {
            verify += stringHasValue(verify) ? "|datetime" : "datetime";
        }

        //数字类型
        if (column.getJdbcTypeName().equalsIgnoreCase("DECIMAL")
                || column.getJdbcTypeName().equalsIgnoreCase("NUMERIC")
                || column.getJdbcTypeName().equalsIgnoreCase("BIGINT")
                || column.getJdbcTypeName().equalsIgnoreCase("INTEGER")
                || column.getJdbcTypeName().equalsIgnoreCase("INT")) {
            verify += stringHasValue(verify) ? "|number" : "number";
        }
        //转list、去重、去空
        List<String> stringList = Arrays.stream(verify.split("\\|"))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        //计算长度限制
        int maxLength = column.getLength();

        int minLength = column.getMinLength() > 0
                ? column.getMinLength()
                : (stringList.contains("required") ? 1 : 0);
        if (maxLength > 0 && maxLength != 255) {
            element.addAttribute(new Attribute("max-length", String.valueOf(maxLength)));
        }
        if (minLength > 0) {
            element.addAttribute(new Attribute("min-length", String.valueOf(minLength)));
        }
        if (stringHasValue(verify)) {
            element.addAttribute(new Attribute("lay-verify", String.join("|", stringList)));
        }
    }

    protected HtmlElement drawLayuiRadio(String propertyName, String value, String text, String entityKey) {
        HtmlElement radio = this.drawRadio(propertyName, value, text, entityKey);
        radio.addAttribute(new Attribute("lay-filter", propertyName));
        return radio;
    }
}
