package org.mybatis.generator.custom;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.enums.MethodReturnTypeEnum;


public class SelectByColumnProperty {
    private String columnName;
    private String orderByClause;
    private MethodReturnTypeEnum returnTypeParam = MethodReturnTypeEnum.LIST_MODEL;
    private String methodName;
    private IntrospectedColumn column;
    //返回类型，默认0-返回list，1-返回model
    private int returnType = 0;

    public SelectByColumnProperty(String columnName) {
        this.columnName = columnName;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public IntrospectedColumn getColumn() {
        return column;
    }

    public void setColumn(IntrospectedColumn column) {
        this.column = column;
    }

    public int getReturnType() {
        return returnType;
    }

    public void setReturnType(int returnType) {
        this.returnType = returnType;
    }

    public MethodReturnTypeEnum getReturnTypeParam() {
        return returnTypeParam;
    }

    public void setReturnTypeParam(MethodReturnTypeEnum returnTypeParam) {
        this.returnTypeParam = returnTypeParam;
    }

    public boolean isReturnPrimaryKey() {
        return this.getReturnTypeParam().equals(MethodReturnTypeEnum.PRIMARY_KEY)
                ||this.getReturnTypeParam().equals(MethodReturnTypeEnum.LIST_PRIMARY_KEY);
    }
}
