package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 15:26
 * @version 3.0
 */
public class DropdownListHtmlGenerator extends AbstractLayuiElementGenerator{

    public DropdownListHtmlGenerator(GeneratorInitialParameters generatorInitialParameters,IntrospectedColumn introspectedColumn) {
        super(generatorInitialParameters,introspectedColumn);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        HtmlElement element = new HtmlElement("select");
        element.addAttribute(new Attribute("id", introspectedColumn.getJavaProperty()));
        element.addAttribute(new Attribute("name", introspectedColumn.getJavaProperty()));
        element.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
        element.addAttribute(new Attribute("th:data-value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
        addDataUrl(element,htmlElementDescriptor,"/system/sys-dict-data-impl/option/" + introspectedColumn.getJavaProperty());
        if (stringHasValue(this.htmlElementDescriptor.getCallback())) {
            element.addAttribute(new Attribute("data-callback", VStringUtil.getFirstCharacterLowercase(this.htmlElementDescriptor.getCallback())));
        }
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
