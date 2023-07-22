package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.config.ConfigUtil;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 17:29
 * @version 3.0
 */
public class SelectElementGenerator extends AbstractLayuiElementGenerator{

    public SelectElementGenerator(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn) {
        super(generatorInitialParameters,introspectedColumn);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        String dataSource = this.htmlElementDescriptor.getDataSource();
        String otherFieldName = this.htmlElementDescriptor.getOtherFieldName();
        String javaProperty = introspectedColumn.getJavaProperty();
        HtmlElement input = generateHtmlInput(otherFieldName, false, false,true,false);
        input.addAttribute(new Attribute("readonly", "readonly"));
        addClassNameToElement(input, "layui-input");
        input.addAttribute(new Attribute("data-field", javaProperty));
        addElementVerify(introspectedColumn.getActualColumnName(), input,this.htmlElementDescriptor);
        input.addAttribute(new Attribute("th:value", getFieldValueFormatPattern(ThymeleafValueScopeEnum.READ)));
        input.addAttribute(new Attribute("for-type", "lay-select"));
        input.addAttribute(new Attribute("data-type", dataSource));
        addClassNameToElement(input, "oas-form-item-edit");
        addDataUrl(input,htmlElementDescriptor,null);
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getElementCss() != null) {
            voGenService.addCssStyleToElement(parent, htmlElementDescriptor.getElementCss());
        }
        if (this.htmlElementDescriptor!=null && stringHasValue(this.htmlElementDescriptor.getCallback())) {
            input.addAttribute(new Attribute("data-callback", VStringUtil.getFirstCharacterLowercase(this.htmlElementDescriptor.getCallback())));
        }
        parent.addElement(input);
        HtmlElement divRead = addDivWithClassToParent(parent, "oas-form-item-read");
        divRead.addAttribute(new Attribute("th:text", getFieldValueFormatPattern(ThymeleafValueScopeEnum.READ)));
        if (stringHasValue(htmlElementDescriptor.getBeanName())) {
            input.addAttribute(new Attribute(HTML_ATTRIBUTE_BEAN_NAME, htmlElementDescriptor.getBeanName()));
            divRead.addAttribute(new Attribute(HTML_ATTRIBUTE_BEAN_NAME, htmlElementDescriptor.getBeanName()));
        }
        if (stringHasValue(htmlElementDescriptor.getApplyProperty())) {
            input.addAttribute(new Attribute(HTML_ATTRIBUTE_APPLY_PROPERTY, htmlElementDescriptor.getApplyProperty()));
            divRead.addAttribute(new Attribute(HTML_ATTRIBUTE_APPLY_PROPERTY, htmlElementDescriptor.getApplyProperty()));
        }
        //增加一个隐藏字段
        HtmlElement hidden = generateHtmlInput(true, false);
        hidden.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
        parent.addElement(hidden);
    }

    @Override
    public String getFieldValueFormatPattern(ThymeleafValueScopeEnum scope) {
        return  thymeleafValue(scope);
    }
}
