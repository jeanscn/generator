package org.mybatis.generator.custom.pojo;

import org.mybatis.generator.api.IntrospectedColumn;

public class CustomMethodGeneratorConfiguration {

    private String methodName;

    private String sqlMethod;

    private IntrospectedColumn parentIdColumn;

    private IntrospectedColumn primaryKeyColumn;

    public IntrospectedColumn getParentIdColumn() {
        return parentIdColumn;
    }

    public void setParentIdColumn(IntrospectedColumn parentIdColumn) {
        this.parentIdColumn = parentIdColumn;
    }

    public IntrospectedColumn getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(IntrospectedColumn primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getSqlMethod() {
        return sqlMethod;
    }

    public void setSqlMethod(String sqlMethod) {
        this.sqlMethod = sqlMethod;
    }
}
