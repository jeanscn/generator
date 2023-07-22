package org.mybatis.generator.config;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class HtmlElementDescriptor  extends PropertyHolder{

    private IntrospectedColumn column;

    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

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

    private String labelCss;

    private String elementCss;

    public HtmlElementDescriptor() {
    }

    public HtmlElementDescriptor(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HtmlElementDescriptor)) return false;
        HtmlElementDescriptor that = (HtmlElementDescriptor) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
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
        if (enumClassName != null) {
            return enumClassName;
        } else  if(VStringUtil.stringHasValue(this.dataFormat)){
            switch (this.dataFormat) {
                case "exist":
                case "有":
                case "有无":
                    return "com.vgosoft.core.constant.enums.core.ExistOrNotEnum";
                case "yes":
                case "true":
                case "是":
                case "是否":
                    return "com.vgosoft.core.constant.enums.core.YesNoEnum";
                case "sex":
                case "性别":
                    return "com.vgosoft.core.constant.enums.core.GenderEnum";
                case "启停":
                case "启用停用":
                case "state":
                    return "com.vgosoft.core.constant.enums.core.CommonStatusEnum";
                case "急":
                case "缓急":
                    return "com.vgosoft.core.constant.enums.core.UrgencyEnum";
                case "level":
                case "级别":
                    return "com.vgosoft.core.constant.enums.core.LevelListEnum";
                default:
                    return null;
            }
        }
        return null;
    }

    public void setEnumClassName(String enumClassName) {
        this.enumClassName = enumClassName;
    }

    public String getSwitchText() {
        if (VStringUtil.stringHasValue(switchText)) {
            return switchText;
        }else if(VStringUtil.stringHasValue(this.dataFormat)){
            switch (this.dataFormat) {
                case "exist":
                case "有":
                case "有无":
                    return "有|无";
                case "yes":
                case "true":
                case "是":
                case "是否":
                    return "是|否";
                case "sex":
                case "性别":
                    return "男|女";
                case "启停":
                case "启用停用":
                case "state":
                    return "启用|停用";
                case "急":
                case "缓急":
                    return "急|";
                default:
                    return null;
            }
        }
        return null;
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

    public void setHtmlGeneratorConfiguration(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }

    public String getLabelCss() {
        return labelCss;
    }

    public void setLabelCss(String labelCss) {
        this.labelCss = labelCss;
    }

    public String getElementCss() {
        return elementCss;
    }

    public void setElementCss(String elementCss) {
        this.elementCss = elementCss;
    }
}
