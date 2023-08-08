package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 15:26
 * @version 3.0
 */
public class DropdownListThymeleafHtmlGenerator extends AbstractThymeleafLayuiElementGenerator {

    public DropdownListThymeleafHtmlGenerator(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters, introspectedColumn, htmlGeneratorConfiguration);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        boolean displayOnly = this.isDisplayOnly(introspectedColumn);
        HtmlElement editDiv = addDivWithClassToParent(parent, "oas-form-item-edit",displayOnly?"layui-hide":"");
        HtmlElement element;
        if (isReadonly(this.introspectedColumn)) {
            //生成一个显示的input
            element = this.generateHtmlSelect(this.htmlElementDescriptor.getOtherFieldName(), false, true, true);
            editDiv.addElement(element);
            element.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.READONLY)));
            //生成一个隐藏的input，用于读提交数据
            HtmlElement hidden = this.generateHtmlInput(introspectedColumn.getJavaProperty(), true, false, true, true);
            hidden.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
            parent.addElement(hidden);
        }else{
            element = this.generateHtmlSelect(introspectedColumn.getJavaProperty(), displayOnly, true, true);
            editDiv.addElement(element);
            element.addAttribute(new Attribute("th:data-value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
            addDataUrl(element, htmlElementDescriptor, "/system/sys-dict-data-impl/option/" + introspectedColumn.getJavaProperty());
            if (stringHasValue(this.htmlElementDescriptor.getCallback())) {
                element.addAttribute(new Attribute("data-callback", VStringUtil.getFirstCharacterLowercase(this.htmlElementDescriptor.getCallback())));
            }
            HtmlElement option = new HtmlElement("option");
            option.addAttribute(new Attribute("value", ""));
            option.addElement(new TextElement("请选择"));
            element.addElement(option);
            parent.addAttribute(new Attribute("for-type", "lay-dropdownlist"));
            element.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
        }
        //非空验证
        addElementVerify(introspectedColumn.getActualColumnName(), element, this.htmlElementDescriptor);
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getElementCss() != null) {
            addCssStyleToElement(editDiv, htmlElementDescriptor.getElementCss());
        }
        //只读内容
        HtmlElement dpRead = addDivWithClassToParent(parent, displayOnly ? "oas-form-item-readonly" : "oas-form-item-read");
        if (getOtherValueFormatPattern(htmlElementDescriptor) != null) {
            dpRead.addAttribute(new Attribute("th:text", getOtherValueFormatPattern(htmlElementDescriptor)));
        }
        addEnumClassNamAttribute(htmlElementDescriptor, dpRead);
        addBeanNameApplyProperty(htmlElementDescriptor, dpRead);
        addDictCodeAttribute(htmlElementDescriptor, dpRead);
    }

    @Override
    public String getFieldValueFormatPattern(ThymeleafValueScopeEnum scope) {
        return thymeleafValue(scope);
    }
}
