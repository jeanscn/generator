package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.constant.enums.core.QueryModesEnum;
import com.vgosoft.core.constant.enums.db.FieldTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.QueryColumnConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-07 06:58
 * @version 3.0
 */
public class CompositeQueryDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@CompositeQuery";

    private String listKey;

    private String column;
    private String remark;
    private HtmlElementTagTypeEnum tagName = HtmlElementTagTypeEnum.INPUT;
    private QueryModesEnum queryMode = QueryModesEnum.LIKE;
    private int order;
    private FieldTypeEnum fieldType = FieldTypeEnum.TEXT;
    private String dataUrl;
    private String field;
    private String tableName;
    private String tableAlias;

    private final IntrospectedColumn introspectedColumn;

    public static CompositeQueryDesc create(IntrospectedColumn introspectedColumn) {
        return new CompositeQueryDesc(introspectedColumn);
    }

    public static CompositeQueryDesc create(QueryColumnConfiguration queryColumnConfiguration, IntrospectedTable introspectedTable) {
        CompositeQueryDesc compositeQueryDesc = new CompositeQueryDesc(queryColumnConfiguration, introspectedTable);
        if (queryColumnConfiguration.getDataFormat() != null) {
            String enumClassNameByDataFmt = Mb3GenUtil.getEnumClassNameByDataFmt(queryColumnConfiguration.getDataFormat().codeName());
            if (enumClassNameByDataFmt != null) {
                queryColumnConfiguration.setEnumClassFullName(enumClassNameByDataFmt);
                queryColumnConfiguration.setDataSource(HtmlElementDataSourceEnum.DICT_ENUM);
            }
        }
        if (queryColumnConfiguration.getDataSource() != null) {
            switch (queryColumnConfiguration.getDataSource()) {
                case DICT_DATA:
                    if (queryColumnConfiguration.getDictCode() != null) {
                        compositeQueryDesc.setDataUrl("/system/sys-dict-data-impl/option/" + queryColumnConfiguration.getDictCode());
                    } else {
                        compositeQueryDesc.setDataUrl("/system/sys-dict-data-impl/option/" + queryColumnConfiguration.getField());
                    }
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case DICT_SYS:
                    if (queryColumnConfiguration.getDictCode() != null) {
                        compositeQueryDesc.setDataUrl("/system/sys-cfg-dict-impl/option/" + queryColumnConfiguration.getDictCode());
                    } else {
                        compositeQueryDesc.setDataUrl("/system/sys-cfg-dict-impl/option/" + queryColumnConfiguration.getField());
                    }
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case DICT_USER:
                    if (queryColumnConfiguration.getDictCode() != null) {
                        compositeQueryDesc.setDataUrl("/system/dict-content-impl/option/" + queryColumnConfiguration.getDictCode());
                    } else {
                        compositeQueryDesc.setDataUrl("/system/dict-content-impl/option/" + queryColumnConfiguration.getField());
                    }
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case DICT_MODULE:
                    compositeQueryDesc.setDataUrl("/system/sys-cfg-module-impl/option/name");
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case DICT_ENUM:
                    if (queryColumnConfiguration.getEnumClassFullName() != null) {
                        compositeQueryDesc.setDataUrl("/system/enum/options/" + queryColumnConfiguration.getEnumClassFullName());
                    }
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case DICT:
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case DEPARTMENT:
                    compositeQueryDesc.setFieldType(FieldTypeEnum.DEPARTMENT);
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case USER:
                    compositeQueryDesc.setFieldType(FieldTypeEnum.USER);
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case ROLE:
                    compositeQueryDesc.setFieldType(FieldTypeEnum.ROLE);
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case ORGANIZATION:
                    compositeQueryDesc.setFieldType(FieldTypeEnum.ORGAN);
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
            }
        }
        return compositeQueryDesc;
    }

    public CompositeQueryDesc(IntrospectedColumn introspectedColumn) {
        super();
        this.column = introspectedColumn.getActualColumnName();
        this.addImports("com.vgosoft.core.annotation.CompositeQuery");
        this.introspectedColumn = introspectedColumn;
        if (introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJavaLocalDateTimeColumn()) {
            this.tagName = HtmlElementTagTypeEnum.DATE;
            this.fieldType = FieldTypeEnum.DATETIME;
            this.queryMode = QueryModesEnum.EQUAL;
        } else if (introspectedColumn.isJDBCTimeColumn() || introspectedColumn.isJavaLocalTimeColumn()) {
            this.tagName = HtmlElementTagTypeEnum.DATE;
            this.fieldType = FieldTypeEnum.TIME;
            this.queryMode = QueryModesEnum.EQUAL;
        } else if (introspectedColumn.isJavaLocalDateColumn()) {
            this.tagName = HtmlElementTagTypeEnum.DATE;
            this.fieldType = FieldTypeEnum.DATE;
            this.queryMode = QueryModesEnum.EQUAL;
        } else if (introspectedColumn.isStringColumn()) {
            this.fieldType = FieldTypeEnum.TEXT;
            this.queryMode = QueryModesEnum.LIKE;
        } else if (introspectedColumn.isJdbcCharacterColumn()) {
            this.fieldType = FieldTypeEnum.TEXT;
            this.queryMode = QueryModesEnum.LIKE;
        } else if (introspectedColumn.isNumericColumn()) {
            this.fieldType = FieldTypeEnum.NUMBER;
            this.queryMode = QueryModesEnum.EQUAL;
        } else {
            this.fieldType = FieldTypeEnum.TEXT;
            this.queryMode = QueryModesEnum.EQUAL;
        }
    }

    public CompositeQueryDesc(QueryColumnConfiguration queryColumnConfiguration, IntrospectedTable introspectedTable) {
        super();
        this.listKey = queryColumnConfiguration.getListKey();
        this.column = queryColumnConfiguration.getColumn();
        this.remark = queryColumnConfiguration.getRemark();
        this.tagName = queryColumnConfiguration.getTagName();
        this.queryMode = queryColumnConfiguration.getQueryMode();
        this.order = queryColumnConfiguration.getOrder();
        this.fieldType = queryColumnConfiguration.getFieldType();
        this.dataUrl = queryColumnConfiguration.getDataUrl();
        this.field = queryColumnConfiguration.getField();
        this.addImports("com.vgosoft.core.annotation.CompositeQuery");
        this.introspectedColumn = introspectedTable.getColumn(queryColumnConfiguration.getColumn()).orElse(null);
    }

    @Override
    public String toAnnotation() {
        this.items.add(VStringUtil.format("value = \"{0}\"", this.column));
        if (stringHasValue(this.listKey)) {
            this.items.add(VStringUtil.format("listKey = \"{0}\"", this.listKey));
        }
        if (stringHasValue(this.remark)) {
            this.items.add(VStringUtil.format("remark = \"{0}\"", this.remark));
        } else {
            this.items.add(VStringUtil.format("remark = \"{0}\"", this.introspectedColumn.getRemarks(true)));
        }
        if (!this.tagName.equals(HtmlElementTagTypeEnum.INPUT)) {
            this.items.add(VStringUtil.format("tagName = HtmlElementTagTypeEnum.{0}", this.tagName.name()));
            this.addImports("com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum");
        }
        if (!this.getQueryMode().equals(QueryModesEnum.LIKE)) {
            this.items.add(VStringUtil.format("queryMode = QueryModesEnum.{0}", this.getQueryMode().name()));
            this.addImports("com.vgosoft.core.constant.enums.core.QueryModesEnum");
        }
        if (this.getOrder() != 10) {
            this.items.add(VStringUtil.format("order = {0}", this.getOrder()));
        }
        if (!this.getFieldType().equals(FieldTypeEnum.TEXT)) {
            this.items.add(VStringUtil.format("fieldType = FieldTypeEnum.{0}", this.getFieldType().name()));
            this.addImports("com.vgosoft.core.constant.enums.db.FieldTypeEnum");
        }
        if (VStringUtil.isNotBlank(this.getDataUrl())) {
            this.items.add(VStringUtil.format("dataUrl = \"{0}\"", this.getDataUrl()));
        }
        if (VStringUtil.isNotBlank(this.getField())) {
            this.items.add(VStringUtil.format("field = \"{0}\"", this.getField()));
        } else {
            this.items.add(VStringUtil.format("field = \"{0}\"", this.introspectedColumn.getJavaProperty()));
        }
        if (VStringUtil.isNotBlank(this.getTableName())) {
            this.items.add(VStringUtil.format("tableName = \"{0}\"", this.getTableName()));
        }
        if (VStringUtil.isNotBlank(this.getTableAlias())) {
            this.items.add(VStringUtil.format("tableAlias = \"{0}\"", this.getTableAlias()));
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
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

    public FieldTypeEnum getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldTypeEnum fieldType) {
        this.fieldType = fieldType;
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

    public IntrospectedColumn getIntrospectedColumn() {
        return introspectedColumn;
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

    public String getListKey() {
        return listKey;
    }

    public void setListKey(String listKey) {
        this.listKey = listKey;
    }
}
