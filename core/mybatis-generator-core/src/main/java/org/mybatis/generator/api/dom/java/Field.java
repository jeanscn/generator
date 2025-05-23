package org.mybatis.generator.api.dom.java;

import java.util.Objects;
import java.util.Optional;

public class Field extends JavaElement {
    private FullyQualifiedJavaType type;
    private String name;
    private String initializationString;
    private boolean isTransient;
    private boolean isVolatile;
    private boolean isFinal;
    private String remark;
    private String sourceColumnName;

    public Field(String name, FullyQualifiedJavaType type) {
        this.name = name;
        this.type = type;
    }

    public Field(Field field) {
        super(field);
        this.type = field.type;
        this.name = field.name;
        this.initializationString = field.initializationString;
        this.isTransient = field.isTransient;
        this.isVolatile = field.isVolatile;
        this.isFinal = field.isFinal;
        this.remark = field.remark;
        this.sourceColumnName = field.sourceColumnName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FullyQualifiedJavaType getType() {
        return type;
    }

    public void setType(FullyQualifiedJavaType type) {
        this.type = type;
    }

    public Optional<String> getInitializationString() {
        return Optional.ofNullable(initializationString);
    }

    public void setInitializationString(String initializationString) {
        this.initializationString = initializationString;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public void setVolatile(boolean isVolatile) {
        this.isVolatile = isVolatile;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSourceColumnName() {
        return sourceColumnName;
    }

    public void setSourceColumnName(String sourceColumnName) {
        this.sourceColumnName = sourceColumnName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Field)) return false;
        Field field = (Field) o;
        return Objects.equals(getName(), field.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
