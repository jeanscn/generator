package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.core.QueryModesEnum;
import com.vgosoft.core.constant.enums.db.FieldTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataFormat;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import org.mybatis.generator.api.IntrospectedColumn;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-09-20 12:30
 * @version 4.0
 */
public class QueryColumnConfiguration extends TypedPropertyHolder {

    private String listKey;
    private String column;
    private String field;
    private HtmlElementTagTypeEnum tagName = HtmlElementTagTypeEnum.INPUT;
    private String remark;
    private QueryModesEnum queryMode = QueryModesEnum.LIKE;
    private FieldTypeEnum fieldType = FieldTypeEnum.TEXT;
    private int order;

    private HtmlElementDataFormat dataFormat;

    private HtmlElementDataSourceEnum dataSource;

    private String enumClassFullName;

    private String dictCode;

    private String switchText;
    private String dataUrl;
    private String tableName;

    private String tableAlias;

    private IntrospectedColumn introspectedColumn;


    public QueryColumnConfiguration(TableConfiguration tc) {
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

    public FieldTypeEnum getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldTypeEnum fieldType) {
        this.fieldType = fieldType;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
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

    public HtmlElementDataFormat getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(HtmlElementDataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    public HtmlElementDataSourceEnum getDataSource() {
        return dataSource;
    }

    public void setDataSource(HtmlElementDataSourceEnum dataSource) {
        this.dataSource = dataSource;
    }

    public String getEnumClassFullName() {
        return enumClassFullName;
    }

    public void setEnumClassFullName(String enumClassFullName) {
        this.enumClassFullName = enumClassFullName;
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getSwitchText() {
        return switchText;
    }

    public void setSwitchText(String switchText) {
        this.switchText = switchText;
    }

    public IntrospectedColumn getIntrospectedColumn() {
        return introspectedColumn;
    }

    public void setIntrospectedColumn(IntrospectedColumn introspectedColumn) {
        this.introspectedColumn = introspectedColumn;
    }

    public String getListKey() {
        return listKey;
    }

    public void setListKey(String listKey) {
        this.listKey = listKey;
    }
}
