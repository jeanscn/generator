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
 * 2023-04-13 14:49
 * @version 3.0
 */
public class SwitchHtmlGenerator extends AbstractLayuiElementGenerator {

    public SwitchHtmlGenerator() {
    }

    public SwitchHtmlGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        super(context, introspectedTable, warnings, progressCallback);
    }

    public SwitchHtmlGenerator(GeneratorInitialParameters generatorInitialParameters) {
        super(generatorInitialParameters);
    }

    @Override
    public void addHtmlElement(IntrospectedColumn introspectedColumn, HtmlElement parent) {
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        HtmlElement element = new HtmlElement("input");
        element.addAttribute(new Attribute("type", "checkbox"));
        element.addAttribute(new Attribute("lay-skin", "switch"));
        if (htmlElementDescriptor.getDataFormat() != null) {
            switch (htmlElementDescriptor.getDataFormat()) {
                case "有无":
                    element.addAttribute(new Attribute("lay-text", "有|无"));
                    break;
                case "是否":
                    element.addAttribute(new Attribute("lay-text", "是|否"));
                    break;
                case "性别":
                    element.addAttribute(new Attribute("lay-text", "男|女"));
                    break;
                default:
                    element.addAttribute(new Attribute("lay-text", "启用|停用"));
            }
        }
        element.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
        if (htmlElementDescriptor.getDataUrl() != null) {
            element.addAttribute(new Attribute("data-url", htmlElementDescriptor.getDataUrl()));
        }
        String sb = "${" + entityKey + "?." +
                introspectedColumn.getJavaProperty() + "} eq 1";
        element.addAttribute(new Attribute("th:checked", sb));
        parent.addElement(element);
        //增加提交数据的隐藏域
        element = new HtmlElement("input");
        element.addAttribute(new Attribute("id", introspectedColumn.getJavaProperty()));
        element.addAttribute(new Attribute("name", introspectedColumn.getJavaProperty()));
        element.addAttribute(new Attribute("type", "hidden"));
        element.addAttribute(new Attribute("th:value", thymeleafValue(introspectedColumn)));
        parent.addElement(element);

        //读写状态区
        addClassNameToElement(parent, "oas-form-item-edit");
        parent.addAttribute(new Attribute("for-type", "lay-switch"));
        //在parent中添加data-data属性，用于保存初始值
        parent.addAttribute(new Attribute("th:data-data", thymeleafValue(introspectedColumn)));
        //在parent中添加data-field属性，用于保存属性名
        parent.addAttribute(new Attribute("data-field", introspectedColumn.getJavaProperty()));
        //非空验证
        addElementRequired(introspectedColumn.getActualColumnName(), element);
    }

    @Override
    public String getFieldValueFormatPattern(IntrospectedColumn introspectedColumn) {
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedColumn.getIntrospectedTable());
        StringBuilder sb = new StringBuilder();
        sb.append("${").append(entityKey).append("?.").append(introspectedColumn.getJavaProperty());
        if (htmlElementDescriptor.getDataFormat() != null) {
            switch (htmlElementDescriptor.getDataFormat()) {
                case "有无":
                    sb.append("} eq 1 ? '有':'无'");
                    break;
                case "是否":
                    sb.append("} eq 1 ? '是':'否'");
                    break;
                case "性别":
                    sb.append("} eq 1 ? '男':'女'");
                    break;
                default:
                    sb.append("} eq 1 ? '启用':'停用'");
            }
        }
        return sb.toString();
    }
}
