package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.AbstractHtmlElementGenerator;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;

import java.util.List;

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

     protected void addElementRequired(String columnName, HtmlElement element) {
        IntrospectedColumn introspectedColumn = introspectedTable.getColumn(columnName).orElse(null);
         if (htmlGeneratorConfiguration != null) {
             List<String> htmlElementInputRequired = htmlGeneratorConfiguration.getElementRequired();
             if (htmlElementInputRequired.contains(columnName) || (introspectedColumn != null && !introspectedColumn.isNullable())) {
                 element.addAttribute(new Attribute("lay-verify", "required"));
             }
         }else{
                if (introspectedColumn != null && !introspectedColumn.isNullable()) {
                    element.addAttribute(new Attribute("lay-verify", "required"));
                }
         }
    }

    protected HtmlElement drawLayuiRadio(String propertyName, String value, String text, String entityKey){
        HtmlElement radio = this.drawRadio(propertyName, value, text, entityKey);
        radio.addAttribute(new Attribute("lay-filter", propertyName));
        return radio;
    }
}
