package org.mybatis.generator.custom.pojo;

import org.mybatis.generator.config.PropertyHolder;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.internal.util.StringUtility;

public class RelationPropertyHolder extends PropertyHolder {

    /* 关系类型：association、collection
    * */
    private RelationTypeEnum type;

    private String propertyName;

    private String column;

    private String select;

    private String modelTye;

    private String javaType;

    private String columnRemark;

    public String getColumnRemark() {
        return columnRemark;
    }

    public void setColumnRemark(String columnRemark) {
        this.columnRemark = columnRemark;
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

    public boolean isSubSelected(){
        return StringUtility.stringHasValue(select) && StringUtility.stringHasValue(column);
    }

    public String getModelTye() {
        return modelTye;
    }

    public void setModelTye(String modelTye) {
        this.modelTye = modelTye;
    }
}