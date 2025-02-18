package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.core.QueryModesEnum;
import com.vgosoft.core.constant.enums.db.FieldTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataFormat;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import org.mybatis.generator.api.IntrospectedColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-09-20 12:30
 * @version 4.0
 */
public class FilterColumnConfiguration extends TypedPropertyHolder {
    private String column;
    private HtmlElementTagTypeEnum tagName;
    private String remark;
    private QueryModesEnum queryMode = QueryModesEnum.LIKE;
    private int order;
    private String enumClassFullName;
    private boolean repeat = false;
    private List<String> operators = new ArrayList<>();

    private boolean multiple;

   private boolean range;
    private String tableName;

    private String tableAlias;

    private IntrospectedColumn introspectedColumn;


    public FilterColumnConfiguration(TableConfiguration tc) {
        super();
        this.tableName = tc.getTableName();
        this.tableAlias = tc.getAlias();
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public HtmlElementTagTypeEnum getTagName() {
        return tagName;
    }

    public void setTagName(HtmlElementTagTypeEnum tagName) {
        this.tagName = tagName;
    }

    public QueryModesEnum getQueryMode() {
        return queryMode;
    }

    public void setQueryMode(QueryModesEnum queryMode) {
        this.queryMode = queryMode;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    public String getEnumClassFullName() {
        return enumClassFullName;
    }

    public void setEnumClassFullName(String enumClassFullName) {
        this.enumClassFullName = enumClassFullName;
    }

    public IntrospectedColumn getIntrospectedColumn() {
        return introspectedColumn;
    }

    public void setIntrospectedColumn(IntrospectedColumn introspectedColumn) {
        this.introspectedColumn = introspectedColumn;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public List<String> getOperators() {
        return operators;
    }

    public void setOperators(List<String> operators) {
        this.operators = operators;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isRange() {
        return range;
    }

    public void setRange(boolean range) {
        this.range = range;
    }
}
