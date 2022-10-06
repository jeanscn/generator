package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.ConstantsUtil;

import java.sql.JDBCType;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-07 01:58
 * @version 3.0
 */
public class ColumnMeta  extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@ColumnMeta";

    private String value;
    private String description;
    private boolean pkid;
    private boolean summary;
    private int order;
    private boolean sort;
    private  int size;
    private  int scale;
    private JDBCType type;
    private String dataFormat;
    private  boolean nullable;
    private  String charSet;
    private  String defaultValue;
    private  String remarks;
    private  String position;

    public static ColumnMeta create(IntrospectedColumn introspectedColumn){
        return new ColumnMeta(introspectedColumn);
    }

    public ColumnMeta(IntrospectedColumn introspectedColumn) {
        this.value = introspectedColumn.getActualColumnName();
        this.description = introspectedColumn.getRemarks(true);
        this.pkid = introspectedColumn.isIdentity();
        this.summary = true;
        this.order = introspectedColumn.getOrder();
        this.sort = true;
        this.size = introspectedColumn.getLength();
        this.scale = introspectedColumn.getScale();
        this.type = JDBCType.valueOf(introspectedColumn.getJdbcType());
        this.dataFormat = introspectedColumn.getDatePattern();
        this.nullable = introspectedColumn.isNullable();
        this.charSet = "utf8mb4";
        this.defaultValue = "";
        this.remarks = introspectedColumn.getRemarks(false);
        this.position = "";
        this.addImports(ConstantsUtil.ANNOTATION_COLUMN_META);
    }

    @Override
    public String toAnnotation() {
        items.add(VStringUtil.format("value = \"{0}\"", this.getValue()));
        items.add(VStringUtil.format("description = \"{0}\"",
                VStringUtil.isNotBlank(description)?this.getValue():this.getDescription()));
        if (pkid) {
            items.add("pkid = true");
        }
        if (!this.isSummary()) {
            items.add("summary = false");
        }
        if (this.getOrder()!= GlobalConstant.COLUMN_META_ANNOTATION_DEFAULT_ORDER) {
            items.add(VStringUtil.format("order = {0}", this.getOrder()));
        }
        if (!this.isSort()) {
            items.add("sort = false");
        }
        if (this.getSize()!=255) {
            items.add(VStringUtil.format("size = {0}", this.getSize()));
        }
        if (this.getScale()!=0) {
            items.add(VStringUtil.format("scale = {0}", this.getScale()));
        }
        if (!this.getType().equals(JDBCType.VARCHAR)) {
            items.add(VStringUtil.format("type = JDBCType.{0}", this.getType().getName()));
            this.addImports("java.sql.JDBCType");
        }
        if (VStringUtil.isNotBlank(this.getDataFormat())) {
            items.add(VStringUtil.format("dataFormat = \"{0}\"", this.getDataFormat()));
        }
        if (!this.isNullable()) {
            items.add("nullable = false");
        }
        if (!this.getCharSet().equals("utf8mb4")) {
            items.add(VStringUtil.format("charSet = \"{0}\"", this.getCharSet()));
        }
        if (VStringUtil.isNotBlank(this.getDefaultValue())) {
            items.add(VStringUtil.format("defaultValue = \"{0}\"", this.getDefaultValue()));
        }
        items.add(VStringUtil.format("remarks = \"{0}\"", this.getRemarks()));
        if (VStringUtil.isNotBlank(this.getPosition())) {
            items.add(VStringUtil.format("position = \"{0}\"", this.getPosition()));
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

    public boolean isPkid() {
        return pkid;
    }

    public void setPkid(boolean pkid) {
        this.pkid = pkid;
    }

    public boolean isSummary() {
        return summary;
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isSort() {
        return sort;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public JDBCType getType() {
        return type;
    }

    public void setType(JDBCType type) {
        this.type = type;
        this.addImports("java.sql.JDBCType");
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCharSet() {
        return charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }
}
