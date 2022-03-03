package org.mybatis.generator.custom;

public class SelectByTableProperties {

    private String tableName;

    private String primaryKeyColumn;

    private String otherPrimaryKeyColumn;

    private String methodName;

    private String parameterName;

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
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
}
