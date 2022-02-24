package org.mybatis.generator.custom;

import org.mybatis.generator.api.IntrospectedColumn;

public class HtmlElementDescriptor {

    private IntrospectedColumn column;

    private String name;

    private String tagType;

    private String dataUrl;

    private String dataFormat;

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public IntrospectedColumn getColumn() {
        return column;
    }

    public void setColumn(IntrospectedColumn column) {
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }
}
