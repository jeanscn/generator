package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 12:53
 * @version 3.0
 */
public class DateHtmlElementGenerator extends AbstractLayuiElementGenerator {



    public DateHtmlElementGenerator(GeneratorInitialParameters generatorInitialParameters,IntrospectedColumn introspectedColumn) {
        super(generatorInitialParameters,introspectedColumn);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        HtmlElement input = generateHtmlInput(isReadonly(), false);
        parent.addElement(input);
        HtmlElement dateRead;
        if (!isReadonly()) {
            String dateType = this.htmlElementDescriptor!=null && this.htmlElementDescriptor.getDataFormat() != null ? this.htmlElementDescriptor.getDataFormat() : null;
            if (!StringUtility.stringHasValue(dateType)) {
                if (introspectedColumn.getJdbcType()==93) {
                    dateType = "datetime";
                } else if (introspectedColumn.getJdbcType()==91) {
                    dateType = "date";
                } else if (introspectedColumn.getJdbcType()==92) {
                    dateType = "time";
                }
            }
            input.addAttribute(new Attribute("lay-date", dateType));
            input.addAttribute(new Attribute("readonly", "readonly"));
            input.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
            addClassNameToElement(input, "layui-input");
            addClassNameToElement(input, "oas-form-item-edit");
            addElementVerify(introspectedColumn.getActualColumnName(), input,this.htmlElementDescriptor);
            parent.addAttribute(new Attribute("for-type", "lay-date"));
            dateRead = addDivWithClassToParent(parent, "oas-form-item-read");
        }else{
            dateRead = addDivWithClassToParent(parent, "oas-form-item-readonly");
        }
        input.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
        dateRead.addAttribute(new Attribute("th:text", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.READ)));
    }

    @Override
    public String getFieldValueFormatPattern(ThymeleafValueScopeEnum scope) {
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
        if (introspectedColumn.getJdbcType()==91) {
            sb.append(introspectedColumn.getJavaProperty()).append(",'yyyy-MM-dd'):''}");
        } else if (introspectedColumn.getJdbcType()==92) {
            sb.append(introspectedColumn.getJavaProperty()).append(",'HH:mm:ss'):''}");
        } else if (introspectedColumn.getJdbcType()==93) {
            sb.append(introspectedColumn.getJavaProperty()).append(",'yyyy-MM-dd HH:mm:ss'):''}");
        }
        return sb.toString();
    }
}
