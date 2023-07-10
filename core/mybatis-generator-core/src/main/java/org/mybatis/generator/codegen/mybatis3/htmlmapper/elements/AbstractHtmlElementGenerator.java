package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.HtmlConstant;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.internal.util.VoGenService;

import java.util.List;

public abstract class AbstractHtmlElementGenerator extends AbstractGenerator implements HtmlConstant {

    protected HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    protected HtmlElementDescriptor htmlElementDescriptor;

    protected IntrospectedColumn introspectedColumn;

    protected VoGenService voGenService;

    protected AbstractHtmlElementGenerator(GeneratorInitialParameters generatorInitialParameters,IntrospectedColumn introspectedColumn) {
        super(generatorInitialParameters.getContext(), generatorInitialParameters.getIntrospectedTable(), generatorInitialParameters.getWarnings(), generatorInitialParameters.getProgressCallback());
        this.introspectedColumn = introspectedColumn;
        this.voGenService = new VoGenService(this.introspectedTable);
    }

    public abstract void addHtmlElement(HtmlElement parent);

    protected HtmlElement generateHtmlInput(boolean isHidden, boolean isTextArea) {
        return generateHtmlInput(this.introspectedColumn.getJavaProperty(), isHidden, isTextArea);
    }

    protected HtmlElement generateHtmlInput(String name, boolean isHidden, boolean isTextArea) {
        String type = isTextArea ? "textarea" : "input";
        HtmlElement input = new HtmlElement(type);
        input.addAttribute(new Attribute("id", name));
        input.addAttribute(new Attribute("name", name));
        if (isHidden) {
            addClassNameToElement(input, "layui-hide");
            //input.addAttribute(new Attribute("type", "hidden"));
        } else {
            input.addAttribute(new Attribute("type", "text"));
        }
        return input;
    }

    /**
     * 生成thymeleaf模板的值部分
     *
     * @return thymeleaf模板的值部分
     */
    protected String thymeleafValue() {
        return thymeleafValue(this.introspectedColumn.getJavaProperty(), GenerateUtils.getEntityKeyStr(introspectedTable));
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

    /**
     * 为元素添加class属性
     *
     * @param element   元素
     * @param className class名称
     */
    protected void addClassNameToElement(HtmlElement element, String className) {
        Attribute htmlClass = element.getAttributes().stream().filter(attribute -> "class".equalsIgnoreCase(attribute.getName())).findFirst().orElse(null);
        if (htmlClass == null) {
            element.addAttribute(new Attribute("class", className));
        } else {
            if (!htmlClass.getValue().contains(className)) {
                htmlClass.setValue(htmlClass.getValue() + " " + className);
            }
        }
    }

    /**
     * 在父元素中添加一个带有class属性的div元素
     * @param parent    父元素
     * @param className class名称
     * @return  div元素
     */
    protected HtmlElement addDivWithClassToParent(HtmlElement parent, String className) {
        HtmlElement div = new HtmlElement("div");
        if (!className.isEmpty()) {
            addClassNameToElement(div, className);
        }
        parent.addElement(div);
        return div;
    }

    public HtmlElementDescriptor getHtmlElementDescriptor() {
        return htmlElementDescriptor;
    }

    public void setHtmlElementDescriptor(HtmlElementDescriptor htmlElementDescriptor) {
        this.htmlElementDescriptor = htmlElementDescriptor;
    }

    public HtmlGeneratorConfiguration getHtmlGeneratorConfiguration() {
        return htmlGeneratorConfiguration;
    }

    public void setHtmlGeneratorConfiguration(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }

    protected HtmlElement drawRadio(String propertyName, String value, String text, String entityKey) {
        HtmlElement element = new HtmlElement("input");
        element.addAttribute(new Attribute("name", propertyName));
        element.addAttribute(new Attribute("type", "radio"));
        element.addAttribute(new Attribute("value", value));
        element.addAttribute(new Attribute("title", text));
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        sb.append("${").append(entityKey).append("?.");
        sb.append(propertyName).append("} eq ");
        sb.append("'").append(value).append("'");
        element.addAttribute(new Attribute("th:checked", sb.toString()));
        return element;
    }
    public abstract String getFieldValueFormatPattern();

    protected boolean isDateType() {
        return GenerateUtils.isDateType(introspectedColumn);
    }

    protected boolean isReadonly(){
        return this.htmlGeneratorConfiguration.getReadonlyColumnNames().contains(introspectedColumn.getActualColumnName());
    }
}
