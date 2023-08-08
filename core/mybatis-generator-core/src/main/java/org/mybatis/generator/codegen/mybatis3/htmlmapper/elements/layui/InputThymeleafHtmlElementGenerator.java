package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 16:19
 * @version 3.0
 */
public class InputThymeleafHtmlElementGenerator extends AbstractThymeleafLayuiElementGenerator {

    public InputThymeleafHtmlElementGenerator(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters, introspectedColumn, htmlGeneratorConfiguration);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        boolean displayOnly = this.isDisplayOnly(introspectedColumn);
        boolean readonly = this.isReadonly(introspectedColumn);
        boolean isTextArea = introspectedColumn.getLength() > 500;
        HtmlElement editDiv = addDivWithClassToParent(parent, "oas-form-item-edit", "layui-input-wrap",displayOnly ? "layui-hide" : "");
        HtmlElement input;
        String javaProperty = this.htmlElementDescriptor != null ? this.htmlElementDescriptor.getColumn().getJavaProperty() : this.introspectedColumn.getJavaProperty();
        String otherProperty = this.htmlElementDescriptor != null ? this.htmlElementDescriptor.getOtherFieldName() : this.introspectedColumn.getJavaProperty();
        if (readonly) {
            input = generateHtmlInput(otherProperty, false, isTextArea, true, true);
            input.addAttribute(new Attribute(isTextArea ? "th:utext" : "th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.READONLY)));
            input.addAttribute(new Attribute("readonly", "readonly"));
            addIconToParent(addDivWithClassToParent(editDiv, "layui-input-suffix"), "layui-icon","layui-icon-eye");
            if (this.htmlElementDescriptor != null && !this.htmlElementDescriptor.getColumn().getJavaProperty().equals(this.htmlElementDescriptor.getOtherFieldName())) {
                //增加一个隐藏列
                HtmlElement hidden = generateHtmlInput(this.htmlElementDescriptor.getColumn().getJavaProperty(), true, isTextArea, true, true);
                hidden.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
                hidden.addAttribute(new Attribute("readonly", "readonly"));
                editDiv.addElement(hidden);
            }
        } else {
            input = generateHtmlInput(javaProperty, false, isTextArea, true, true);
            this.addElementVerify(introspectedColumn.getActualColumnName(), input, this.htmlElementDescriptor);
            input.addAttribute(new Attribute(isTextArea ? "th:utext" : "th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
            input.addAttribute(new Attribute("autocomplete", "off"));
            input.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
            addOrReplaceElementAttribute(input, "placeholder", "请输入" + introspectedColumn.getRemarks(true));
            addOrReplaceElementAttribute(input, "lay-affix", "clear");
        }
        editDiv.addElement(input);
        addCssClassToElement(input, isTextArea ? "layui-textarea" : "layui-input");
        HtmlElement dRead = addDivWithClassToParent(parent, displayOnly ? "oas-form-item-readonly" : "oas-form-item-read");
        dRead.addAttribute(new Attribute("th:text", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.READ)));
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getElementCss() != null) {
            addCssStyleToElement(parent, htmlElementDescriptor.getElementCss());
        }
    }

    @Override
    public String getFieldValueFormatPattern(ThymeleafValueScopeEnum scope) {
        return thymeleafValue(scope);
    }
}
