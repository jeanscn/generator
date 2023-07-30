package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;

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
        HtmlElement input = generateHtmlInput(isDisplayOnly(), isTextArea);
        parent.addElement(input);
        HtmlElement dRead;
        if (isDisplayOnly()) {
            input.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
            input.addAttribute(new Attribute("readonly", "readonly"));
            dRead = addDivWithClassToParent(parent, "oas-form-item-readonly");
        } else {
            this.addElementVerify(introspectedColumn.getActualColumnName(), input, this.htmlElementDescriptor);
            if (isTextArea) {
                addClassNameToElement(input, "layui-textarea");
                input.addAttribute(new Attribute("th:utext", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
            } else {
                addClassNameToElement(input, "layui-input");
                input.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
            }
            if (isReadonly()) {
                input.addAttribute(new Attribute("readonly", "readonly"));
            }
            input.addAttribute(new Attribute("autocomplete", "off"));
            input.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
            addClassNameToElement(input, "oas-form-item-edit");
            dRead = addDivWithClassToParent(parent, "oas-form-item-read");
        }
        dRead.addAttribute(new Attribute("th:text", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.READ)));
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getElementCss() != null) {
            voGenService.addCssStyleToElement(parent, htmlElementDescriptor.getElementCss());
        }

    }

    @Override
    public String getFieldValueFormatPattern(ThymeleafValueScopeEnum scope) {
        return thymeleafValue(scope);
    }
}
