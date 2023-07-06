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
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 17:29
 * @version 3.0
 */
public class SelectElementGenerator extends AbstractLayuiElementGenerator{

    private String otherFieldName;

    public SelectElementGenerator() {
    }

    public SelectElementGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        super(context, introspectedTable, warnings, progressCallback);
    }

    public SelectElementGenerator(GeneratorInitialParameters generatorInitialParameters) {
        super(generatorInitialParameters);
    }

    @Override
    public void addHtmlElement(IntrospectedColumn introspectedColumn, HtmlElement parent) {
        String dataSource = this.htmlElementDescriptor.getDataSource();
        //计算使用方言
       /* String thisDialect = null;
        if (stringHasValue(this.htmlElementDescriptor.getDataSource())) {
            thisDialect = "vgo:"+(dataSource.equals("Department")?"deptName":dataSource.equals("User")?"userName":dataSource.toLowerCase());
        }*/

        this.otherFieldName = this.htmlElementDescriptor.getOtherFieldName();
        String javaProperty = introspectedColumn.getJavaProperty();
        if (!StringUtility.stringHasValue(this.otherFieldName)) {
            this.otherFieldName = ConfigUtil.getOverrideJavaProperty(javaProperty);
            this.htmlElementDescriptor.setOtherFieldName(this.otherFieldName);
        }
        HtmlElement input = generateHtmlInput(this.otherFieldName, false, false);
        input.addAttribute(new Attribute("readonly", "readonly"));
        addClassNameToElement(input, "layui-input");
        input.addAttribute(new Attribute("data-field", javaProperty));
        addElementVerify(introspectedColumn.getActualColumnName(), input,this.htmlElementDescriptor);
        //input.addAttribute(new Attribute(thisDialect==null?"th:value":thisDialect, this.thymeleafValue(introspectedColumn)));
        input.addAttribute(new Attribute("th:value", this.thymeleafValue(this.htmlElementDescriptor.getOtherFieldName(),GenerateUtils.getEntityKeyStr(introspectedTable))));
        input.addAttribute(new Attribute("for-type", "lay-select"));
        input.addAttribute(new Attribute("data-type", dataSource));
        if (stringHasValue(this.htmlElementDescriptor.getCallback())) {
            input.addAttribute(new Attribute("data-callback", VStringUtil.getFirstCharacterLowercase(this.htmlElementDescriptor.getCallback())));
        }
        addClassNameToElement(input, "oas-form-item-edit");
        addDataUrl(input,htmlElementDescriptor,null);
        parent.addElement(input);
        HtmlElement divRead = addDivWithClassToParent(parent, "oas-form-item-read");
        //divRead.addAttribute(new Attribute(thisDialect==null?"th:text":thisDialect, this.thymeleafValue(introspectedColumn)));
        divRead.addAttribute(new Attribute("th:text", this.thymeleafValue(this.htmlElementDescriptor.getOtherFieldName(),GenerateUtils.getEntityKeyStr(introspectedTable))));
        if (stringHasValue(htmlElementDescriptor.getBeanName())) {
            input.addAttribute(new Attribute(HTML_ATTRIBUTE_BEAN_NAME, htmlElementDescriptor.getBeanName()));
            divRead.addAttribute(new Attribute(HTML_ATTRIBUTE_BEAN_NAME, htmlElementDescriptor.getBeanName()));
        }
        if (stringHasValue(htmlElementDescriptor.getApplyProperty())) {
            input.addAttribute(new Attribute(HTML_ATTRIBUTE_APPLY_PROPERTY, htmlElementDescriptor.getApplyProperty()));
            divRead.addAttribute(new Attribute(HTML_ATTRIBUTE_APPLY_PROPERTY, htmlElementDescriptor.getApplyProperty()));
        }
        //增加一个隐藏字段
        HtmlElement hidden = generateHtmlInput(introspectedColumn, true, false);
        hidden.addAttribute(new Attribute("th:value", this.getFieldValueFormatPattern(introspectedColumn)));
        parent.addElement(hidden);
    }

    @Override
    public String getFieldValueFormatPattern(IntrospectedColumn introspectedColumn) {
        return  thymeleafValue(introspectedColumn);
    }

    private String getOtherFieldValueFormatPattern(IntrospectedColumn introspectedColumn) {
        return  "${"
                + GenerateUtils.getEntityKeyStr(introspectedTable)
                + "?."
                + (this.otherFieldName == null ? introspectedColumn.getJavaProperty() : this.otherFieldName)
                + "}";
    }
}
