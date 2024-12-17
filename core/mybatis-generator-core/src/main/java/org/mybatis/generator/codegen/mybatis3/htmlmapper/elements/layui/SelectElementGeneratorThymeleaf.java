package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 17:29
 * @version 3.0
 */
public class SelectElementGeneratorThymeleaf extends AbstractThymeleafLayuiElementGenerator {

    public SelectElementGeneratorThymeleaf(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters, introspectedColumn,htmlGeneratorConfiguration);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        String dataSource = this.htmlElementDescriptor.getDataSource();
        String otherFieldName = this.htmlElementDescriptor.getOtherFieldName();
        String javaProperty = introspectedColumn.getJavaProperty();
        //生成input，如果displayOnly则隐藏
        HtmlElement input = generateHtmlInput(otherFieldName, isDisplayOnly(this.introspectedColumn), false, true, true);
        addCssClassToElement(input, "layui-input");
        input.addAttribute(new Attribute("data-field", javaProperty));
        input.addAttribute(new Attribute("readonly", "readonly"));
        input.addAttribute(new Attribute("th:value", getFieldValueFormatPattern(ThymeleafValueScopeEnum.READ)));
        input.addAttribute(new Attribute("data-multiple", htmlElementDescriptor.isMultiple()? "true" : "false"));
        if (!isReadonly(this.introspectedColumn)) {
            addElementVerify(introspectedColumn.getActualColumnName(), input, this.htmlElementDescriptor);
            input.addAttribute(new Attribute("for-type", "lay-select"));
            input.addAttribute(new Attribute("data-type", dataSource));
            addDataUrl(input, htmlElementDescriptor, null);
            if (htmlElementDescriptor != null && HtmlElementDataSourceEnum.INNER_TABLE.getCode().equals(dataSource)) {
                input.addAttribute(new Attribute("data-list-key", htmlElementDescriptor.getListKey()));
                input.addAttribute(new Attribute("data-list-view-class", htmlElementDescriptor.getListViewClass()));
            }
            if (this.htmlElementDescriptor != null && stringHasValue(this.htmlElementDescriptor.getCallback())) {
                input.addAttribute(new Attribute("data-callback", VStringUtil.getFirstCharacterLowercase(this.htmlElementDescriptor.getCallback())));
            }
            addOrReplaceElementAttribute(input, "placeholder", "请选择" + introspectedColumn.getRemarks(true));
        }
        HtmlElement editDiv = addDivWithClassToParent(parent, "oas-form-item-edit","layui-input-wrap");
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getElementCss() != null) {
            addCssStyleToElement(editDiv, htmlElementDescriptor.getElementCss());
        }
        editDiv.addElement(input);

        HtmlElement divRead = generateReadElement(htmlElementDescriptor, introspectedColumn);
        parent.addElement(divRead);

        if (stringHasValue(htmlElementDescriptor.getBeanName())) {
            input.addAttribute(new Attribute(HTML_ATTRIBUTE_BEAN_NAME, htmlElementDescriptor.getBeanName()));
        }
        if (stringHasValue(htmlElementDescriptor.getApplyProperty())) {
            input.addAttribute(new Attribute(HTML_ATTRIBUTE_APPLY_PROPERTY, htmlElementDescriptor.getApplyProperty()));
        }
        //如果有映射字段，增加一个隐藏字段
        if (!this.htmlElementDescriptor.getOtherFieldName().equals(introspectedColumn.getJavaProperty())) {
            HtmlElement hidden = generateHtmlInput(this.htmlElementDescriptor.getColumn().getJavaProperty(), true, false, true, true);
            hidden.addAttribute(new Attribute("th:value", getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
            parent.addElement(hidden);
        }
        if (!isDisplayOnly(this.introspectedColumn) && !isReadonly(this.introspectedColumn)) {
            addLayuiInputSuffix(editDiv,htmlElementDescriptor);
        }
    }

    @Override
    public String getFieldValueFormatPattern(ThymeleafValueScopeEnum scope) {
        return thymeleafValue(scope);
    }
}
