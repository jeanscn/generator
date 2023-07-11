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
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;

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
                input.addAttribute(new Attribute("th:utext", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
            } else {
                addClassNameToElement(input, "layui-input");
                input.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
            }
            input.addAttribute(new Attribute("autocomplete", "off"));
            input.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
            addClassNameToElement(input, "oas-form-item-edit");
            dRead = addDivWithClassToParent(parent, "oas-form-item-read");
            dRead.addAttribute(new Attribute("th:text", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.READ)));
        } else {
            input.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
            dRead = addDivWithClassToParent(parent, "oas-form-item-readonly");
            dRead.addAttribute(new Attribute("th:text", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.READ)));
        }

    }

    @Override
    public String getFieldValueFormatPattern(ThymeleafValueScopeEnum scope) {
        return thymeleafValue(scope);
    }
}
