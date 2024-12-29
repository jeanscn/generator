package org.mybatis.generator.custom;

import org.mybatis.generator.api.dom.java.Field;

public class FieldItem {
    public String name;
    public String type;
    public String remarks;

    public FieldItem(String name, String type) {
        this.name = name+("?");
        this.type = type;
    }

    public FieldItem(Field field) {
        this.name = field.getName()+("?");
        this.type = field.getType().getShortName();
        this.remarks = field.getRemark();
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
}
