package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;

import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 14:17
 * @version 3.0
 */
public class CheckBoxHtmlGenerator extends AbstractLayuiElementGenerator {

    public CheckBoxHtmlGenerator() {
    }

    public CheckBoxHtmlGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        super(context, introspectedTable, warnings, progressCallback);
    }

    public CheckBoxHtmlGenerator(GeneratorInitialParameters generatorInitialParameters) {
        super(generatorInitialParameters);
    }

    @Override
    public void addHtmlElement(IntrospectedColumn introspectedColumn, HtmlElement parent) {
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            HtmlElement element1 = new HtmlElement("input");
            element1.addAttribute(new Attribute("type", "checkbox"));
            element1.addAttribute(new Attribute("name", introspectedColumn.getJavaProperty() + "[" + i + "]"));
            element1.addAttribute(new Attribute("title", "选项"+(i+1)));
            element1.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
            element1.addAttribute(new Attribute("value", Integer.toString(i + 1)));
            sb.setLength(0);
            sb.append("${").append(entityKey).append(".");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append("} eq ").append(i + 1);
            element1.addAttribute(new Attribute("th:checked", sb.toString()));
            parent.addElement(element1);
        }
        if (htmlElementDescriptor.getDataUrl() != null) {
            parent.addAttribute(new Attribute("data-url", htmlElementDescriptor.getDataUrl()));
        }
        //在parent中添加data-data属性，用于保存初始值
        parent.addAttribute(new Attribute("th:data-data", thymeleafValue(introspectedColumn)));
        parent.addAttribute(new Attribute("for-type", "lay-checkbox"));
        //在parent中添加data-field属性，用于保存属性名
        parent.addAttribute(new Attribute("data-field", introspectedColumn.getJavaProperty()));
        addClassNameToElement(parent, "oas-form-item-edit");
    }

    @Override
    public String getFieldValueFormatPattern(IntrospectedColumn introspectedColumn) {
        return thymeleafValue(introspectedColumn);
    }
}