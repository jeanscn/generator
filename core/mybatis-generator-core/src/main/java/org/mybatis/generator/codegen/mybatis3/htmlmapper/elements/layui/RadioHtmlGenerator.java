package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import com.vgosoft.tool.core.VStringUtil;
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
 * 2023-04-13 14:37
 * @version 3.0
 */
public class RadioHtmlGenerator extends AbstractLayuiElementGenerator{

    public RadioHtmlGenerator() {
    }

    public RadioHtmlGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        super(context, introspectedTable, warnings, progressCallback);
    }

    public RadioHtmlGenerator(GeneratorInitialParameters generatorInitialParameters) {
        super(generatorInitialParameters);
    }

    @Override
    public void addHtmlElement(IntrospectedColumn introspectedColumn, HtmlElement parent) {
        StringBuilder sb = new StringBuilder();
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        if (htmlElementDescriptor.getDataFormat() != null) {
            switch (htmlElementDescriptor.getDataFormat()) {
                case "sex":
                case "性别":
                    parent.addElement(this.drawLayuiRadio(introspectedColumn.getJavaProperty(), "1", "男", entityKey));
                    parent.addElement(this.drawLayuiRadio(introspectedColumn.getJavaProperty(), "0", "女", entityKey));
                    break;
                case "level":
                case "级别":
                    parent.addElement(this.drawLayuiRadio(introspectedColumn.getJavaProperty(), "1", "1级", entityKey));
                    parent.addElement(this.drawLayuiRadio(introspectedColumn.getJavaProperty(), "2", "2级", entityKey));
                    parent.addElement(this.drawLayuiRadio(introspectedColumn.getJavaProperty(), "3", "3级", entityKey));
                    break;
                case "true":
                case "是":
                case "是否":
                    parent.addElement(this.drawLayuiRadio(introspectedColumn.getJavaProperty(), "1", "是", entityKey));
                    parent.addElement(this.drawLayuiRadio(introspectedColumn.getJavaProperty(), "0", "否", entityKey));
                    break;
                case "有":
                case "有无":
                    parent.addElement(this.drawLayuiRadio(introspectedColumn.getJavaProperty(), "1", "有", entityKey));
                    parent.addElement(this.drawLayuiRadio(introspectedColumn.getJavaProperty(), "0", "无", entityKey));
                    break;
                case "急":
                case "缓急":
                    parent.addElement(this.drawLayuiRadio(introspectedColumn.getJavaProperty(), "70", "紧急", entityKey));
                    parent.addElement(this.drawLayuiRadio(introspectedColumn.getJavaProperty(), "50", "正常", entityKey));
                    break;
            }
        } else {
            for (int i = 0; i < 3; i++) {
                HtmlElement element1 = new HtmlElement("input");
                element1.addAttribute(new Attribute("name", introspectedColumn.getJavaProperty()));
                element1.addAttribute(new Attribute("type", "radio"));
                element1.addAttribute(new Attribute("value", Integer.toString(i + 1)));
                element1.addAttribute(new Attribute("title", "选项"));
                element1.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
                sb.setLength(0);
                sb.append("${").append(entityKey).append(".");
                sb.append(introspectedColumn.getJavaProperty()).append("} eq ").append(i + 1);
                element1.addAttribute(new Attribute("th:checked", sb.toString()));
                parent.addElement(element1);
            }
            addDataUrl(parent,htmlElementDescriptor,null);
        }
        //在parent中添加data-data属性，用于保存初始值
        parent.addAttribute(new Attribute("th:data-data", this.getFieldValueFormatPattern(introspectedColumn)));
        parent.addAttribute(new Attribute("for-type", "lay-radio"));
        //在parent中添加data-field属性，用于保存属性名
        parent.addAttribute(new Attribute("data-field", introspectedColumn.getJavaProperty()));
        addClassNameToElement(parent, "oas-form-item-edit");
    }

    @Override
    public String getFieldValueFormatPattern(IntrospectedColumn introspectedColumn) {
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        if (htmlElementDescriptor.getDataFormat()!=null && htmlElementDescriptor.getDataFormat().equals("急")) {
            return VStringUtil.format("$'{'{0}.{1} ne null?({0}.{1} <= 50?''正常'':''紧急''):''正常''}'", entityKey, introspectedColumn.getJavaProperty());
        } else {
           return thymeleafValue(introspectedColumn);
        }
    }
}
