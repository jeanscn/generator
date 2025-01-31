package org.mybatis.generator.custom;

import org.mybatis.generator.api.dom.java.Field;

import java.util.Objects;

public class FieldItem {
    public String name;
    public String type;
    public String remarks;
    public boolean required;

    public FieldItem(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public FieldItem(Field field) {
        this.name = field.getName();
        this.type = field.getType().getShortName();
        this.remarks = field.getRemark();
        this.required = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldItem fieldItem = (FieldItem) o;
        return Objects.equals(name, fieldItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
