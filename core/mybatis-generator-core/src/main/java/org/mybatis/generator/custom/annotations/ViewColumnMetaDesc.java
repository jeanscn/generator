package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.ViewColumnMeta;
import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.VoViewGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.Set;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-06 23:42
 * @version 3.0
 */
@Setter
@Getter
public class ViewColumnMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@ViewColumnMeta";

    private String value;

    private String title;

    private String width;

    private String minWidth;

    private String headerAlign = "left";

    private String align = "left";

    private String fixed = "";

    private boolean edit = false;

    private boolean hide = false;

    private String defaultContent;

    private String searchable;

    private String orderable = "true";

    private String render;

    private String dataType;

    private String dataFormat;

    private boolean ignore;

    private int order;

    private boolean defaultDisplay;

    private String columnName;

    private final IntrospectedTable introspectedTable;

    private IntrospectedColumn introspectedColumn;

    private final VoViewGeneratorConfiguration configuration;

    Set<String> hiddenProperties;

    public static ViewColumnMetaDesc create(IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        return new ViewColumnMetaDesc(introspectedColumn, introspectedTable);
    }

    public ViewColumnMetaDesc(Field field, String title, IntrospectedTable introspectedTable) {
        super();
        this.introspectedTable = introspectedTable;
        this.value = field.getName();
        this.title = title;
        this.columnName = field.getSourceColumnName();
        this.configuration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
        this.addImports(ViewColumnMeta.class.getCanonicalName());
        parseDefaultFormatRender(field.getType(), field.getName());
        parseRenderFun(field.getName());
    }

    public ViewColumnMetaDesc(IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        super();
        this.introspectedColumn = introspectedColumn;
        this.introspectedTable = introspectedTable;
        this.value = introspectedColumn.getJavaProperty();
        this.title = introspectedColumn.getRemarks(true) != null ? introspectedColumn.getRemarks(true) : introspectedColumn.getActualColumnName();
        this.configuration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
        this.columnName = introspectedColumn.getActualColumnName();
        this.addImports(ViewColumnMeta.class.getCanonicalName());
        parseDefaultFormatRender(introspectedColumn.getFullyQualifiedJavaType(), introspectedColumn.getJavaProperty());
        parseRenderFun(introspectedColumn.getJavaProperty());
    }

    private void parseRenderFun(String fieldName) {
        //指定的render
        configuration.getVoColumnRenderFunGeneratorConfigurations().stream()
                .filter(configuration -> configuration.getFieldNames().contains(fieldName))
                .findFirst()
                .ifPresent(voColumnRenderFunGeneratorConfiguration -> this.render = voColumnRenderFunGeneratorConfiguration.getRenderFun());
    }

    private void parseDefaultFormatRender(FullyQualifiedJavaType javaType, String fieldName) {
        //默认的render
        switch (javaType.getShortNameWithoutTypeArguments()) {
            case "Date":
            case "LocalDateTime":
            case "Instant":
                this.render = "colDefsDatefmt";
                this.dataType = "datetime";
                this.dataFormat = "yyyy-MM-dd HH:mm:ss";
                break;
            case "LocalDate":
                this.render = "colDefsDatefmt";
                this.dataType = "date";
                this.dataFormat = "yyyy-MM-dd";
                break;
            case "LocalTime":
                this.render = "colDefsDatefmt";
                this.dataType = "time";
                this.dataFormat = "HH:mm:ss";
                break;
            case "Boolean":
            case "boolean":
                this.dataType = "boolean";
                break;
            case "Double":
            case "double":
            case "Float":
            case "float":
                this.dataType = "number";
                this.dataFormat = "0.00";
                break;
            case "Integer":
            case "int":
            case "Long":
            case "long":
            case "Short":
            case "short":
                this.dataType = "number";
                this.dataFormat = "0";
                break;
        }
        if (fieldName.equals("state")) {
            this.render = "colDefsState";
        }
    }

    @Override
    public String toAnnotation() {
        if (configuration.getDefaultHiddenFields().isEmpty() && configuration.getDefaultDisplayFields().isEmpty()) {
            configuration.getDefaultHiddenFields().addAll(StringUtility.splitToSet(GlobalConstant.VIEW_VO_DEFAULT_HIDDEN_FIELDS));
        }
        items.add(VStringUtil.format("value = \"{0}\"", this.value));
        items.add(VStringUtil.format("title = \"{0}\"", this.title));
        int o = this.order > 0 ? this.order :
                (introspectedColumn != null && introspectedColumn.getOrder() > 0) ? introspectedColumn.getOrder() : 0;
        if (o > 0 && o != GlobalConstant.COLUMN_META_ANNOTATION_DEFAULT_ORDER) {
            items.add(VStringUtil.format("order = {0}", o));
        }
        if (this.width != null) {
            items.add(VStringUtil.format("width = \"{0}\"", this.width));
        }
        if (this.minWidth != null) {
            items.add(VStringUtil.format("minWidth = \"{0}\"", this.minWidth));
        }
        if (this.headerAlign != null && !this.headerAlign.equals("left")) {
            items.add(VStringUtil.format("headerAlign = \"{0}\"", this.headerAlign));
        }
        if (this.align != null && !this.align.equals("left")) {
            items.add(VStringUtil.format("align = \"{0}\"", this.align));
        }
        if (this.fixed != null && !this.fixed.isEmpty()) {
            items.add(VStringUtil.format("fixed = \"{0}\"", this.fixed));
        }
        if (VStringUtil.stringHasValue(this.columnName)) {
            items.add(VStringUtil.format("columnName = \"{0}\"", this.columnName));
        }
        if (this.edit) {
            items.add("edit = true");
        }
        if (this.hide) {
            items.add("hide = true");
        }

        if (this.defaultContent != null) {
            items.add(VStringUtil.format("defaultContent = \"{0}\"", this.defaultContent));
        }
        if (this.searchable != null) {
            items.add(VStringUtil.format("searchable = {0}", this.searchable));
        }
        if (this.orderable.equalsIgnoreCase("false")) {
            items.add("orderable = false");
        }
        if (this.render != null) {
            items.add(VStringUtil.format("render = \"{0}\"", this.render));
        }
        if (this.dataType != null && !this.dataType.equals("string")) {
            items.add(VStringUtil.format("dataType = \"{0}\"", this.dataType));
        }
        if (this.dataFormat != null) {
            items.add(VStringUtil.format("dataFormat = \"{0}\"", this.dataFormat));
        }
        if (this.ignore) {
            items.add("ignore = true");
        }
        if (!configuration.getDefaultDisplayFields().isEmpty()) {
            if (configuration.getDefaultDisplayFields().contains(this.value)) {
                items.add("defaultDisplay = true");
            }
        }else{
            Set<String> hiddenProperties = getHiddenProperties();
            hiddenProperties.addAll(configuration.getDefaultHiddenFields());
            if (!hiddenProperties.isEmpty()){
                if (!hiddenProperties.contains(this.value)) {
                    items.add("defaultDisplay = true");
                }
            }else{
                items.add("defaultDisplay = true");
            }
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    public Set<String> getHiddenProperties() {
        if (hiddenProperties != null) {
            return hiddenProperties;
        }
        return this.introspectedTable.getTableConfiguration().getHtmlHiddenFields();
    }
}
