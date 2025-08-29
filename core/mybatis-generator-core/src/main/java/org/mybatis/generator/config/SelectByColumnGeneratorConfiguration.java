package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.IntrospectedColumn;

import java.util.*;


/**
 * @author cen_c
 */
@Setter
@Getter
public class SelectByColumnGeneratorConfiguration extends PropertyHolder {

    private List<String> columnNames = new ArrayList<>();

    private List<IntrospectedColumn> columns = new ArrayList<>();

    private String orderByClause;
    /**
     * 方法返回列表的泛型参数，主键primaryKey或者model默认model
     */
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
    @Getter
    private int returnType = 0;

    public SelectByColumnGeneratorConfiguration() {
        super();
    }

    public SelectByColumnGeneratorConfiguration(String columnName) {
        this.columnNames.add(columnName);
    }

    public boolean addColumnName(String columnName) {
        if (!this.columnNames.contains(columnName)) {
            this.columnNames.add(columnName);
            return true;
        }
        return false;
    }

    public void addColumn(IntrospectedColumn column) {
        if (!columns.contains(column)) {
            this.columns.add(column);
        }
    }

    public boolean isReturnPrimaryKey() {
        return "primaryKey".equalsIgnoreCase(this.getReturnTypeParam())
                ||"list_primaryKey".equals(this.getReturnTypeParam());
    }

    public Boolean getParameterList() {
        if (parameterList==null) {
            return "list".equals(this.parameterType);
        }
        return parameterList;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) { return false; }
        SelectByColumnGeneratorConfiguration that = (SelectByColumnGeneratorConfiguration) o;
        return enableDelete == that.enableDelete && returnType == that.returnType && Objects.equals(columnNames, that.columnNames) && Objects.equals(orderByClause, that.orderByClause) && Objects.equals(returnTypeParam, that.returnTypeParam) && Objects.equals(parameterType, that.parameterType) && Objects.equals(parameterList, that.parameterList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnNames, orderByClause, returnTypeParam, parameterType, enableDelete, parameterList, returnType);
    }
}
