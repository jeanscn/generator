package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 14:17
 * @version 3.0
 */
public class CheckBoxThymeleafHtmlGenerator extends AbstractThymeleafLayuiElementGenerator {

    public CheckBoxThymeleafHtmlGenerator(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters,introspectedColumn,htmlGeneratorConfiguration);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        StringBuilder sb = new StringBuilder();
        HtmlElement editDiv = addDivWithClassToParent(parent, "oas-form-item-edit");
        for (int i = 0; i < 2; i++) {
            HtmlElement element1 = new HtmlElement("input");
            element1.addAttribute(new Attribute("type", "checkbox"));
            element1.addAttribute(new Attribute("name", introspectedColumn.getJavaProperty() + "[" + i + "]"));
            element1.addAttribute(new Attribute("title", "选项"+(i+1)));
            element1.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
            element1.addAttribute(new Attribute("value", Integer.toString(i + 1)));
            sb.setLength(0);
            sb.append("${").append(entityKey).append(".");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append("} eq ").append(i + 1);
            element1.addAttribute(new Attribute("th:checked", sb.toString()));
            editDiv.addElement(element1);
        }
        addDataUrl(editDiv,htmlElementDescriptor,null);
        //在parent中添加data-data属性，用于保存初始值
        editDiv.addAttribute(new Attribute("th:data-data", getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
        editDiv.addAttribute(new Attribute("for-type", "lay-checkbox"));
        //在parent中添加data-field属性，用于保存属性名
        editDiv.addAttribute(new Attribute("data-field", introspectedColumn.getJavaProperty()));
        if (stringHasValue(this.htmlElementDescriptor.getCallback())) {
            editDiv.addAttribute(new Attribute("data-callback", VStringUtil.getFirstCharacterLowercase(this.htmlElementDescriptor.getCallback())));
        }
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getElementCss() != null) {
            addCssStyleToElement(editDiv, htmlElementDescriptor.getElementCss());
        }
        //只读内容
        HtmlElement cRead = addDivWithClassToParent(parent, this.isDisplayOnly(introspectedColumn)?"oas-form-item-readonly":"oas-form-item-read");
        if (getOtherValueFormatPattern(htmlElementDescriptor) != null) {
            cRead.addAttribute(new Attribute("th:text", getOtherValueFormatPattern(htmlElementDescriptor)));
        }
        addBeanNameApplyProperty(htmlElementDescriptor, cRead);
        addEnumClassNamAttribute(htmlElementDescriptor, cRead);
        addDictCodeAttribute(htmlElementDescriptor, cRead);
    }

    @Override
    public String getFieldValueFormatPattern(ThymeleafValueScopeEnum scope) {
        return thymeleafValue(scope);
    }
}
