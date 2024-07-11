package org.mybatis.generator.custom;

public class FieldItem {
    public String name;
    public String type;
    public String remarks;

    public FieldItem(String name, String type, boolean optional) {
        this.name = name+("?");
        this.type = type;
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
