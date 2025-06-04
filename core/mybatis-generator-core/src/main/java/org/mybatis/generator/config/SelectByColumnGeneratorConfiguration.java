package org.mybatis.generator.config;

import org.mybatis.generator.api.IntrospectedColumn;

import java.util.*;


public class SelectByColumnGeneratorConfiguration extends PropertyHolder {
    private List<String> columnNames = new ArrayList<>();
    private List<IntrospectedColumn> columns = new ArrayList<>();
    private String orderByClause;
    //方法返回列表的泛型参数，主键primaryKey或者model默认model
    private String returnTypeParam = "model";
    private String methodName;
    private String deleteMethodName;
    private String parameterType = "single";
    private boolean enableDelete;

    private Boolean parameterList;

    private Boolean genControllerMethod = false;
    /**
     * 返回类型，默认0-返回list，1-返回model
     * 主要为了标识selectBaseByPrimaryKey方法，返回model
     * */
    private int returnType = 0;

    public SelectByColumnGeneratorConfiguration() {
        super();
    }

    public SelectByColumnGeneratorConfiguration(String columnName) {
        this.columnNames.add(columnName);
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

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public boolean addColumnName(String columnName) {
        if (!this.columnNames.contains(columnName)) {
            this.columnNames.add(columnName);
            return true;
        }
        return false;
    }

    public List<IntrospectedColumn> getColumns() {
        return columns;
    }

    public void addColumn(IntrospectedColumn column) {
        if (!columns.contains(column)) {
            this.columns.add(column);
        }
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

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public void setColumns(List<IntrospectedColumn> columns) {
        this.columns = columns;
    }

    public Boolean getParameterList() {
        if (parameterList==null) {
            return "list".equals(this.parameterType);
        }
        return parameterList;
    }

    public void setParameterList(Boolean parameterList) {
        this.parameterList = parameterList;
    }

    public boolean isEnableDelete() {
        return enableDelete;
    }

    public void setEnableDelete(boolean enableDelete) {
        this.enableDelete = enableDelete;
    }

    public String getDeleteMethodName() {
        return deleteMethodName;
    }

    public void setDeleteMethodName(String deleteMethodName) {
        this.deleteMethodName = deleteMethodName;
    }

    public Boolean getGenControllerMethod() {
        return genControllerMethod;
    }

    public void setGenControllerMethod(Boolean genControllerMethod) {
        this.genControllerMethod = genControllerMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectByColumnGeneratorConfiguration that = (SelectByColumnGeneratorConfiguration) o;
        return methodName.equals(that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName);
    }
}
