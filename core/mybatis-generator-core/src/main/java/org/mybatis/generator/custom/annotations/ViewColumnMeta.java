package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.config.VOViewGeneratorConfiguration;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-06 23:42
 * @version 3.0
 */
public class ViewColumnMeta extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@ViewColumnMeta";

    private String value;

    private String title;

    private String width;

    private String defaultContent;

    private String searchable;

    private String orderable;

    private String render;

    private String datafmt;

    private boolean ignore;

    private int order;

    private boolean defaultDisplay;

    private IntrospectedColumn introspectedColumn;

    private final VOViewGeneratorConfiguration configuration;

    private Field field;

    public static ViewColumnMeta create(IntrospectedColumn introspectedColumn,IntrospectedTable introspectedTable){
        return new ViewColumnMeta(introspectedColumn,introspectedTable);
    }

    public ViewColumnMeta(Field field,String title,IntrospectedTable introspectedTable){
        super();
        this.value = field.getName();
        this.title = title;
        this.field = field;
        this.configuration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
        this.addImports("com.vgosoft.core.annotation.ViewColumnMeta");
    }

    public ViewColumnMeta(IntrospectedColumn introspectedColumn,IntrospectedTable introspectedTable) {
        super();
        this.value = introspectedColumn.getJavaProperty();
        this.title = introspectedColumn.getRemarks(true)!=null?introspectedColumn.getRemarks(true):introspectedColumn.getActualColumnName();
       this.introspectedColumn = introspectedColumn;
        this.configuration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
        this.addImports("com.vgosoft.core.annotation.ViewColumnMeta");
    }

    @Override
    public String toAnnotation() {
        items.add(VStringUtil.format("value = \"{0}\"", this.value));
        items.add(VStringUtil.format("title = \"{0}\"", this.title));
        if (introspectedColumn!=null && introspectedColumn.getOrder()!= GlobalConstant.COLUMN_META_ANNOTATION_DEFAULT_ORDER) {
            items.add(VStringUtil.format("order = {0}", introspectedColumn.getOrder()));
        }
        if (configuration.getDefaultDisplayFields().size()==0 || configuration.getDefaultDisplayFields().contains(this.value)) {
            items.add("defaultDisplay = true");
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getDefaultContent() {
        return defaultContent;
    }

    public void setDefaultContent(String defaultContent) {
        this.defaultContent = defaultContent;
    }

    public String getSearchable() {
        return searchable;
    }

    public void setSearchable(String searchable) {
        this.searchable = searchable;
    }

    public String getOrderable() {
        return orderable;
    }

    public void setOrderable(String orderable) {
        this.orderable = orderable;
    }

    public String getRender() {
        return render;
    }

    public void setRender(String render) {
        this.render = render;
    }

    public String getDatafmt() {
        return datafmt;
    }

    public void setDatafmt(String datafmt) {
        this.datafmt = datafmt;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isDefaultDisplay() {
        return defaultDisplay;
    }

    public void setDefaultDisplay(boolean defaultDisplay) {
        this.defaultDisplay = defaultDisplay;
    }
}
