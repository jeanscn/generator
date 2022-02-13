package org.mybatis.generator.custom;

import org.mybatis.generator.config.PropertyHolder;

public class RelationPropertyHolder extends PropertyHolder {

    /* 关系类型：association、collection
    * */
    private RelationTypeEnum type;

    private String propertyName;

    private String column;

    private String select;

    private String modelTye;

    private String javaType;



    public RelationPropertyHolder() {
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public RelationTypeEnum getType() {
        return type;
    }

    public void setType(RelationTypeEnum type) {
        this.type = type;
    };

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public String getModelTye() {
        return modelTye;
    }

    public void setModelTye(String modelTye) {
        this.modelTye = modelTye;
    }
}
