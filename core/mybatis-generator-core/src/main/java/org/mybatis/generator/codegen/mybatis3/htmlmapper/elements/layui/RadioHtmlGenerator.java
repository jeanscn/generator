package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;

import java.util.List;

import static com.vgosoft.tool.core.VStringUtil.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 14:37
 * @version 3.0
 */
public class RadioHtmlGenerator extends AbstractLayuiElementGenerator {

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
        String enumClassName = htmlElementDescriptor.getEnumClassName();
        if (!stringHasValue(htmlElementDescriptor.getDataSource())) {
            htmlElementDescriptor.setDataSource("DictEnum");
            if (htmlElementDescriptor.getDataFormat() != null) {
                enumClassName = htmlElementDescriptor.getEnumClassName();
            } else {
                enumClassName = "com.vgosoft.core.constant.enums.core.OptionsEnum";
            }
        }
        if ("DictEnum".equals(htmlElementDescriptor.getDataSource()) && !stringHasValue(htmlElementDescriptor.getDataUrl())) {
            parent.addAttribute(new Attribute("data-url", "/system/enum/options/" + enumClassName));
        } else if (stringHasValue(htmlElementDescriptor.getDataUrl())) {
            parent.addAttribute(new Attribute("data-url", htmlElementDescriptor.getDataUrl()));
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
        if (htmlElementDescriptor.getDataFormat() != null && htmlElementDescriptor.getDataFormat().contains("急")) {
            return format("$'{'{0}.{1} ne null?({0}.{1} <= 50?''正常'':''紧急''):''正常''}'", entityKey, introspectedColumn.getJavaProperty());
        } else {
            return thymeleafValue(introspectedColumn);
        }
    }
}
