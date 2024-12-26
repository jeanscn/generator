package org.mybatis.generator.config;

import com.vgosoft.core.constant.GlobalConstant;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class OverridePropertyValueGeneratorConfiguration extends TypedPropertyHolder {

    private final TableConfiguration tc;

    private final Context context;

    private final String sourceColumnName;

    private String targetColumnName;

    private String targetPropertyName;

    private String targetPropertyType;

    private String typeValue;

    private String annotationType;

    private String beanName;

    private String applyProperty = GlobalConstant.CACHE_PO_DEFAULT_VALUE_TEXT;

    private String remark;

    private String initializationString;

    private final Set<String> importTypes = new HashSet<>();

    private String enumClassName;

    private HtmlElementDescriptor elementDescriptor;


    public OverridePropertyValueGeneratorConfiguration(Context context, TableConfiguration tc,String sourceColumnNames) {
        super();
        this.context = context;
        this.tc = tc;
        this.sourceColumnName = sourceColumnNames;
    }

    public TableConfiguration getTc() {
        return tc;
    }

    public Context getContext() {
        return context;
    }

    public String getSourceColumnName() {
        return sourceColumnName;
    }

    public String getTargetColumnName() {
        return targetColumnName;
    }

    public void setTargetColumnName(String targetColumnName) {
        this.targetColumnName = targetColumnName;
    }

    public String getTargetPropertyName() {
        return targetPropertyName;
    }

    public void setTargetPropertyName(String targetPropertyName) {
        this.targetPropertyName = targetPropertyName;
    }

    public String getTargetPropertyType() {
        return targetPropertyType==null? FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName():targetPropertyType;
    }

    public void setTargetPropertyType(String targetPropertyType) {
        this.targetPropertyType = targetPropertyType;
    }

    public String getTypeValue() {
        return typeValue;
    }

    public void setTypeValue(String typeValue) {
        this.typeValue = typeValue;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getEnumClassName() {
        return enumClassName;
    }

    public void setEnumClassName(String enumClassName) {
        this.enumClassName = enumClassName;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OverridePropertyValueGeneratorConfiguration)) return false;
        OverridePropertyValueGeneratorConfiguration that = (OverridePropertyValueGeneratorConfiguration) o;
        return Objects.equals(getSourceColumnName(), that.getSourceColumnName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSourceColumnName());
    }

    public Optional<String> getInitializationString() {
        return Optional.ofNullable(initializationString);
    }

    public void setInitializationString(String initializationString) {
        this.initializationString = initializationString;
    }

    public Set<String> getImportTypes() {
        return importTypes;
    }

    public void addImportTypes(String importType) {
        this.importTypes.add(importType);
    }

    public HtmlElementDescriptor getElementDescriptor() {
        return elementDescriptor;
    }

    public void setElementDescriptor(HtmlElementDescriptor elementDescriptor) {
        this.elementDescriptor = elementDescriptor;
    }
}
