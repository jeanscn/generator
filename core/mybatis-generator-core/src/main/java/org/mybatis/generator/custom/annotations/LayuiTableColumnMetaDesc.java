package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.LayuiTableColumnMeta;

import static com.vgosoft.tool.core.VStringUtil.format;
import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * 用于动态构造@LayuiTableColumnMeta()注解的类
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-06-02 20:08
 * @version 4.0
 */
public class LayuiTableColumnMetaDesc extends AbstractAnnotation{

        public static final String ANNOTATION_NAME = "@LayuiTableColumnMeta";

        private String value;

        private String width;
        private int minWidth;
        private String fixed;
        private String templet;
        private boolean totalRow;
        private boolean edit;

        private String editor;

        private boolean hide;
        private boolean sort;
        private int colspan;
        private int rowspan;

        private int order;

        private String scope;

        private String align;

        private String label;

        public LayuiTableColumnMetaDesc() {
            super();
            this.addImports(LayuiTableColumnMeta.class.getCanonicalName());
        }

        @Override
        public String toAnnotation() {
            if (stringHasValue(value)) {
                items.add(format("value = \"{0}\"", value));
            }
            if (stringHasValue(width)) {
                items.add(format("width = \"{0}\"", width));
            }
            if (minWidth > 0) {
                items.add(format("minWidth = {0}", minWidth));
            }
            if (stringHasValue(fixed)) {
                items.add(format("fixed = \"{0}\"", fixed));
            }
            if (stringHasValue(templet)) {
                items.add(format("templet = \"{0}\"", templet));
            }
            if (totalRow) {
                items.add("totalRow = true");
            }
            if (edit) {
                items.add("edit = true");
            }
            if (hide) {
                items.add("hide = true");
            }
            if (sort) {
                items.add("sort = true");
            }
            if (colspan > 0) {
                items.add(format("colspan = {0}", colspan));
            }
            if (rowspan > 0) {
                items.add(format("rowspan = {0}", rowspan));
            }
            if (order > 0) {
                items.add(format("order = {0}", order));
            }
            if (stringHasValue(editor)) {
                items.add(format("editor = \"{0}\"", editor));
            }
            if (stringHasValue(scope) && !scope.equals("both")) {
                items.add(format("scope = \"{0}\"", scope));
            }
            if (stringHasValue(label)) {
                items.add(format("label = \"{0}\"", label));
            }
            if (stringHasValue(align)) {
                items.add(format("align = \"{0}\"", align));
            }
            return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
        }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public String getFixed() {
        return fixed;
    }

    public void setFixed(String fixed) {
        this.fixed = fixed;
    }

    public String getTemplet() {
        return templet;
    }

    public void setTemplet(String templet) {
        this.templet = templet;
    }

    public boolean isTotalRow() {
        return totalRow;
    }

    public void setTotalRow(boolean totalRow) {
        this.totalRow = totalRow;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public boolean isSort() {
        return sort;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public int getColspan() {
        return colspan;
    }

    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    public int getRowspan() {
        return rowspan;
    }

    public void setRowspan(int rowspan) {
        this.rowspan = rowspan;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
