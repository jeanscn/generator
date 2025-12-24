package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.ColumnMeta;
import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.IntrospectedColumn;

import java.sql.JDBCType;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-07 01:58
 * @version 3.0
 */
@Getter
@Setter
public class ColumnMetaDesc extends AbstractAnnotation{

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

    public static ColumnMetaDesc create(IntrospectedColumn introspectedColumn){
        return new ColumnMetaDesc(introspectedColumn);
    }

    public ColumnMetaDesc(IntrospectedColumn introspectedColumn) {
        this.value = introspectedColumn.getActualColumnName();
        this.description = introspectedColumn.getRemarks(false);
        this.pkid = introspectedColumn.isPrimaryKey();
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
        this.remarks = introspectedColumn.getRemarks(true);
        this.position = "";
        this.addImports(ColumnMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        items.add(VStringUtil.format("value = \"{0}\"", this.getValue()));
        items.add(VStringUtil.format("description = \"{0}\"",
                VStringUtil.isNotBlank(description)?this.getDescription():this.getValue()));
        if (pkid) {
            items.add("pkid = true");
        }
        if (!this.isSummary()) {
            items.add("summary = false");
        }
        if (this.getOrder()!= GlobalConstant.COLUMN_META_ANNOTATION_DEFAULT_ORDER) {
            items.add("order = "+this.getOrder());
        }
        if (!this.isSort()) {
            items.add("sort = false");
        }
        if (this.getSize()!=255) {
            items.add("size = "+this.getSize());
        }
        if (this.getScale()!=0) {
            items.add("scale = "+this.getScale());
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
        if (!"utf8mb4".equals(this.getCharSet())) {
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

    public void setType(JDBCType type) {
        this.type = type;
        this.addImports("java.sql.JDBCType");
    }
}
