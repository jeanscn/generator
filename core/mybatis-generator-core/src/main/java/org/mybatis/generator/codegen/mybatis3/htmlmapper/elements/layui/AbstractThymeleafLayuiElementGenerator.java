package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.AbstractThymeleafHtmlElementGenerator;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import com.vgosoft.core.constant.enums.core.DictTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import org.mybatis.generator.custom.enums.ThymeleafValueScopeEnum;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 13:47
 * @version 3.0
 */
public abstract class AbstractThymeleafLayuiElementGenerator extends AbstractThymeleafHtmlElementGenerator {


    protected AbstractThymeleafLayuiElementGenerator(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters, introspectedColumn, htmlGeneratorConfiguration);
    }

    @Override
    public abstract void addHtmlElement(HtmlElement parent);

    @Override
    public abstract String getFieldValueFormatPattern(ThymeleafValueScopeEnum scope);

    protected void addElementVerify(String columnName, HtmlElement element, @Nullable HtmlElementDescriptor htmlElementDescriptor) {
        IntrospectedColumn column = introspectedTable.getColumn(columnName).orElse(null);
        if (column == null) {
            return;
        }
        List<String> verifyList = new ArrayList<>();
        HtmlGeneratorConfiguration htmlGeneratorConfiguration = this.getHtmlGeneratorConfiguration();
        if (htmlElementDescriptor != null) {
            htmlGeneratorConfiguration = htmlElementDescriptor.getHtmlGeneratorConfiguration();
            verifyList.addAll(htmlElementDescriptor.getVerify().stream().distinct().collect(Collectors.toList()));
        }

        //日期类型
        if (column.getJdbcTypeName().equalsIgnoreCase("DATE") && !verifyList.contains("date")) {
            verifyList.add("date");
        }
        //时间类型
        if (column.getJdbcTypeName().equalsIgnoreCase("TIME") && !verifyList.contains("time")) {
            verifyList.add("time");
        }
        //日期时间类型
        if (column.getJdbcTypeName().equalsIgnoreCase("TIMESTAMP") && !verifyList.contains("datetime")) {
            verifyList.add("datetime");
        }
        //数字类型
        if ((column.getJdbcTypeName().equalsIgnoreCase("NUMERIC")
                || column.getJdbcTypeName().equalsIgnoreCase("BIGINT")
                || column.getJdbcTypeName().equalsIgnoreCase("INTEGER")
                || column.getJdbcTypeName().equalsIgnoreCase("INT")) && !verifyList.contains("number")) {
            verifyList.add("number");
        }
        //长度
        if (column.getLength() > 0 && !verifyList.contains("limit")) {
            verifyList.add(0, "limit");
        }
        //非空
        if (htmlGeneratorConfiguration.getElementRequired().contains(columnName) && !verifyList.contains("required")) {
            verifyList.add(0, "required");
        }

        //计算长度限制
        int maxLength = column.getLength();
        int minLength = column.getMinLength() > 0
                ? column.getMinLength()
                : (verifyList.contains("required") ? 1 : 0);
        if (maxLength > 0 && maxLength != 255) {
            element.addAttribute(new Attribute("max-length", String.valueOf(maxLength)));
        }
        if (minLength > 0) {
            element.addAttribute(new Attribute("min-length", String.valueOf(minLength)));
        }
        if (!verifyList.isEmpty()) {
            element.addAttribute(new Attribute("lay-verify", String.join("|", verifyList)));
        }
    }

    protected void addDataUrl(HtmlElement element, HtmlElementDescriptor htmlElementDescriptor, String defaultDataUrl) {
        if (htmlElementDescriptor == null) {
            return;
        }
        if (htmlElementDescriptor.getDataUrl() != null) {
            element.addAttribute(new Attribute("data-url", htmlElementDescriptor.getDataUrl()));
        } else if (htmlElementDescriptor.getDataSource() != null && htmlElementDescriptor.getDataSource().equals(DictTypeEnum.DICT_ENUM.getCode()) && stringHasValue(htmlElementDescriptor.getEnumClassName())) {
            element.addAttribute(new Attribute("data-url", "/system/enum/options/" + htmlElementDescriptor.getEnumClassName()));
        } else if (defaultDataUrl != null) {
            element.addAttribute(new Attribute("data-url", defaultDataUrl));
        }
    }

    /**
     * 添加layui输入框的后缀
     */
    protected void addLayuiInputSuffix(HtmlElement element, HtmlElementDescriptor htmlElementDescriptor) {
        if (htmlElementDescriptor == null) {
            return;
        }
        HtmlElementTagTypeEnum tagType = HtmlElementTagTypeEnum.ofCodeName(htmlElementDescriptor.getTagType());
        switch (tagType) {
            case SELECT:
                HtmlElementDataSourceEnum anEnum = HtmlElementDataSourceEnum.getEnum(htmlElementDescriptor.getDataSource());
                if (anEnum != null) {
                    switch (anEnum) {
                        case DEPARTMENT:
                            addIconToParent(addDivWithClassToParent(element, "layui-input-suffix"), "layui-icon","layui-icon-transfer");
                            break;
                        case USER:
                            addIconToParent(addDivWithClassToParent(element, "layui-input-suffix"), "layui-icon","layui-icon-username");
                            break;
                        case ROLE:
                        case ORGANIZATION:
                            addIconToParent(addDivWithClassToParent(element, "layui-input-suffix"), "layui-icon","layui-icon-user");
                            break;
                        case INNER_TABLE:
                            addIconToParent(addDivWithClassToParent(element, "layui-input-suffix"), "layui-icon","layui-icon-table");
                            break;
                        case DICT_MODULE:
                            addIconToParent(addDivWithClassToParent(element, "layui-input-suffix"), "layui-icon","layui-icon-set");
                            break;
                        default:
                            addIconToParent(addDivWithClassToParent(element, "layui-input-suffix"), "layui-icon","layui-icon-more-vertical");
                            break;
                    }
                }
                break;
            case DATE:
                addIconToParent(addDivWithClassToParent(element, "layui-input-suffix"), "layui-icon","layui-icon-date");
                break;
            default:
                break;
        }
    }
}
