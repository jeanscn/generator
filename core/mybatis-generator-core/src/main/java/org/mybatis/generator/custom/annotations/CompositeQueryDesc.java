package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.constant.enums.core.QueryModesEnum;
import com.vgosoft.core.constant.enums.db.FieldTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-07 06:58
 * @version 3.0
 */
public class CompositeQueryDesc extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@CompositeQuery";

    private String value;
    private String description;
    private HtmlElementTagTypeEnum tagName = HtmlElementTagTypeEnum.INPUT;
    private QueryModesEnum queryMode = QueryModesEnum.EQUAL;
    private int order;
    private FieldTypeEnum fieldType = FieldTypeEnum.TEXT;
    private String dataUrl;
    private String fieldName;
    private String tableName;

    private final IntrospectedColumn introspectedColumn;

    public static CompositeQueryDesc create(IntrospectedColumn introspectedColumn){
        return new CompositeQueryDesc(introspectedColumn);
    }

    public CompositeQueryDesc(IntrospectedColumn introspectedColumn) {
        super();
        this.value = introspectedColumn.getActualColumnName();
        this.addImports("com.vgosoft.core.annotation.CompositeQuery");
        this.introspectedColumn = introspectedColumn;
    }

    @Override
    public String toAnnotation() {
        this.items.add(VStringUtil.format("value=\"{0}\"", this.value));
        if (stringHasValue(this.description)) {
            this.items.add(VStringUtil.format("description=\"{0}\"", this.description));
        }else{
            this.items.add(VStringUtil.format("description=\"{0}\"", this.introspectedColumn.getRemarks(true)));
        }
        if (!this.tagName.equals(HtmlElementTagTypeEnum.INPUT)) {
            this.items.add(VStringUtil.format("tagName=TagNamesEnum.{0}", this.tagName.name()));
        }
        if (!this.getQueryMode().equals(QueryModesEnum.EQUAL)) {
            this.items.add(VStringUtil.format("queryMode=QueryModesEnum.{0}", this.getQueryMode().name()));
        }
        if (this.getOrder()!=10) {
            this.items.add(VStringUtil.format("order={0}", this.getOrder()));
        }
        if (!this.getFieldType().equals(FieldTypeEnum.TEXT)) {
            this.items.add(VStringUtil.format("fieldType=FieldTypeEnum.{0}", this.getFieldType().name()));
        }
        if (VStringUtil.isNotBlank(this.getDataUrl())) {
            this.items.add(VStringUtil.format("dataUrl=\"{0}\"", this.getDataUrl()));
        }
        if (VStringUtil.isNotBlank(this.getFieldName())) {
            this.items.add(VStringUtil.format("fieldName=\"{0}\"", this.getFieldName()));
        }else{
            this.items.add(VStringUtil.format("fieldName=\"{0}\"", this.introspectedColumn.getJavaProperty()));
        }
        if (VStringUtil.isNotBlank(this.getTableName())) {
            this.items.add(VStringUtil.format("tableName=\"{0}\"", this.getTableName()));
        }else{
            if (this.introspectedColumn.getIntrospectedTable() != null) {
                if (stringHasValue(this.introspectedColumn.getIntrospectedTable().getTableConfiguration().getAlias())) {
                    this.items.add(VStringUtil.format("tableName=\"{0}\"", this.introspectedColumn.getIntrospectedTable().getTableConfiguration().getAlias()));
                }else{
                    this.items.add(VStringUtil.format("tableName=\"{0}\"", this.introspectedColumn.getIntrospectedTable().getTableConfiguration().getTableName()));
                }
            }
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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
}
