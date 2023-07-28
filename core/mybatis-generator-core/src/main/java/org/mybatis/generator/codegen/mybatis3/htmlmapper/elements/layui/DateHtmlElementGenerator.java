package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.StringUtility;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 12:53
 * @version 3.0
 */
public class DateHtmlElementGenerator extends AbstractLayuiElementGenerator {


    public DateHtmlElementGenerator(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn) {
        super(generatorInitialParameters, introspectedColumn);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        HtmlElement input = generateHtmlInput(true, false);
        parent.addElement(input);
        HtmlElement dateRead;
        if (!isReadonly()) {
            String dateType = Mb3GenUtil.getDateType(this.htmlElementDescriptor, introspectedColumn);
            input.addAttribute(new Attribute("lay-date", dateType));
            //dateFmt
            String dateFormat = Mb3GenUtil.getDateFormat(this.htmlElementDescriptor, introspectedColumn);
            input.addAttribute(new Attribute("lay-date-format", dateFormat));
            //range
            if (this.htmlElementDescriptor != null && this.htmlElementDescriptor.isDateRange()) {
                input.addAttribute(new Attribute("lay-date-range", "true"));
            }
            input.addAttribute(new Attribute("readonly", "readonly"));
            input.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
            addClassNameToElement(input, "layui-input");
            addClassNameToElement(input, "oas-form-item-edit");
            if (htmlElementDescriptor != null && stringHasValue(this.htmlElementDescriptor.getCallback())) {
                input.addAttribute(new Attribute("data-callback", VStringUtil.getFirstCharacterLowercase(this.htmlElementDescriptor.getCallback())));
            }
            addElementVerify(introspectedColumn.getActualColumnName(), input, this.htmlElementDescriptor);
            parent.addAttribute(new Attribute("for-type", "lay-date"));
            dateRead = addDivWithClassToParent(parent, "oas-form-item-read");
        } else {
            dateRead = addDivWithClassToParent(parent, "oas-form-item-readonly");
        }
        input.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.EDIT)));
        dateRead.addAttribute(new Attribute("th:text", this.getFieldValueFormatPattern(ThymeleafValueScopeEnum.READ)));
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getElementCss() != null) {
            voGenService.addCssStyleToElement(parent, htmlElementDescriptor.getElementCss());
        }
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
