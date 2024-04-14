package org.mybatis.generator.config;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.HtmlDocumentTypeEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HtmlGeneratorConfiguration extends AbstractGeneratorConfiguration {

    private HtmlDocumentTypeEnum type;

    private String viewPath;

    private String title;

    private String simpleViewPath;

    private String htmlFileName;

    private boolean overWriteHtmlFile;

    private boolean overWriteCssFile;

    private boolean overWriteJsFile;

    private boolean defaultConfig = true;

    private boolean overWriteVueFile = true;

    private final Set<String> hiddenColumnNames = new HashSet<>();

    private final Set<String> readonlyFields = new HashSet<>();

    private final Set<String> displayOnlyFields = new HashSet<>();

    private final Set<IntrospectedColumn> hiddenColumns = new HashSet<>();

    //指定页面表单不允许为空的字段
    private final Set<String> elementRequired = new HashSet<>();

    private final List<HtmlElementDescriptor> elementDescriptors = new ArrayList<>();

    private HtmlLayoutDescriptor layoutDescriptor;

    private HtmlElementInnerListConfiguration htmlElementInnerListConfiguration;

    private String htmlBaseTargetPackage;

    private List<HtmlFileAttachmentConfiguration> htmlFileAttachmentConfiguration = new ArrayList<>();

    private final List<HtmlApprovalCommentConfiguration> htmlApprovalCommentConfigurations = new ArrayList<>();

    private final Set<HtmlButtonGeneratorConfiguration> htmlButtons = new HashSet<>();

    public HtmlGeneratorConfiguration(Context context, TableConfiguration tc) {
        super();
        overWriteHtmlFile = false;
        overWriteCssFile = false;
        overWriteJsFile = false;
        targetProject = "src/main/resources/templates";
        this.context = context;
        type = HtmlDocumentTypeEnum.EDITABLE;
    }

    @Override
    protected void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "HtmlMapGeneratorConfiguration");
    }

    public HtmlDocumentTypeEnum getType() {
        return type;
    }

    public void setType(HtmlDocumentTypeEnum type) {
        this.type = type;
    }

    public String getViewPath() {
        return viewPath;
    }

    public void setViewPath(String viewPath) {
        this.viewPath = viewPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Set<IntrospectedColumn> getHiddenColumns() {
        return hiddenColumns;
    }

    public Set<String> getElementRequired() {
        return elementRequired;
    }

    public Set<String> getDisplayOnlyFields() {
        return displayOnlyFields;
    }

    public List<HtmlElementDescriptor> getElementDescriptors() {
        return elementDescriptors;
    }

    public HtmlLayoutDescriptor getLayoutDescriptor() {
        return layoutDescriptor;
    }

    public void setLayoutDescriptor(HtmlLayoutDescriptor layoutDescriptor) {
        this.layoutDescriptor = layoutDescriptor;
    }

    public boolean isDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(boolean defaultConfig) {
        this.defaultConfig = defaultConfig;
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

    public List<HtmlFileAttachmentConfiguration> getHtmlFileAttachmentConfiguration() {
        return htmlFileAttachmentConfiguration;
    }

    public void setHtmlFileAttachmentConfiguration(List<HtmlFileAttachmentConfiguration> htmlFileAttachmentConfiguration) {
        this.htmlFileAttachmentConfiguration = htmlFileAttachmentConfiguration;
    }

    public List<HtmlApprovalCommentConfiguration> getHtmlApprovalCommentConfigurations() {
        return htmlApprovalCommentConfigurations;
    }

    public void addHtmlApprovalCommentConfiguration(HtmlApprovalCommentConfiguration htmlApprovalCommentConfiguration) {
        this.htmlApprovalCommentConfigurations.add(htmlApprovalCommentConfiguration);
    }

    public Set<String> getReadonlyFields() {
        return readonlyFields;
    }

    public Set<HtmlButtonGeneratorConfiguration> getHtmlButtons() {
        return htmlButtons;
    }

    public boolean isOverWriteVueFile() {
        return overWriteVueFile;
    }

    public void setOverWriteVueFile(boolean overWriteVueFile) {
        this.overWriteVueFile = overWriteVueFile;
    }
}
