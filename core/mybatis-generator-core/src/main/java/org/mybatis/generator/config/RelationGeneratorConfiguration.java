package org.mybatis.generator.config;

import org.mybatis.generator.config.PropertyHolder;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.internal.util.StringUtility;

public class RelationGeneratorConfiguration extends PropertyHolder {

    /**
     *  关系类型：association、collection
    * */
    private RelationTypeEnum type;

    /**
     * 新增的属性名称
     */
    private String propertyName;

    /**
     * 用于查询的列名
     */
    private String column;

    /**
     * 执行子查询调用的mapper方法
     */
    private String select;

    /**
     * 属性类型的泛型（PO类中使用）
     */
    private String modelTye;

    /**
     * VO属性类型的泛型（VO类中使用）
     */
    private String voModelTye;

    /**
     * mapper xml中collection元算的javaType。
     * 默认值为ArrayList
     */
    private String javaType;

    /**
     *
     */
    private String columnRemark;

    /**
     * 是否支持属性级联insert操作
     */
    private boolean enableInsert;

    /**
     * 是否支持属性级联Update操作
     */
    private boolean enableUpdate;

    private boolean enableInsertOrUpdate;

    /**
     * 是否支持属性级联Delete操作
     */
    private boolean enableDelete;

    /**
     * 用于支持新增和更新时，新增更新方法的bean类全名。
     * 仅在支持新增、更新、删除时使用
     */
    private String beanClassFullName;

    /**
     * 用于支持新增和更新时，检查关联属性值。构造getter、setter
     * 仅在支持新增、更新、删除时使用
     */
    private String relationProperty;

    /**
     * 用于支持新增和更新时，检查关联属性值。构造getter
     * 仅在支持新增、更新、删除时使用
     */
    private boolean relationPropertyIsBoolean;

    private String remark;

    public RelationGeneratorConfiguration() {
        super();
        this.enableInsert = false;
        this.enableUpdate = false;
        this.enableDelete = false;
        this.enableInsertOrUpdate = false;
        this.relationPropertyIsBoolean = false;
    }

    public String getColumnRemark() {
        return columnRemark;
    }

    public void setColumnRemark(String columnRemark) {
        this.columnRemark = columnRemark;
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

    public String getVoModelTye() {
        return voModelTye==null?this.modelTye:voModelTye;
    }

    public void setVoModelTye(String voModelTye) {
        this.voModelTye = voModelTye;
    }

    public boolean isEnableInsert() {
        return enableInsert;
    }

    public void setEnableInsert(boolean enableInsert) {
        this.enableInsert = enableInsert;
    }

    public boolean isEnableUpdate() {
        return enableUpdate;
    }

    public void setEnableUpdate(boolean enableUpdate) {
        this.enableUpdate = enableUpdate;
    }

    public boolean isEnableDelete() {
        return enableDelete;
    }

    public void setEnableDelete(boolean enableDelete) {
        this.enableDelete = enableDelete;
    }

    public String getBeanClassFullName() {
        return beanClassFullName;
    }

    public void setBeanClassFullName(String beanClassFullName) {
        this.beanClassFullName = beanClassFullName;
    }

    public String getRelationProperty() {
        return relationProperty;
    }

    public void setRelationProperty(String relationProperty) {
        this.relationProperty = relationProperty;
    }

    public boolean isRelationPropertyIsBoolean() {
        return relationPropertyIsBoolean;
    }

    public void setRelationPropertyIsBoolean(boolean relationPropertyIsBoolean) {
        this.relationPropertyIsBoolean = relationPropertyIsBoolean;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public boolean isEnableInsertOrUpdate() {
        return enableInsertOrUpdate;
    }

    public void setEnableInsertOrUpdate(boolean enableInsertOrUpdate) {
        this.enableInsertOrUpdate = enableInsertOrUpdate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
