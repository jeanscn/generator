package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 12:53
 * @version 3.0
 */
public class DateHtmlElementGenerator extends AbstractLayuiElementGenerator {



    public DateHtmlElementGenerator(GeneratorInitialParameters generatorInitialParameters) {
        super(generatorInitialParameters);
    }

    public DateHtmlElementGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        super(context, introspectedTable, warnings, progressCallback);
    }

    @Override
    public void addHtmlElement(IntrospectedColumn introspectedColumn,HtmlElement parent) {
        HtmlElement input = generateHtmlInput(introspectedColumn, false, false);
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
        input.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(introspectedColumn)));
        addClassNameToElement(input, "layui-input");
        input.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
        addElementRequired(introspectedColumn.getActualColumnName(), input);
        parent.addElement(input);
        parent.addAttribute(new Attribute("for-type", "lay-date"));
        addClassNameToElement(input, "oas-form-item-edit");
        HtmlElement dateRead = addDivWithClassToParent(parent, "oas-form-item-read");
        dateRead.addAttribute(new Attribute("th:text", this.getFieldValueFormatPattern(introspectedColumn)));
    }

    @Override
    public String getFieldValueFormatPattern(IntrospectedColumn baseColumn) {
        String entityName = GenerateUtils.getEntityKeyStr(introspectedTable);
        StringBuilder sb = new StringBuilder();
        sb.append("${").append(entityName).append("?.").append(baseColumn.getJavaProperty());
        if (baseColumn.isJava8TimeColumn()) {
            sb.append("!=null?#temporals.format(").append(entityName).append(".");
        } else if (baseColumn.isJDBCTimeColumn() || baseColumn.isJDBCTimeStampColumn() || baseColumn.isJDBCDateColumn()) {
            sb.append("!=null?#dates.format(").append(entityName).append(".");
        } else {
            sb.append("}?:_");
            return sb.toString();
        }
        if (baseColumn.getJdbcType()==91) {
            sb.append(baseColumn.getJavaProperty()).append(",'yyyy-MM-dd'):''}");
        } else if (baseColumn.getJdbcType()==92) {
            sb.append(baseColumn.getJavaProperty()).append(",'HH:mm:ss'):''}");
        } else if (baseColumn.getJdbcType()==93) {
            sb.append(baseColumn.getJavaProperty()).append(",'yyyy-MM-dd HH:mm:ss'):''}");
        }
        return sb.toString();
    }
}
