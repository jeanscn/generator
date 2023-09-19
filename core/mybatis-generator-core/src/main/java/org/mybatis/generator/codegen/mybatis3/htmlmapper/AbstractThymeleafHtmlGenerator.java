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
import org.mybatis.generator.config.HtmlHrefElementConfiguration;
import org.mybatis.generator.custom.HtmlDocumentTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;

import javax.annotation.Nullable;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * html生成基类
 * 包含html document、html element生成的公共方法
 */
public class AbstractThymeleafHtmlGenerator extends AbstractHtmlGenerator {

    protected HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public AbstractThymeleafHtmlGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(context, introspectedTable, warnings, progressCallback);
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }

    /**
     * 生成thymeleaf模板的值部分
     * EDIT(1,"编辑状态"),
     * READ(2,"读状态"),
     * READONLY(3,"只读状态"),
     *
     * @return thymeleaf模板的值部分
     */
    protected String thymeleafValue(IntrospectedColumn introspectedColumn, ThymeleafValueScopeEnum scopeEnum, HtmlElementDescriptor htmlElementDescriptor) {
        String javaProperty = htmlElementDescriptor != null ? htmlElementDescriptor.getColumn().getJavaProperty() : introspectedColumn.getJavaProperty();
        String otherProperty;
        if (htmlElementDescriptor != null && stringHasValue(htmlElementDescriptor.getOtherFieldName())) {
            otherProperty = htmlElementDescriptor.getOtherFieldName();
        } else {
            otherProperty = introspectedColumn.getJavaProperty();
        }
        String entityKeyStr = GenerateUtils.getEntityKeyStr(introspectedTable);
        if (scopeEnum.equals(ThymeleafValueScopeEnum.READ) || scopeEnum.equals(ThymeleafValueScopeEnum.READONLY)) {
            return thymeleafValue(otherProperty, entityKeyStr);
        } else {
            return thymeleafValue(javaProperty, entityKeyStr);
        }
    }

    protected String thymeleafValue(String propertyName, String entityKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("${").append(entityKey).append("?.").append(propertyName);
        if ("version".equals(propertyName)) {
            sb.append("}?:1");
        } else {
            sb.append("}?:_");
        }
        return sb.toString();
    }

    protected String getOtherValueFormatPattern(HtmlElementDescriptor htmlElementDescriptor) {
        String fieldName = stringHasValue(htmlElementDescriptor.getOtherFieldName()) ? htmlElementDescriptor.getOtherFieldName() : htmlElementDescriptor.getColumn().getJavaProperty();
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
        HtmlElement div = new HtmlElement("link");
        div.addAttribute(new Attribute("th:replace", value));
        htmlElement.addElement(div);
    }

    protected boolean isDateType(IntrospectedColumn introspectedColumn) {
        return GenerateUtils.isDateType(introspectedColumn);
    }

    protected boolean isReadonly(IntrospectedColumn introspectedColumn) {
        return this.htmlGeneratorConfiguration.getReadonlyFields().contains(introspectedColumn.getJavaProperty());
    }

    protected boolean isHidden(IntrospectedColumn introspectedColumn) {
        return this.htmlGeneratorConfiguration.getHiddenColumnNames().contains(introspectedColumn.getActualColumnName());
    }

    protected boolean isDisplayOnly(IntrospectedColumn introspectedColumn) {
        return this.htmlGeneratorConfiguration.getDisplayOnlyFields().contains(introspectedColumn.getJavaProperty()) || !this.htmlGeneratorConfiguration.getType().equals(HtmlDocumentTypeEnum.EDITABLE);
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

    protected HtmlElement generateReadElement(@Nullable HtmlElementDescriptor htmlElementDescriptor, IntrospectedColumn introspectedColumn) {
        HtmlElement cRead = new HtmlElement("div");
        addCssClassToElement(cRead, this.isDisplayOnly(introspectedColumn) ? "oas-form-item-readonly" : "oas-form-item-read");
        if (htmlElementDescriptor != null) {
            HtmlElementTagTypeEnum anEnum = HtmlElementTagTypeEnum.getEnum(htmlElementDescriptor.getTagType());
            switch (anEnum) {
                case CHECKBOX:
                case RADIO:
                case SWITCH:
                    if (getOtherValueFormatPattern(htmlElementDescriptor) != null) {
                        cRead.addAttribute(new Attribute("th:text", getOtherValueFormatPattern(htmlElementDescriptor)));
                    } else {
                        cRead.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, ThymeleafValueScopeEnum.READ, htmlElementDescriptor)));
                    }
                    addBeanNameApplyProperty(htmlElementDescriptor, cRead);
                    addEnumClassNamAttribute(htmlElementDescriptor, cRead);
                    addDictCodeAttribute(htmlElementDescriptor, cRead);
                    break;
                case DATE:
                    cRead.addAttribute(new Attribute("th:text", this.getDateFieldValueFormatPattern(introspectedColumn, ThymeleafValueScopeEnum.READ)));
                    break;
                case DROPDOWN_LIST:
                    if (getOtherValueFormatPattern(htmlElementDescriptor) != null) {
                        cRead.addAttribute(new Attribute("th:text", getOtherValueFormatPattern(htmlElementDescriptor)));
                    } else {
                        cRead.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, ThymeleafValueScopeEnum.READ, htmlElementDescriptor)));
                    }
                    addEnumClassNamAttribute(htmlElementDescriptor, cRead);
                    addBeanNameApplyProperty(htmlElementDescriptor, cRead);
                    addDictCodeAttribute(htmlElementDescriptor, cRead);
                    break;
                case INPUT:
                    cRead.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, ThymeleafValueScopeEnum.READ, htmlElementDescriptor)));
                    break;
                case SELECT:
                    cRead.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, ThymeleafValueScopeEnum.READ, htmlElementDescriptor)));
                    if (stringHasValue(htmlElementDescriptor.getBeanName())) {
                        cRead.addAttribute(new Attribute(HTML_ATTRIBUTE_BEAN_NAME, htmlElementDescriptor.getBeanName()));
                    }
                    if (stringHasValue(htmlElementDescriptor.getApplyProperty())) {
                        cRead.addAttribute(new Attribute(HTML_ATTRIBUTE_APPLY_PROPERTY, htmlElementDescriptor.getApplyProperty()));
                    }
                    break;
            }
        } else {
            if (GenerateUtils.isDateType(introspectedColumn)) {
                cRead.addAttribute(new Attribute("th:text", this.getDateFieldValueFormatPattern(introspectedColumn, ThymeleafValueScopeEnum.READ)));
            } else {
                cRead.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, ThymeleafValueScopeEnum.READ, null)));
            }
        }
        //增加一个链接
        if (htmlElementDescriptor != null && !htmlElementDescriptor.getHtmlHrefElementConfigurations().isEmpty()) {
            HtmlHrefElementConfiguration hrefElementConfiguration = htmlElementDescriptor.getHtmlHrefElementConfigurations().get(0);
            HtmlElement a = new HtmlElement("a");
            //获得cRead中的th:text属性
            Attribute attribute = cRead.getAttribute("th:text");
            if (attribute != null) {
                a.addAttribute(attribute);
                //删除cRead中的th:text属性
                cRead.removeAttribute(attribute);
            }
            if (hrefElementConfiguration.getHref() != null) {
                a.addAttribute(new Attribute("data-form-href", hrefElementConfiguration.getHref()));
            }
            a.addAttribute(new Attribute("title", hrefElementConfiguration.getTitle()));
            a.addAttribute(new Attribute("data-target", hrefElementConfiguration.getTarget()));
            a.addAttribute(new Attribute("data-key-selector", hrefElementConfiguration.getKeySelector()));
            if (hrefElementConfiguration.getMethod() != null) {
                a.addAttribute(new Attribute("data-form-method", hrefElementConfiguration.getMethod()));
            }
            a.addAttribute(new Attribute("data-type", hrefElementConfiguration.getType()));
            cRead.addElement(a);
        }
        return cRead;
    }

    protected String getDateFieldValueFormatPattern(IntrospectedColumn introspectedColumn, ThymeleafValueScopeEnum scopeEnum) {
        String entityName = GenerateUtils.getEntityKeyStr(introspectedTable);
        StringBuilder sb = new StringBuilder();
        sb.append("${").append(entityName).append("?.").append(introspectedColumn.getJavaProperty());
        if (introspectedColumn.isJava8TimeColumn()) {
            sb.append("!=null?#temporals.format(").append(entityName).append(".");
        } else if (introspectedColumn.isJDBCTimeColumn() || introspectedColumn.isJDBCTimeStampColumn() || introspectedColumn.isJDBCDateColumn()) {
            sb.append("!=null?#dates.format(").append(entityName).append(".");
        } else {
            sb.append("}?:_");
            return sb.toString();
        }
        if (introspectedColumn.getJdbcType() == 91) {
            sb.append(introspectedColumn.getJavaProperty()).append(",'yyyy-MM-dd'):''}");
        } else if (introspectedColumn.getJdbcType() == 92) {
            sb.append(introspectedColumn.getJavaProperty()).append(",'HH:mm:ss'):''}");
        } else if (introspectedColumn.getJdbcType() == 93) {
            sb.append(introspectedColumn.getJavaProperty()).append(",'yyyy-MM-dd HH:mm:ss'):''}");
        }
        return sb.toString();
    }
}
