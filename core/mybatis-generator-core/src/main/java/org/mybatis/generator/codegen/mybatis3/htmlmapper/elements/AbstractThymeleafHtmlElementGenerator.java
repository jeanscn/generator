package org.mybatis.generator.codegen.mybatis3.htmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.AbstractThymeleafHtmlGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.HtmlConstant;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.enums.ThymeleafValueScopeEnum;

public abstract class AbstractThymeleafHtmlElementGenerator extends AbstractThymeleafHtmlGenerator implements HtmlConstant {

    protected HtmlElementDescriptor htmlElementDescriptor;

    protected IntrospectedColumn introspectedColumn;


    protected AbstractThymeleafHtmlElementGenerator(GeneratorInitialParameters generatorInitialParameters, IntrospectedColumn introspectedColumn,HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters.getContext(), generatorInitialParameters.getIntrospectedTable(), generatorInitialParameters.getWarnings(), generatorInitialParameters.getProgressCallback(),htmlGeneratorConfiguration);
        this.introspectedColumn = introspectedColumn;
    }

    public abstract void addHtmlElement(HtmlElement parent);

    protected HtmlElement generateHtmlInput(boolean isHidden, boolean isTextArea) {
        return super.generateHtmlInput(this.introspectedColumn.getJavaProperty(), isHidden, isTextArea, true, true);
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

    /**
     * 生成thymeleaf模板的值部分
     * @param scopeEnum thymeleaf值的作用域
     *
     * @return thymeleaf模板的值部分
     */
    public abstract String getFieldValueFormatPattern(ThymeleafValueScopeEnum scopeEnum);


    protected String thymeleafValue(ThymeleafValueScopeEnum scopeEnum) {
        return super.thymeleafValue(this.introspectedColumn, scopeEnum, this.htmlElementDescriptor);
    }
}
