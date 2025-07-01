package org.mybatis.generator.config;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.internal.util.JavaBeansUtil;

public class SelectBySqlMethodGeneratorConfiguration extends PropertyHolder {

    private String sqlMethod;

    private String parentIdColumnName;

    private String primaryKeyColumnName;

    private IntrospectedColumn parentIdColumn;

    private IntrospectedColumn primaryKeyColumn;

    public SelectBySqlMethodGeneratorConfiguration() {
        super();
    }

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

    public String getSqlMethod() {
        return sqlMethod;
    }

    public void setSqlMethod(String sqlMethod) {
        this.sqlMethod = sqlMethod;
    }

    public String getParentIdColumnName() {
        return parentIdColumnName;
    }

    public void setParentIdColumnName(String parentIdColumnName) {
        this.parentIdColumnName = parentIdColumnName;
    }

    public String getPrimaryKeyColumnName() {
        return primaryKeyColumnName;
    }

    public void setPrimaryKeyColumnName(String primaryKeyColumnName) {
        this.primaryKeyColumnName = primaryKeyColumnName;
    }

    public String getMethodName() {
        return "selectBySqlMethod"+ JavaBeansUtil.getFirstCharacterUppercase(this.sqlMethod);
    }
}
