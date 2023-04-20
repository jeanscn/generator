package org.mybatis.generator.config;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.config.PropertyHolder;
import org.mybatis.generator.internal.util.JavaBeansUtil;

public class SelectByTableGeneratorConfiguration extends PropertyHolder {

    private String tableName;

    private String primaryKeyColumn;

    private String otherPrimaryKeyColumn;

    private String methodSuffix;

    private String parameterName;

    private String orderByClause;

    private String additionCondition;

    private String returnTypeParam;

    private boolean enableSplit;

    private boolean enableUnion;

    private IntrospectedColumn thisColumn;

    private IntrospectedColumn otherColumn;

    private String parameterType = "single";

    public SelectByTableGeneratorConfiguration() {
        super();
        returnTypeParam = "model";
        enableSplit = true;
        enableUnion = true;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(String primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    public String getOtherPrimaryKeyColumn() {
        return otherPrimaryKeyColumn;
    }

    public void setOtherPrimaryKeyColumn(String otherPrimaryKeyColumn) {
        this.otherPrimaryKeyColumn = otherPrimaryKeyColumn;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getAdditionCondition() {
        return additionCondition;
    }

    public void setAdditionCondition(String additionCondition) {
        this.additionCondition = additionCondition;
    }

    public String getReturnTypeParam() {
        return returnTypeParam;
    }

    public void setReturnTypeParam(String returnTypeParam) {
        this.returnTypeParam = returnTypeParam;
    }

    public boolean isReturnPrimaryKey() {
        return this.getReturnTypeParam().equals("primaryKey")
                ||this.getReturnTypeParam().equals("list_primaryKey");
    }

    public boolean isEnableSplit() {
        return enableSplit;
    }

    public void setEnableSplit(boolean enableSplit) {
        this.enableSplit = enableSplit;
    }

    public boolean isEnableUnion() {
        return enableUnion;
    }

    public void setEnableUnion(boolean enableUnion) {
        this.enableUnion = enableUnion;
    }

    public String getMethodSuffix() {
        return methodSuffix;
    }

    public void setMethodSuffix(String methodSuffix) {
        this.methodSuffix = methodSuffix;
    }

    public String getMethodName() {
        return "selectByTable"+JavaBeansUtil.getFirstCharacterUppercase(this.getMethodSuffix());
    }

    public String getSplitMethodName() {
        return "deleteByTable"+JavaBeansUtil.getFirstCharacterUppercase(this.getMethodSuffix());
    }

    public String getUnionMethodName() {
        return "insertByTable"+JavaBeansUtil.getFirstCharacterUppercase(this.getMethodSuffix());
    }

    public IntrospectedColumn getThisColumn() {
        return thisColumn;
    }

    public void setThisColumn(IntrospectedColumn thisColumn) {
        this.thisColumn = thisColumn;
    }

    public IntrospectedColumn getOtherColumn() {
        return otherColumn;
    }

    public void setOtherColumn(IntrospectedColumn otherColumn) {
        this.otherColumn = otherColumn;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }
}
