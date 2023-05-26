package org.mybatis.generator.config;

import org.mybatis.generator.api.IntrospectedColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HtmlElementDescriptor {

    private IntrospectedColumn column;

    private final HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    private String name;

    private String tagType;

    private String dataUrl;

    private String dataFormat;

    private String otherFieldName;

    private String dataSource;

    private String beanName;

    private String applyProperty;

    private List<String> verify = new ArrayList<>();

    private String enumClassName;

    private String switchText;

    private String dictCode;

    private String callback;

    public HtmlElementDescriptor(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }

    public HtmlGeneratorConfiguration getHtmlGeneratorConfiguration() {
        return htmlGeneratorConfiguration;
    }

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

    public String getOtherFieldName() {
        return otherFieldName;
    }

    public void setOtherFieldName(String otherFieldName) {
        this.otherFieldName = otherFieldName;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getApplyProperty() {
        return applyProperty;
    }

    public void setApplyProperty(String applyProperty) {
        this.applyProperty = applyProperty;
    }

    public List<String> getVerify() {
        return verify;
    }

    public void setVerify(List<String> verify) {
        this.verify = verify;
    }

    public String getEnumClassName() {
        return enumClassName;
    }

    public void setEnumClassName(String enumClassName) {
        this.enumClassName = enumClassName;
    }

    public String getSwitchText() {
        return switchText;
    }

    public void setSwitchText(String switchText) {
        this.switchText = switchText;
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
}
