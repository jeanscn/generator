package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.enums.ThymeleafValueScopeEnum;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 12:53
 * @version 3.0
 */
public class DateThymeleafHtmlElementGenerator extends AbstractThymeleafLayuiElementGenerator {


    public DateThymeleafHtmlElementGenerator(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters, introspectedColumn,htmlGeneratorConfiguration);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        HtmlElement editDiv = addDivWithClassToParent(parent, "oas-form-item-edit", "layui-input-wrap");
        HtmlElement input = generateHtmlInput(isDisplayOnly(this.introspectedColumn), false);
        editDiv.addElement(input);
        input.addAttribute(new Attribute("readonly", "readonly"));
        String dateType = Mb3GenUtil.getDateType(this.htmlElementDescriptor, introspectedColumn);
        if (!isDisplayOnly(this.introspectedColumn)) {
            addCssClassToElement(input, "layui-input");
            input.addAttribute(new Attribute("lay-date", dateType));
            String dateFormat = Mb3GenUtil.getDateFormat(this.htmlElementDescriptor, introspectedColumn);
            input.addAttribute(new Attribute("lay-date-format", dateFormat));
            if (this.htmlElementDescriptor != null && this.htmlElementDescriptor.isDateRange()) {
                input.addAttribute(new Attribute("lay-date-range", "true"));
            }else{
                input.addAttribute(new Attribute("lay-date-range", "false"));
            }
            input.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
            if (htmlElementDescriptor != null && stringHasValue(this.htmlElementDescriptor.getCallback())) {
                input.addAttribute(new Attribute("data-callback", VStringUtil.getFirstCharacterLowercase(this.htmlElementDescriptor.getCallback())));
            }
            addElementVerify(introspectedColumn.getActualColumnName(), input, this.htmlElementDescriptor);
            parent.addAttribute(new Attribute("for-type", "lay-date"));
            addIconToParent(addDivWithClassToParent(editDiv, "layui-input-suffix"), "layui-icon","layui-icon-date");
            addOrReplaceElementAttribute(input, "placeholder", "请选择" + introspectedColumn.getRemarks(true));
        }
        input.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
        HtmlElement dateRead = generateReadElement(htmlElementDescriptor, introspectedColumn);
        parent.addElement(dateRead);
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getElementCss() != null) {
            addCssStyleToElement(parent, htmlElementDescriptor.getElementCss());
        }
    }

    @Override
    public String getFieldValueFormatPattern(ThymeleafValueScopeEnum scope) {
        return super.getDateFieldValueFormatPattern(introspectedColumn, scope);
    }
}
