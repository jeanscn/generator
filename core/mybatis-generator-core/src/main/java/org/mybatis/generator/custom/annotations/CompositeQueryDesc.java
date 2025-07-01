package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.constant.enums.core.QueryModesEnum;
import com.vgosoft.core.constant.enums.db.FieldTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.AbstractFilterConditionConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-07 06:58
 * @version 3.0
 */
@Getter
@Setter
public class CompositeQueryDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@CompositeQuery";

    private final IntrospectedColumn introspectedColumn;
    private String tableName;
    private String tableAlias;
    private String column;
    private String listKey;
    private HtmlElementTagTypeEnum tagName = HtmlElementTagTypeEnum.UNKNOWN;
    private String field;
    private String remark;
    private QueryModesEnum queryMode;
    private int order;
    private FieldTypeEnum fieldType;

    private String dataUrl;
    private boolean multiple = false;
    private boolean range = false;
    private boolean repeat = false;
    private String operators;


    public static CompositeQueryDesc create(IntrospectedColumn introspectedColumn) {
        return new CompositeQueryDesc(introspectedColumn);
    }

    public static CompositeQueryDesc create(AbstractFilterConditionConfiguration abstractFilterConditionConfiguration, IntrospectedTable introspectedTable) {
        CompositeQueryDesc compositeQueryDesc = new CompositeQueryDesc(abstractFilterConditionConfiguration, introspectedTable);
        if (abstractFilterConditionConfiguration.getDataFormat() != null) {
            String enumClassNameByDataFmt = Mb3GenUtil.getEnumClassNameByDataFmt(abstractFilterConditionConfiguration.getDataFormat().codeName());
            if (enumClassNameByDataFmt != null) {
                abstractFilterConditionConfiguration.setEnumClassFullName(enumClassNameByDataFmt);
                abstractFilterConditionConfiguration.setDataSource(HtmlElementDataSourceEnum.DICT_ENUM);
            }
        }
        if (abstractFilterConditionConfiguration.getTagName()!=null) {
            if (abstractFilterConditionConfiguration.getTagName().equals(HtmlElementTagTypeEnum.INPUT)) {
                compositeQueryDesc.setQueryMode(QueryModesEnum.LIKE);
            }
            compositeQueryDesc.setTagName(abstractFilterConditionConfiguration.getTagName());
        }
        if (abstractFilterConditionConfiguration.getDataSource() != null) {
            switch (abstractFilterConditionConfiguration.getDataSource()) {
                case DICT_DATA:
                    if (abstractFilterConditionConfiguration.getDictCode() != null) {
                        compositeQueryDesc.setDataUrl("/system/sys-dict-data-impl/option/" + abstractFilterConditionConfiguration.getDictCode());
                    } else {
                        compositeQueryDesc.setDataUrl("/system/sys-dict-data-impl/option/" + abstractFilterConditionConfiguration.getField());
                    }
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case DICT_SYS:
                    if (abstractFilterConditionConfiguration.getDictCode() != null) {
                        compositeQueryDesc.setDataUrl("/system/sys-cfg-dict-impl/option/" + abstractFilterConditionConfiguration.getDictCode());
                    } else {
                        compositeQueryDesc.setDataUrl("/system/sys-cfg-dict-impl/option/" + abstractFilterConditionConfiguration.getField());
                    }
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case DICT_USER:
                    if (abstractFilterConditionConfiguration.getDictCode() != null) {
                        compositeQueryDesc.setDataUrl("/system/dict-content-impl/option/" + abstractFilterConditionConfiguration.getDictCode());
                    } else {
                        compositeQueryDesc.setDataUrl("/system/dict-content-impl/option/" + abstractFilterConditionConfiguration.getField());
                    }
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case DICT_MODULE:
                    compositeQueryDesc.setDataUrl("/system/sys-cfg-module-impl/option/name");
                    compositeQueryDesc.setQueryMode(QueryModesEnum.EQUAL);
                    break;
                case DICT_ENUM:
                    if (abstractFilterConditionConfiguration.getEnumClassFullName() != null) {
                        compositeQueryDesc.setDataUrl("/system/enum/options/" + abstractFilterConditionConfiguration.getEnumClassFullName());
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

    public CompositeQueryDesc(AbstractFilterConditionConfiguration queryColumnConfiguration, IntrospectedTable introspectedTable) {
        super();
        this.introspectedColumn = introspectedTable.getColumn(queryColumnConfiguration.getColumn()).orElse(null);
        this.tableName = introspectedTable.getTableConfiguration().getTableName();
        this.tableAlias = introspectedTable.getTableConfiguration().getAlias();
        this.column = queryColumnConfiguration.getColumn();
        this.tagName = queryColumnConfiguration.getTagName();
        this.field = queryColumnConfiguration.getField();
        this.remark = queryColumnConfiguration.getRemark();
        this.queryMode = queryColumnConfiguration.getQueryMode();
        this.order = queryColumnConfiguration.getOrder();
        this.fieldType = queryColumnConfiguration.getFieldType();
        this.dataUrl = queryColumnConfiguration.getDataUrl();
        this.listKey = queryColumnConfiguration.getListKey();
        this.multiple = queryColumnConfiguration.isMultiple();
        this.range = queryColumnConfiguration.isRange();
        this.addImports("com.vgosoft.core.annotation.CompositeQuery");
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
        if (this.tagName!=null && !this.tagName.equals(HtmlElementTagTypeEnum.UNKNOWN)) {
            this.items.add(VStringUtil.format("tagName = HtmlElementTagTypeEnum.{0}", this.tagName.name()));
            this.addImports("com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum");
        }
        if (this.getQueryMode()!=null && !this.getQueryMode().equals(QueryModesEnum.LIKE)) {
            this.items.add(VStringUtil.format("queryMode = QueryModesEnum.{0}", this.getQueryMode().name()));
            this.addImports("com.vgosoft.core.constant.enums.core.QueryModesEnum");
        }
        if (this.getOrder() != 10) {
            this.items.add(VStringUtil.format("order = {0}", this.getOrder()));
        }
        if (this.getFieldType() != null && !this.getFieldType().equals(FieldTypeEnum.TEXT)) {
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
        if (this.isRepeat()) {
            this.items.add("repeat = true");
        }
        if (VStringUtil.isNotBlank(this.getOperators())) {
            this.items.add(VStringUtil.format("operators = \"{0}\"", this.getOperators()));
        }
        if (this.isMultiple()) {
            this.items.add("multiple = true");
        }
        if (this.isRange()) {
            this.items.add("range = true");
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }
}
