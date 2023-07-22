package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;
import org.mybatis.generator.internal.util.StringUtility;

import static com.vgosoft.tool.core.VStringUtil.format;
import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 14:37
 * @version 3.0
 */
public class RadioHtmlGenerator extends AbstractLayuiElementGenerator {

    public RadioHtmlGenerator(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn) {
        super(generatorInitialParameters,introspectedColumn);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        StringBuilder sb = new StringBuilder();
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        String enumClassName = htmlElementDescriptor.getEnumClassName();
        if (!stringHasValue(htmlElementDescriptor.getDataSource())) {
            htmlElementDescriptor.setDataSource("DictEnum");
            if (htmlElementDescriptor.getDataFormat() != null) {
                enumClassName = htmlElementDescriptor.getEnumClassName();
            } else {
                enumClassName = "com.vgosoft.core.constant.enums.core.OptionsEnum";
            }
        }
        if ("DictEnum".equals(htmlElementDescriptor.getDataSource()) && !stringHasValue(htmlElementDescriptor.getDataUrl())) {
            parent.addAttribute(new Attribute("data-url", "/system/enum/options/" + enumClassName));
        } else if (stringHasValue(htmlElementDescriptor.getDataUrl())) {
            parent.addAttribute(new Attribute("data-url", htmlElementDescriptor.getDataUrl()));
        }
        //在parent中添加data-data属性，用于保存初始值
        parent.addAttribute(new Attribute("th:data-data", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
        parent.addAttribute(new Attribute("for-type", "lay-radio"));
        //在parent中添加data-field属性，用于保存属性名
        parent.addAttribute(new Attribute("data-field", introspectedColumn.getJavaProperty()));
        addClassNameToElement(parent, "oas-form-item-edit");
        if (StringUtility.stringHasValue(this.htmlElementDescriptor.getCallback())) {
            parent.addAttribute(new Attribute("data-callback", VStringUtil.getFirstCharacterLowercase(this.htmlElementDescriptor.getCallback())));
        }
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getElementCss() != null) {
            voGenService.addCssStyleToElement(parent, htmlElementDescriptor.getElementCss());
        }
    }

    @Override
    public String getFieldValueFormatPattern(ThymeleafValueScopeEnum scope) {
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        if (htmlElementDescriptor.getDataFormat() != null && htmlElementDescriptor.getDataFormat().contains("急")) {
            return format("$'{'{0}.{1} ne null?({0}.{1} <= 50?''正常'':''紧急''):''正常''}'", entityKey, introspectedColumn.getJavaProperty());
        } else {
            return thymeleafValue(scope);
        }
    }
}
