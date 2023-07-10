package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui;

import com.vgosoft.core.constant.enums.IBaseEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 14:49
 * @version 3.0
 */

public class SwitchHtmlGenerator extends AbstractLayuiElementGenerator {
    public SwitchHtmlGenerator(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn) {
        super(generatorInitialParameters,introspectedColumn);
    }

    @Override
    public void addHtmlElement(HtmlElement parent) {
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        HtmlElement element = new HtmlElement("input");
        element.addAttribute(new Attribute("type", "checkbox"));
        element.addAttribute(new Attribute("lay-skin", "switch"));
        if (htmlElementDescriptor.getDataFormat() != null) {
            htmlElementDescriptor.setEnumClassName(htmlElementDescriptor.getEnumClassName());
        }
        String checkedValue = "1";
        if (htmlElementDescriptor.getSwitchText() != null) {
            element.addAttribute(new Attribute("lay-text", htmlElementDescriptor.getSwitchText()));
        } else if (htmlElementDescriptor.getEnumClassName() != null) {
            try {
                Class<?> aClass = Class.forName(htmlElementDescriptor.getEnumClassName());
                if (aClass.isEnum() && IBaseEnum.class.isAssignableFrom(aClass)) {
                    Object[] enumConstants = aClass.getEnumConstants();
                    if (enumConstants.length>1) {
                        element.addAttribute(
                                new Attribute("lay-text", ((IBaseEnum<?>) enumConstants[0]).codeName() + "|" + ((IBaseEnum<?>) enumConstants[1]).codeName())
                        );
                        checkedValue = ((IBaseEnum<?>) enumConstants[0]).code().toString();
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        element.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
        addDataUrl(element, htmlElementDescriptor, null);
        String sb = "${" + entityKey + "?." +  introspectedColumn.getJavaProperty() + "} eq " + checkedValue;
        element.addAttribute(new Attribute("th:checked", sb));
        parent.addElement(element);
        //增加提交数据的隐藏域
        element = new HtmlElement("input");
        element.addAttribute(new Attribute("id", introspectedColumn.getJavaProperty()));
        element.addAttribute(new Attribute("name", introspectedColumn.getJavaProperty()));
        element.addAttribute(new Attribute("type", "hidden"));
        element.addAttribute(new Attribute("th:value", thymeleafValue()));
        parent.addElement(element);

        //读写状态区
        addClassNameToElement(parent, "oas-form-item-edit");
        parent.addAttribute(new Attribute("for-type", "lay-switch"));
        //在parent中添加data-data属性，用于保存初始值
        parent.addAttribute(new Attribute("th:data-data", thymeleafValue()));
        //在parent中添加data-field属性，用于保存属性名
        parent.addAttribute(new Attribute("data-field", introspectedColumn.getJavaProperty()));
        //非空验证
        addElementVerify(introspectedColumn.getActualColumnName(), element, this.htmlElementDescriptor);
    }

    @Override
    public String getFieldValueFormatPattern() {
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedColumn.getIntrospectedTable());
        StringBuilder sb = new StringBuilder();
        sb.append("${").append(entityKey).append("?.").append(introspectedColumn.getJavaProperty());
        String str = null;
        if ("DictEnum".equals(htmlElementDescriptor.getDataSource()) && htmlElementDescriptor.getEnumClassName() != null) {
            try {
                Class<?> aClass = Class.forName(htmlElementDescriptor.getEnumClassName());
                if (aClass.isEnum() && IBaseEnum.class.isAssignableFrom(aClass)) {
                    Object[] enumConstants = aClass.getEnumConstants();
                    if (enumConstants.length>1) {
                        IBaseEnum<?> enum1 = (IBaseEnum<?>) enumConstants[0];
                        IBaseEnum<?> enum2 = (IBaseEnum<?>) enumConstants[1];
                        str = VStringUtil.format("} eq {0} ? {1} : {2}", enum1.code(), enum1.codeName(), enum2.codeName());
                    }else if(enumConstants.length>0){
                        IBaseEnum<?> enum1 = (IBaseEnum<?>) enumConstants[0];
                        str = VStringUtil.format("} eq {0} ? {1} : {2}", enum1.code(), enum1.codeName(), enum1.codeName());
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (str == null && htmlElementDescriptor.getDataFormat()!=null) {
                str = "} eq 1 ? '是':'否'";
            }
        } else {
            str = "}?:_";
        }
        sb.append(str);
        return sb.toString();
    }
}
