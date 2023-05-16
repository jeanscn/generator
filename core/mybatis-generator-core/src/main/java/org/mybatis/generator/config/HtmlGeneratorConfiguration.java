package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HtmlGeneratorConfiguration extends AbstractGeneratorConfiguration {

    private String viewPath;

    private String htmlFileName;

    private boolean overWriteFile;

    private Set<String> hiddenColumns;

    //指定页面表单不允许为空的字段
    private List<String> elementRequired;

    private List<HtmlElementDescriptor> elementDescriptors;

    private HtmlLayoutDescriptor layoutDescriptor;

    public HtmlGeneratorConfiguration(Context context, TableConfiguration tc) {
        super();
        hiddenColumns = new HashSet<>();
        elementRequired = new ArrayList<>();
        elementDescriptors = new ArrayList<>();
        overWriteFile = false;
        targetProject = "src/main/resources/templates";
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

    public Set<String> getHiddenColumns() {
        return hiddenColumns;
    }

    public void setHiddenColumns(Set<String> hiddenColumns) {
        this.hiddenColumns = hiddenColumns;
    }

    public List<String> getElementRequired() {
        return elementRequired;
    }

    public void setElementRequired(List<String> elementRequired) {
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
}
