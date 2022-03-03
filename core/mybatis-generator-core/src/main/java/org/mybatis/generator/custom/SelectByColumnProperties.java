package org.mybatis.generator.custom;

import org.mybatis.generator.api.IntrospectedColumn;


public class SelectByColumnProperties {
    private String methodName;
    private String columnName;
    private String orderByClause;
    private IntrospectedColumn column;

    public SelectByColumnProperties(String columnName) {
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
}
