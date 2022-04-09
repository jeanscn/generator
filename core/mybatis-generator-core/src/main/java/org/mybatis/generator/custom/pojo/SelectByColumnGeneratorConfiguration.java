package org.mybatis.generator.custom.pojo;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.config.PropertyHolder;


public class SelectByColumnGeneratorConfiguration extends PropertyHolder {
    private String columnName;
    private String orderByClause;
    //方法返回列表的泛型参数，主键primaryKey或者model默认model
    private String returnTypeParam = "model";
    private String methodName;
    private IntrospectedColumn column;
    /**
     * 返回类型，默认0-返回list，1-返回model
     * 主要为了标识selectBaseByPrimaryKey方法，返回model
     * */
    private int returnType = 0;

    public SelectByColumnGeneratorConfiguration() {
        super();
    }

    public SelectByColumnGeneratorConfiguration(String columnName) {
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
}
