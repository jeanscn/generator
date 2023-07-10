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

    private boolean overWriteHtmlFile;

    private boolean overWriteCssFile;

    private boolean overWriteJsFile;

    private Set<String> hiddenColumnNames = new HashSet<>();

    private Set<String> readonlyColumnNames = new HashSet<>();

    private Set<IntrospectedColumn> hiddenColumns = new HashSet<>();

    //指定页面表单不允许为空的字段
    private Set<String> elementRequired = new HashSet<>();

    private List<HtmlElementDescriptor> elementDescriptors = new ArrayList<>();

    private HtmlLayoutDescriptor layoutDescriptor;

    private HtmlElementInnerListConfiguration htmlElementInnerListConfiguration;

    private String htmlBaseTargetPackage;

    private HtmlFileAttachmentConfiguration htmlFileAttachmentConfiguration;

    private List<HtmlApprovalCommentConfiguration> htmlApprovalCommentConfigurations = new ArrayList<>();

    public HtmlGeneratorConfiguration(Context context, TableConfiguration tc) {
        super();
        overWriteHtmlFile = false;
        overWriteCssFile = false;
        overWriteJsFile = false;
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

    public boolean isOverWriteHtmlFile() {
        return overWriteHtmlFile;
    }

    public void setOverWriteHtmlFile(boolean overWriteHtmlFile) {
        this.overWriteHtmlFile = overWriteHtmlFile;
    }

    public boolean isOverWriteCssFile() {
        return overWriteCssFile;
    }

    public void setOverWriteCssFile(boolean overWriteCssFile) {
        this.overWriteCssFile = overWriteCssFile;
    }

    public boolean isOverWriteJsFile() {
        return overWriteJsFile;
    }

    public void setOverWriteJsFile(boolean overWriteJsFile) {
        this.overWriteJsFile = overWriteJsFile;
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

    public HtmlFileAttachmentConfiguration getHtmlFileAttachmentConfiguration() {
        return htmlFileAttachmentConfiguration;
    }

    public void setHtmlFileAttachmentConfiguration(HtmlFileAttachmentConfiguration htmlFileAttachmentConfiguration) {
        this.htmlFileAttachmentConfiguration = htmlFileAttachmentConfiguration;
    }

    public List<HtmlApprovalCommentConfiguration> getHtmlApprovalCommentConfigurations() {
        return htmlApprovalCommentConfigurations;
    }

    public void addHtmlApprovalCommentConfiguration(HtmlApprovalCommentConfiguration htmlApprovalCommentConfiguration) {
        this.htmlApprovalCommentConfigurations.add(htmlApprovalCommentConfiguration);
    }

    public Set<String> getReadonlyColumnNames() {
        return readonlyColumnNames;
    }

    public void setReadonlyColumnNames(Set<String> readonlyColumnNames) {
        this.readonlyColumnNames = readonlyColumnNames;
    }
}
