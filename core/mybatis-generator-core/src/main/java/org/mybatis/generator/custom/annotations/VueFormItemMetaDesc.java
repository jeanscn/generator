package org.mybatis.generator.custom.annotations;

import cn.hutool.core.annotation.Alias;
import com.vgosoft.core.annotation.VueFormItemMeta;
import com.vgosoft.core.annotation.VueFormItemRule;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.internal.util.Mb3GenUtil;

/**
 * ElementPlus表单元数据描述注解
 *
 * @version 6.0
 */
public class VueFormItemMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + VueFormItemMeta.class.getSimpleName();

    private String fieldValue;
    private final String fieldName;
    private final String fieldLabel;
    private Integer span = 24;
    private String tips;
    private String component;
    private String rules;
    private String hideHandle;
    private String requiredHandle;
    // 以下为options属性
    private String placeholder; // 提示文字
    private String maxlength; // 最大长度
    private String minlength; // 最小长度
    private Boolean multiple; // 是否允许多选
    // 类型,input-可能是text、password、url、textarea；date-可能是time、datetime
    private String type;
    private String valueFormat; // 日期格式化
    private boolean dateRange = false; // 是否为日期范围
    private boolean remoteApiParse = false; // 是否解析远程数据源的结果，如果为true，则远程数据源返回的数据需要进行转换label和value。如果指定了keyMapLabel和keyMapValue，则按照keyMap转换，否则按照默认规则转换label-name，value-id
    private String keyMapLabel = "name"; // 远程数据源返回的数据中，label对应的字段名,remoteApiParse为true时有效
    private String keyMapValue = "id"; // 远程数据源返回的数据中，value对应的字段名,remoteApiParse为true时有效
    private String dataUrl = ""; // 远程数据源的URL
    private String dataSource = "";
    private String beanName = "";
    private String applyProperty = "";
    private String enumClassFullName = "";
    private String dictCode = "";
    private String callback = "";

    private String labelCss = "";
    private String elementCss = "";
    private String switchText = "";


    public static VueFormItemMetaDesc create(IntrospectedColumn introspectedColumn) {
        return new VueFormItemMetaDesc(introspectedColumn);
    }

    public VueFormItemMetaDesc(IntrospectedColumn introspectedColumn) {
        super();
        this.fieldName = introspectedColumn.getJavaProperty();
        items.add(VStringUtil.format("fieldName = \"{0}\"", this.getFieldName()));
        this.fieldLabel = introspectedColumn.getRemarks(true);
        items.add(VStringUtil.format("fieldLabel = \"{0}\"", this.getFieldLabel()));
        this.addImports(VueFormItemMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        if (this.span != 24) {
            items.add(VStringUtil.format("span = {0}", this.getSpan()));
        }
        if (VStringUtil.isNotBlank(this.getFieldValue())) {
            items.add(VStringUtil.format("fieldValue = \"{0}\"", this.getFieldValue()));
        }
        if (VStringUtil.isNotBlank(this.getTips())) {
            items.add(VStringUtil.format("tips = \"{0}\"", this.getTips()));
        }
        if (VStringUtil.isNotBlank(this.getComponent()) && !"input".equals(this.getComponent())) {
            items.add(VStringUtil.format("component = \"{0}\"", this.getComponent()));
        }
        if (VStringUtil.isNotBlank(this.getRules())) {
            this.addImports(VueFormItemRule.class.getCanonicalName());
            items.add(VStringUtil.format("\n            rules = '{'{0}'}'\n            ", this.getRules()));
        }
        if (VStringUtil.isNotBlank(this.getHideHandle())) {
            items.add(VStringUtil.format("hideHandle = \"{0}\"", this.getHideHandle()));
        }
        if (VStringUtil.isNotBlank(this.getRequiredHandle())) {
            items.add(VStringUtil.format("requiredHandle = \"{0}\"", this.getRequiredHandle()));
        }
        if (VStringUtil.isNotBlank(this.getPlaceholder())) {
            items.add(VStringUtil.format("placeholder = \"{0}\"", this.getPlaceholder()));
        }
        if (VStringUtil.isNotBlank(this.getMaxlength())) {
            items.add(VStringUtil.format("maxlength = \"{0}\"", this.getMaxlength()));
        }
        if (VStringUtil.isNotBlank(this.getMinlength()) && !"0".equals(this.getMinlength())) {
            items.add(VStringUtil.format("minlength = \"{0}\"", this.getMinlength()));
        }
        if (this.getMultiple() != null && this.getMultiple()){
            items.add(VStringUtil.format("multiple = {0}", this.getMultiple().toString().toLowerCase()));
        }
        if (VStringUtil.isNotBlank(this.getType())) {
            items.add(VStringUtil.format("type = \"{0}\"", this.getType()));
        }
        if (VStringUtil.isNotBlank(this.getValueFormat())) {
            items.add(VStringUtil.format("valueFormat = \"{0}\"", this.getValueFormat()));
        }
        if (this.isDateRange()) {
            items.add(VStringUtil.format("dateRange = true"));
        }
        if (this.isRemoteApiParse()) {
            items.add(VStringUtil.format("remoteApiParse = true"));
        }
        if (VStringUtil.isNotBlank(this.getKeyMapLabel()) && !"name".equals(this.getKeyMapLabel())){
            items.add(VStringUtil.format("keyMapLabel = \"{0}\"", this.getKeyMapLabel()));
        }
        if (VStringUtil.isNotBlank(this.getKeyMapValue()) && !"id".equals(this.getKeyMapValue())){
            items.add(VStringUtil.format("keyMapValue = \"{0}\"", this.getKeyMapValue()));
        }
        if (VStringUtil.isNotBlank(this.getDataUrl())) {
            items.add(VStringUtil.format("dataUrl = \"{0}\"", this.getDataUrl()));
        }
        if (VStringUtil.isNotBlank(this.getDataSource())) {
            items.add(VStringUtil.format("dataSource = \"{0}\"", this.getDataSource()));
        }
        if (VStringUtil.isNotBlank(this.getBeanName())) {
            items.add(VStringUtil.format("beanName = \"{0}\"", this.getBeanName()));
        }
        if (VStringUtil.isNotBlank(this.getApplyProperty())) {
            items.add(VStringUtil.format("applyProperty = \"{0}\"", this.getApplyProperty()));
        }
        if (VStringUtil.isNotBlank(this.getEnumClassFullName())) {
            items.add(VStringUtil.format("enumClassFullName = \"{0}\"", this.getEnumClassFullName()));
        }
        if (VStringUtil.isNotBlank(this.getDictCode())) {
            items.add(VStringUtil.format("dictCode = \"{0}\"", this.getDictCode()));
        }
        if (VStringUtil.isNotBlank(this.getCallback())) {
            items.add(VStringUtil.format("callback = \"{0}\"", this.getCallback()));
        }
        if (VStringUtil.isNotBlank(this.getLabelCss())) {
            items.add(VStringUtil.format("labelCss = \"{0}\"", this.getLabelCss()));
        }
        if (VStringUtil.isNotBlank(this.getElementCss())) {
            items.add(VStringUtil.format("elementCss = \"{0}\"", this.getElementCss()));
        }
        if (VStringUtil.isNotBlank(this.getSwitchText())) {
            items.add(VStringUtil.format("switchText = \"{0}\"", this.getSwitchText()));
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public Integer getSpan() {
        return span;
    }

    public void setSpan(Integer span) {
        this.span = span;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getHideHandle() {
        return hideHandle;
    }

    public void setHideHandle(String hideHandle) {
        this.hideHandle = hideHandle;
    }

    public String getRequiredHandle() {
        return requiredHandle;
    }

    public void setRequiredHandle(String requiredHandle) {
        this.requiredHandle = requiredHandle;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(String maxlength) {
        this.maxlength = maxlength;
    }

    public Boolean getMultiple() {
        return multiple;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValueFormat() {
        return valueFormat;
    }

    public void setValueFormat(String valueFormat) {
        this.valueFormat = valueFormat;
    }

    public String getMinlength() {
        return minlength;
    }

    public void setMinlength(String minlength) {
        this.minlength = minlength;
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

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
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

    public String getEnumClassFullName() {
        return enumClassFullName;
    }

    public void setEnumClassFullName(String enumClassFullName) {
        this.enumClassFullName = enumClassFullName;
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

    public boolean isDateRange() {
        return dateRange;
    }

    public void setDateRange(boolean dateRange) {
        this.dateRange = dateRange;
    }

    public String getSwitchText() {
        return switchText;
    }

    public void setSwitchText(String switchText) {
        this.switchText = switchText;
    }
}
