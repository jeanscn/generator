package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.config.Context;

import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 15:26
 * @version 3.0
 */
public class DropdownListHtmlGenerator extends AbstractLayuiElementGenerator{

    public DropdownListHtmlGenerator() {
    }

    public DropdownListHtmlGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        super(context, introspectedTable, warnings, progressCallback);
    }

    public DropdownListHtmlGenerator(GeneratorInitialParameters generatorInitialParameters) {
        super(generatorInitialParameters);
    }

    @Override
    public void addHtmlElement(IntrospectedColumn introspectedColumn, HtmlElement parent) {
        HtmlElement element = new HtmlElement("select");
        element.addAttribute(new Attribute("id", introspectedColumn.getJavaProperty()));
        element.addAttribute(new Attribute("name", introspectedColumn.getJavaProperty()));
        element.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
        element.addAttribute(new Attribute("th:data-value", this.getFieldValueFormatPattern(introspectedColumn)));
        addDataUrl(element,htmlElementDescriptor,"/system/sys-dict-data-impl/option/" + introspectedColumn.getJavaProperty());
        HtmlElement option = new HtmlElement("option");
        option.addAttribute(new Attribute("value", ""));
        option.addElement(new TextElement("请选择"));
        element.addElement(option);
        parent.addElement(element);
        parent.addAttribute(new Attribute("for-type", "lay-dropdownlist"));
        //读写状态区
        addClassNameToElement(parent, "oas-form-item-edit");
        //非空验证
        addElementVerify(introspectedColumn.getActualColumnName(), element,this.htmlElementDescriptor);
    }

    @Override
    public String getFieldValueFormatPattern(IntrospectedColumn introspectedColumn) {
        return thymeleafValue(introspectedColumn);
    }
}
