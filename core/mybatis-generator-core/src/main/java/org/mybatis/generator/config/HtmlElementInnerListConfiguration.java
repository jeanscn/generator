package org.mybatis.generator.config;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-06-12 13:43
 * @version 4.0
 */
public class HtmlElementInnerListConfiguration extends PropertyHolder{

    private String tagId;

    private String dataField;

    private String dataUrl;

    private String sourceViewPath;

    private String sourceBeanName;

    private String appKey;

    private String relationField;

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

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getRelationField() {
        return relationField;
    }

    public void setRelationField(String relationField) {
        this.relationField = relationField;
    }
}
