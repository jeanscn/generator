package org.mybatis.generator.config;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-06-12 13:43
 * @version 4.0
 */
public class HtmlElementInnerListConfiguration extends PropertyHolder{

    private String listKey;

    private String moduleKeyword;

    private String tagId;

    private String dataField;

    private String dataUrl;

    private String sourceViewPath;

    private String sourceBeanName;

    private String sourceViewVoClass;

    private String relationField;

    private String relationKey;

    private String afterColumn;

    public HtmlElementInnerListConfiguration() {
    }

    public String getDataField() {
        return dataField;
    }

    public void setDataField(String dataField) {
        this.dataField = dataField;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getSourceViewPath() {
        if (sourceViewPath.contains("/")) {
            sourceViewPath = sourceViewPath.substring(sourceViewPath.lastIndexOf("/")+1);
        }
        return sourceViewPath;
    }

    public void setSourceViewPath(String sourceViewPath) {
        this.sourceViewPath = sourceViewPath;
    }

    public String getSourceBeanName() {
        return sourceBeanName;
    }

    public void setSourceBeanName(String sourceBeanName) {
        this.sourceBeanName = sourceBeanName;
    }

    public String getModuleKeyword() {
        return moduleKeyword;
    }

    public void setModuleKeyword(String moduleKeyword) {
        this.moduleKeyword = moduleKeyword;
    }

    public String getRelationField() {
        return relationField;
    }

    public void setRelationField(String relationField) {
        this.relationField = relationField;
    }

    public String getRelationKey() {
        return relationKey;
    }

    public void setRelationKey(String relationKey) {
        this.relationKey = relationKey;
    }

    public String getListKey() {
        return listKey;
    }

    public void setListKey(String listKey) {
        this.listKey = listKey;
    }

    public String getAfterColumn() {
        return afterColumn;
    }

    public void setAfterColumn(String afterColumn) {
        this.afterColumn = afterColumn;
    }

    public String getSourceViewVoClass() {
        return sourceViewVoClass;
    }

    public void setSourceViewVoClass(String sourceViewVoClass) {
        this.sourceViewVoClass = sourceViewVoClass;
    }
}