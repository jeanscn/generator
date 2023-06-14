package org.mybatis.generator.config;

import org.mybatis.generator.api.IntrospectedColumn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HtmlGeneratorConfiguration extends AbstractGeneratorConfiguration {

    private String viewPath;

    private String simpleViewPath;

    private String htmlFileName;

    private boolean overWriteFile;

    private Set<String> hiddenColumnNames = new HashSet<>();

    private Set<IntrospectedColumn> hiddenColumns = new HashSet<>();

    //指定页面表单不允许为空的字段
    private Set<String> elementRequired = new HashSet<>();

    private List<HtmlElementDescriptor> elementDescriptors = new ArrayList<>();

    private HtmlLayoutDescriptor layoutDescriptor;

    private HtmlElementInnerListConfiguration htmlElementInnerListConfiguration;

    private String htmlBaseTargetPackage;

    public HtmlGeneratorConfiguration(Context context, TableConfiguration tc) {
        super();
        overWriteFile = false;
        targetProject = "src/main/resources/templates";
        this.context = context;
    }

    @Override
    protected void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "HtmlMapGeneratorConfiguration");
    }

    public String getViewPath() {
        return viewPath;
    }

    public void setViewPath(String viewPath) {
        this.viewPath = viewPath;
    }

    public String getHtmlFileName() {
        return htmlFileName;
    }

    public void setHtmlFileName(String htmlFileName) {
        this.htmlFileName = htmlFileName;
    }

    public boolean isOverWriteFile() {
        return overWriteFile;
    }

    public void setOverWriteFile(boolean overWriteFile) {
        this.overWriteFile = overWriteFile;
    }

    public Set<String> getHiddenColumnNames() {
        return hiddenColumnNames;
    }

    public void setHiddenColumnNames(Set<String> hiddenColumnNames) {
        this.hiddenColumnNames = hiddenColumnNames;
    }

    public Set<IntrospectedColumn> getHiddenColumns() {
        return hiddenColumns;
    }

    public void setHiddenColumns(Set<IntrospectedColumn> hiddenColumns) {
        this.hiddenColumns = hiddenColumns;
    }

    public Set<String> getElementRequired() {
        return elementRequired;
    }

    public void setElementRequired(Set<String> elementRequired) {
        this.elementRequired = elementRequired;
    }

    public List<HtmlElementDescriptor> getElementDescriptors() {
        return elementDescriptors;
    }

    public void setElementDescriptors(List<HtmlElementDescriptor> elementDescriptors) {
        this.elementDescriptors = elementDescriptors;
    }

    public HtmlLayoutDescriptor getLayoutDescriptor() {
        return layoutDescriptor;
    }

    public void setLayoutDescriptor(HtmlLayoutDescriptor layoutDescriptor) {
        this.layoutDescriptor = layoutDescriptor;
    }

    public List<HtmlElementDescriptor> addElementDescriptors(HtmlElementDescriptor htmlElementDescriptor) {
        this.elementDescriptors.add(htmlElementDescriptor);
        return this.elementDescriptors;
    }

    public String getSimpleViewPath() {
        return simpleViewPath;
    }

    public void setSimpleViewPath(String simpleViewPath) {
        this.simpleViewPath = simpleViewPath;
    }

    public String getHtmlBaseTargetPackage() {
        return htmlBaseTargetPackage;
    }

    public void setHtmlBaseTargetPackage(String htmlBaseTargetPackage) {
        this.htmlBaseTargetPackage = htmlBaseTargetPackage;
    }

    public HtmlElementInnerListConfiguration getHtmlElementInnerListConfiguration() {
        return htmlElementInnerListConfiguration;
    }

    public void setHtmlElementInnerListConfiguration(HtmlElementInnerListConfiguration htmlElementInnerListConfiguration) {
        this.htmlElementInnerListConfiguration = htmlElementInnerListConfiguration;
    }
}
