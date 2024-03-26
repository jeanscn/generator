package org.mybatis.generator.custom.annotations;

import cn.hutool.core.annotation.Alias;
import com.vgosoft.core.annotation.VueFormItemMeta;
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
    private String size;
    private String component;
    private String rules;
    private String hideHandle;
    private String requiredHandle;
    private String placeholder;
    private String maxlength;
    private Boolean multiple;


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
        items.add(VStringUtil.format("span = {0}", this.getSpan()));
        if (VStringUtil.isNotBlank(this.getFieldValue())) {
            items.add(VStringUtil.format("fieldValue = \"{0}\"", this.getFieldValue()));
        }
        if (VStringUtil.isNotBlank(this.getTips())) {
            items.add(VStringUtil.format("tips = \"{0}\"", this.getTips()));
        }
        if (VStringUtil.isNotBlank(this.getSize())) {
            items.add(VStringUtil.format("size = \"{0}\"", this.getSize()));
        }
        if (VStringUtil.isNotBlank(this.getComponent())) {
            items.add(VStringUtil.format("component = \"{0}\"", this.getComponent()));
        }
        if (VStringUtil.isNotBlank(this.getRules())) {
            items.add(VStringUtil.format("rules = \"{0}\"", this.getRules()));
        }
        if (VStringUtil.isNotBlank(this.getHideHandle())) {
            items.add(VStringUtil.format("hideHandle = \"{0}\"", this.getHideHandle()));
        }
        if (VStringUtil.isNotBlank(this.getRequiredHandle())) {
            items.add(VStringUtil.format("requiredHandle = \"{0}\"", this.getRequiredHandle()));
        }
        return ANNOTATION_NAME + "(" + String.join("\n       ,", items.toArray(new String[0])) + ")";
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
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
}
