package org.mybatis.generator.config;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.*;

public class HtmlElementDescriptor  extends AbstractHtmlElementDescriptor{

    private IntrospectedColumn column;

    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    private String name;

    private String tagType;

    private String dataFormat;

    private String otherFieldName;

    private String dataSource;

    private String beanName;

    private String applyProperty;

    private String applyPropertyKey;

    private List<String> verify = new ArrayList<>();

    private String enumClassName;

    private String switchText;

    private String dictCode;

    private String callback;

    private String labelCss;

    private String elementCss;

    private String dataFmt;

    private boolean dateRange = false;

    private String listViewClass;

    private boolean multiple;

    private boolean remoteApiParse = false;

    private boolean remoteToTree = false;

    private String remoteValueType;

    private boolean remoteAsync = false;

    private String keyMapLabel;

    private String keyMapValue;
    //是否排除自己
    private boolean excludeSelf = false;

    private String parentFormKey;

    private String designIdField;
    private String configJsonfield = "jsonContent";

    private Set<String> watchFields = new HashSet<>();
    private String dateRangeSeparator;
    private boolean renderHref;
    private String hrefDataKeyField;
    private String tips;
    private final List<HtmlHrefElementConfiguration> htmlHrefElementConfigurations = new ArrayList<>();

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
            return Mb3GenUtil.getEnumClassNameByDataFmt(this.dataFormat);
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
            return Mb3GenUtil.getSwitchTextByDataFmt(this.dataFormat);
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

    public String getDataFmt() {
        return dataFmt;
    }

    public void setDataFmt(String dataFmt) {
        this.dataFmt = dataFmt;
    }

    public boolean isDateRange() {
        return dateRange;
    }

    public void setDateRange(boolean dateRange) {
        this.dateRange = dateRange;
    }

    public String getListViewClass() {
        return listViewClass;
    }

    public void setListViewClass(String listViewClass) {
        this.listViewClass = listViewClass;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public List<HtmlHrefElementConfiguration> getHtmlHrefElementConfigurations() {
        return htmlHrefElementConfigurations;
    }

    public boolean isRemoteApiParse() {
        return remoteApiParse;
    }

    public void setRemoteApiParse(boolean remoteApiParse) {
        this.remoteApiParse = remoteApiParse;
    }

    public String getKeyMapLabel() {
        return keyMapLabel;
    }

    public void setKeyMapLabel(String keyMapLabel) {
        this.keyMapLabel = keyMapLabel;
    }

    public String getKeyMapValue() {
        return keyMapValue;
    }

    public void setKeyMapValue(String keyMapValue) {
        this.keyMapValue = keyMapValue;
    }

    public boolean isRemoteToTree() {
        return remoteToTree;
    }

    public void setRemoteToTree(boolean remoteToTree) {
        this.remoteToTree = remoteToTree;
    }

    public String getRemoteValueType() {
        return remoteValueType;
    }

    public void setRemoteValueType(String remoteValueType) {
        this.remoteValueType = remoteValueType;
    }

    public boolean isRemoteAsync() {
        return remoteAsync;
    }

    public void setRemoteAsync(boolean remoteAsync) {
        this.remoteAsync = remoteAsync;
    }

    public boolean isExcludeSelf() {
        return excludeSelf;
    }

    public void setExcludeSelf(boolean excludeSelf) {
        this.excludeSelf = excludeSelf;
    }

    public String getApplyPropertyKey() {
        return applyPropertyKey;
    }

    public void setApplyPropertyKey(String applyPropertyKey) {
        this.applyPropertyKey = applyPropertyKey;
    }

    public String getParentFormKey() {
        return parentFormKey;
    }

    public void setParentFormKey(String parentFormKey) {
        this.parentFormKey = parentFormKey;
    }

    public String getDesignIdField() {
        return designIdField;
    }

    public void setDesignIdField(String designIdField) {
        this.designIdField = designIdField;
    }

    public String getConfigJsonfield() {
        return configJsonfield;
    }

    public void setConfigJsonfield(String configJsonfield) {
        this.configJsonfield = configJsonfield;
    }

    public Set<String> getWatchFields() {
        return watchFields;
    }

    public void setWatchFields(Set<String> watchFields) {
        this.watchFields = watchFields;
    }

    public String getDateRangeSeparator() {
        return dateRangeSeparator;
    }

    public void setDateRangeSeparator(String dateRangeSeparator) {
        this.dateRangeSeparator = dateRangeSeparator;
    }

    public boolean isRenderHref() {
        return renderHref;
    }

    public void setRenderHref(boolean renderHref) {
        this.renderHref = renderHref;
    }

    public String getHrefDataKeyField() {
        return hrefDataKeyField;
    }

    public void setHrefDataKeyField(String hrefDataKeyField) {
        this.hrefDataKeyField = hrefDataKeyField;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
