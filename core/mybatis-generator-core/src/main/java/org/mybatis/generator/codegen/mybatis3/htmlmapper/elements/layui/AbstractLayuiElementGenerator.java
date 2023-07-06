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
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.DictTypeEnum;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

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

    protected void addElementVerify(String columnName, HtmlElement element, @Nullable HtmlElementDescriptor htmlElementDescriptor) {
        IntrospectedColumn column = introspectedTable.getColumn(columnName).orElse(null);
        if (column == null) {
            return;
        }
        List<String> verifyList = new ArrayList<>();
        HtmlGeneratorConfiguration htmlGeneratorConfiguration = this.getHtmlGeneratorConfiguration();
        if (htmlElementDescriptor != null) {
            htmlGeneratorConfiguration = htmlElementDescriptor.getHtmlGeneratorConfiguration();
            verifyList.addAll(htmlElementDescriptor.getVerify().stream().distinct().collect(Collectors.toList()));
        }

        //日期类型
        if (column.getJdbcTypeName().equalsIgnoreCase("DATE") && !verifyList.contains("date")) {
            verifyList.add("date");
        }
        //时间类型
        if (column.getJdbcTypeName().equalsIgnoreCase("TIME") && !verifyList.contains("time")) {
            verifyList.add("time");
        }
        //日期时间类型
        if (column.getJdbcTypeName().equalsIgnoreCase("TIMESTAMP") && !verifyList.contains("datetime")) {
            verifyList.add("datetime");
        }
        //数字类型
        if ((column.getJdbcTypeName().equalsIgnoreCase("DECIMAL")
                || column.getJdbcTypeName().equalsIgnoreCase("NUMERIC")
                || column.getJdbcTypeName().equalsIgnoreCase("BIGINT")
                || column.getJdbcTypeName().equalsIgnoreCase("INTEGER")
                || column.getJdbcTypeName().equalsIgnoreCase("INT")) && !verifyList.contains("number")) {
            verifyList.add("number");
        }
        //长度
        if (column.getLength() > 0 && !verifyList.contains("limit")) {
            verifyList.add(0, "limit");
        }
        //非空
        if (htmlGeneratorConfiguration.getElementRequired().contains(columnName) && !verifyList.contains("required")) {
            verifyList.add(0, "required");
        }

        //计算长度限制
        int maxLength = column.getLength();
        int minLength = column.getMinLength() > 0
                ? column.getMinLength()
                : (verifyList.contains("required") ? 1 : 0);
        if (maxLength > 0 && maxLength != 255) {
            element.addAttribute(new Attribute("max-length", String.valueOf(maxLength)));
        }
        if (minLength > 0) {
            element.addAttribute(new Attribute("min-length", String.valueOf(minLength)));
        }
        if (verifyList.size() > 0) {
            element.addAttribute(new Attribute("lay-verify", String.join("|", verifyList)));
        }
    }

    protected HtmlElement drawLayuiRadio(String propertyName, String value, String text, String entityKey) {
        HtmlElement radio = this.drawRadio(propertyName, value, text, entityKey);
        radio.addAttribute(new Attribute("lay-filter", propertyName));
        return radio;
    }

    protected void addDataUrl(HtmlElement element, HtmlElementDescriptor htmlElementDescriptor, String defaultDataUrl) {
        if (htmlElementDescriptor == null) {
            return;
        }
        if (htmlElementDescriptor.getDataUrl() != null) {
            element.addAttribute(new Attribute("data-url", htmlElementDescriptor.getDataUrl()));
        } else if (htmlElementDescriptor.getDataSource() != null && htmlElementDescriptor.getDataSource().equals(DictTypeEnum.DICT_ENUM.getCode()) && stringHasValue(htmlElementDescriptor.getEnumClassName())) {
            element.addAttribute(new Attribute("data-url", "/system/enum/options/" + htmlElementDescriptor.getEnumClassName()));
        } else if (defaultDataUrl != null) {
            element.addAttribute(new Attribute("data-url", defaultDataUrl));
        }
    }
}
