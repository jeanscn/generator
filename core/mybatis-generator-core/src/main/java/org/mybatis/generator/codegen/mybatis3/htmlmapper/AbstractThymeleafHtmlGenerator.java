package org.mybatis.generator.codegen.mybatis3.htmlmapper;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * html生成基类
 * 包含html document、html element生成的公共方法
 *
 */
public class AbstractThymeleafHtmlGenerator extends AbstractHtmlGenerator {

    protected HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public AbstractThymeleafHtmlGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback,HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(context, introspectedTable, warnings, progressCallback);
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }

    /**
     * 生成thymeleaf模板的值部分
     *          EDIT(1,"编辑状态"),
     *          READ(2,"读状态"),
     *          READONLY(3,"只读状态"),
     * @return thymeleaf模板的值部分
     */
    protected String thymeleafValue(IntrospectedColumn introspectedColumn, ThymeleafValueScopeEnum scopeEnum, HtmlElementDescriptor htmlElementDescriptor) {
        String javaProperty = htmlElementDescriptor!=null?htmlElementDescriptor.getColumn().getJavaProperty():introspectedColumn.getJavaProperty();
        String otherProperty;
        if (htmlElementDescriptor!=null && stringHasValue(htmlElementDescriptor.getOtherFieldName())) {
            otherProperty = htmlElementDescriptor.getOtherFieldName();
        }else{
            otherProperty = introspectedColumn.getJavaProperty();
        }
        String entityKeyStr = GenerateUtils.getEntityKeyStr(introspectedTable);
        if (scopeEnum.equals(ThymeleafValueScopeEnum.READ) ||scopeEnum.equals(ThymeleafValueScopeEnum.READONLY) ) {
            return thymeleafValue(otherProperty, entityKeyStr);
        }else{
            return thymeleafValue(javaProperty, entityKeyStr);
        }
    }

    protected String thymeleafValue(String propertyName,String entityKey){
        StringBuilder sb = new StringBuilder();
        sb.append("${").append(entityKey).append("?.").append(propertyName);
        if ("version".equals(propertyName)) {
            sb.append("}?:1");
        } else {
            sb.append("}?:_");
        }
        return sb.toString();
    }

    protected String getOtherValueFormatPattern(HtmlElementDescriptor htmlElementDescriptor){
        String fieldName = stringHasValue(htmlElementDescriptor.getOtherFieldName())?htmlElementDescriptor.getOtherFieldName():htmlElementDescriptor.getColumn().getJavaProperty();
        return "${" + GenerateUtils.getEntityKeyStr(introspectedTable) + "?." + fieldName + "}?:_";
    }

    protected void addStaticThymeleafStyleSheet(HtmlElement htmlElement, String value) {
        HtmlElement link = super.addStaticStyleSheet(htmlElement, value);
        removeElementAttribute(link, "href");
        addOrReplaceElementAttribute(link, "th:href", "@{" + value + "}");
    }


    protected void addStaticThymeleafJavaScript(HtmlElement htmlElement, String value) {
        HtmlElement script = super.addStaticJavaScript(htmlElement, value);
        removeElementAttribute(script, "src");
        addOrReplaceElementAttribute(script, "th:src", "@{" + value + "}");
    }

    protected void addStaticReplace(HtmlElement htmlElement, String value) {
        HtmlElement div = new HtmlElement("div");
        div.addAttribute(new Attribute("th:replace", value));
        htmlElement.addElement(div);
    }

    protected boolean isDateType(IntrospectedColumn introspectedColumn) {
        return GenerateUtils.isDateType(introspectedColumn);
    }

    protected boolean isReadonly(IntrospectedColumn introspectedColumn){
        return this.htmlGeneratorConfiguration.getReadonlyFields().contains(introspectedColumn.getJavaProperty());
    }
    protected boolean isHidden(IntrospectedColumn introspectedColumn){
        return this.htmlGeneratorConfiguration.getHiddenColumnNames().contains(introspectedColumn.getActualColumnName());
    }

    protected boolean isDisplayOnly(IntrospectedColumn introspectedColumn){
        return this.htmlGeneratorConfiguration.getDisplayOnlyFields().contains(introspectedColumn.getJavaProperty());
    }

    protected void addDictCodeAttribute(HtmlElementDescriptor htmlElementDescriptor, HtmlElement htmlElement) {
        if (VStringUtil.stringHasValue(htmlElementDescriptor.getDictCode())) {
            htmlElement.addAttribute(new Attribute(HTML_ATTRIBUTE_DICT_CODE, htmlElementDescriptor.getDictCode()));
        }
    }

    protected void addBeanNameApplyProperty(HtmlElementDescriptor htmlElementDescriptor, HtmlElement element) {
        if (htmlElementDescriptor.getBeanName() != null) {
            element.addAttribute(new Attribute(HTML_ATTRIBUTE_BEAN_NAME, htmlElementDescriptor.getBeanName()));
        }
        if (htmlElementDescriptor.getApplyProperty() != null) {
            element.addAttribute(new Attribute(HTML_ATTRIBUTE_APPLY_PROPERTY, htmlElementDescriptor.getApplyProperty()));
        }
    }

    protected void addEnumClassNamAttribute(HtmlElementDescriptor htmlElementDescriptor, HtmlElement element) {
        if (htmlElementDescriptor.getEnumClassName() != null) {
            element.addAttribute(new Attribute(HTML_ATTRIBUTE_ENUM_CLASS_NAME, htmlElementDescriptor.getEnumClassName()));
        }
    }

}
