package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.constant.enums.FieldTypeEnum;
import com.vgosoft.core.constant.enums.QueryModesEnum;
import com.vgosoft.core.constant.enums.TagNamesEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-07 06:58
 * @version 3.0
 */
public class CompositeQuery extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@CompositeQuery";

    private String value;
    private String description;
    private TagNamesEnum tagName = TagNamesEnum.INPUT;
    private QueryModesEnum queryMode = QueryModesEnum.EQUAL;
    private int order;
    private FieldTypeEnum fieldType = FieldTypeEnum.TEXT;
    private String dataUrl;

    public static CompositeQuery create(IntrospectedColumn introspectedColumn){
        return new CompositeQuery(introspectedColumn);
    }

    public CompositeQuery(IntrospectedColumn introspectedColumn) {
        super();
        this.value = introspectedColumn.getActualColumnName();
        this.items.add(VStringUtil.format("value=\"{0}\"", this.value));
        this.description = introspectedColumn.getRemarks(true);
        this.items.add(VStringUtil.format("description=\"{0}\"", this.description));
        this.addImports("com.vgosoft.core.annotation.CompositeQuery");
    }

    @Override
    public String toAnnotation() {
        if (!this.tagName.equals(TagNamesEnum.INPUT)) {
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

    public TagNamesEnum getTagName() {
        return tagName;
    }

    public void setTagName(TagNamesEnum tagName) {
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
}
