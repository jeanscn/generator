package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.config.Context;

import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 16:19
 * @version 3.0
 */
public class InputHtmlElementGenerator extends AbstractLayuiElementGenerator{

    public InputHtmlElementGenerator() {
    }

    public InputHtmlElementGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        super(context, introspectedTable, warnings, progressCallback);
    }

    public InputHtmlElementGenerator(GeneratorInitialParameters generatorInitialParameters) {
        super(generatorInitialParameters);
    }

    @Override
    public void addHtmlElement(IntrospectedColumn introspectedColumn, HtmlElement parent) {
        boolean isTextArea = introspectedColumn.getLength() > 500;
        HtmlElement input = generateHtmlInput(introspectedColumn, false, isTextArea);
        this.addElementVerify(introspectedColumn.getActualColumnName(), input,this.htmlElementDescriptor);
        if (isTextArea) {
            addClassNameToElement(input, "layui-textarea");
            input.addAttribute(new Attribute("th:utext", this.getFieldValueFormatPattern(introspectedColumn)));
        } else {
            addClassNameToElement(input, "layui-input");
            input.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(introspectedColumn)));
        }
        input.addAttribute(new Attribute("autocomplete", "off"));
        input.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
        if (introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJavaLocalDateColumn()) {
            input.addAttribute(new Attribute("readonly", "readonly"));
            input.addAttribute(new Attribute("lay-date", "date"));
        } else if (introspectedColumn.isJDBCTimeColumn() || introspectedColumn.isJDBCTimeStampColumn() || introspectedColumn.isJavaLocalDateTimeColumn()) {
            input.addAttribute(new Attribute("readonly", "readonly"));
            input.addAttribute(new Attribute("lay-date", "datetime"));
        } else if (introspectedColumn.isJDBCTimeColumn() || introspectedColumn.isJavaLocalTimeColumn()) {
            input.addAttribute(new Attribute("readonly", "readonly"));
            input.addAttribute(new Attribute("lay-date", "time"));
        }
        addClassNameToElement(input, "oas-form-item-edit");
        parent.addElement(input);
        HtmlElement div = addDivWithClassToParent(parent, "oas-form-item-read");
        div.addAttribute(new Attribute("th:text",this.getFieldValueFormatPattern(introspectedColumn)));
    }

    @Override
    public String getFieldValueFormatPattern(IntrospectedColumn introspectedColumn) {
        return thymeleafValue(introspectedColumn);
    }
}
