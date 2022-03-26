package org.mybatis.generator.custom.pojo;

public class SelectByTableGeneratorConfiguration {

    private String tableName;

    private String primaryKeyColumn;

    private String otherPrimaryKeyColumn;

    private String methodName;

    private String parameterName;

    private String orderByClause;

    private String additionCondition;

    private String returnTypeParam = "model";

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
}
