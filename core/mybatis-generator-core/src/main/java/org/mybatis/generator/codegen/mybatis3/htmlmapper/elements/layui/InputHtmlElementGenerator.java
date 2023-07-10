package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.ConfigUtil;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;

import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 16:19
 * @version 3.0
 */
public class InputHtmlElementGenerator extends AbstractLayuiElementGenerator {

    public InputHtmlElementGenerator(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn) {
        super(generatorInitialParameters, introspectedColumn);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        boolean isTextArea = introspectedColumn.getLength() > 500;
        HtmlElement input = generateHtmlInput(isReadonly(), isTextArea);
        parent.addElement(input);
        HtmlElement dRead;
        if (!isReadonly()) {
            this.addElementVerify(introspectedColumn.getActualColumnName(), input, this.htmlElementDescriptor);
            if (isTextArea) {
                addClassNameToElement(input, "layui-textarea");
                input.addAttribute(new Attribute("th:utext", this.getFieldValueFormatPattern()));
            } else {
                addClassNameToElement(input, "layui-input");
                input.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern()));
            }
            input.addAttribute(new Attribute("autocomplete", "off"));
            input.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
            addClassNameToElement(input, "oas-form-item-edit");
            dRead = addDivWithClassToParent(parent, "oas-form-item-read");
        } else {
            dRead = addDivWithClassToParent(parent, "oas-form-item-readonly");
        }
        dRead.addAttribute(new Attribute("th:text", this.getFieldValueFormatPattern()));
    }

    @Override
    public String getFieldValueFormatPattern() {
        String javaProperty = this.introspectedColumn.getJavaProperty();
        if (htmlElementDescriptor != null) {
            javaProperty = htmlElementDescriptor.getOtherFieldName() != null ? htmlElementDescriptor.getOtherFieldName() : javaProperty;
        }else{
            OverridePropertyValueGeneratorConfiguration overridePropertyValueConfiguration = voGenService.getOverridePropertyValueConfiguration(this.introspectedColumn);
            if (overridePropertyValueConfiguration != null && overridePropertyValueConfiguration.getTargetPropertyName() != null) {
                javaProperty = overridePropertyValueConfiguration.getTargetPropertyName();
            }
        }
        return thymeleafValue(javaProperty, GenerateUtils.getEntityKeyStr(introspectedTable));
    }
}
