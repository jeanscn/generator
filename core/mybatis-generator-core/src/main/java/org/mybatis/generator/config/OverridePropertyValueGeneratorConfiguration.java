package org.mybatis.generator.config;

public class OverridePropertyValueGeneratorConfiguration extends TypedPropertyHolder {

    private final TableConfiguration tc;

    private final Context context;

    private String sourceColumnName;

    private String targetColumnName;

    private String targetPropertyName;

    private String targetPropertyType;

    private String typeValue;

    private String annotationType;

    private String beanName;


    public OverridePropertyValueGeneratorConfiguration(Context context, TableConfiguration tc) {
        super();
        this.context = context;
        this.tc = tc;
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

    public void setSourceColumnName(String sourceColumnName) {
        this.sourceColumnName = sourceColumnName;
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
        return targetPropertyType;
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
}