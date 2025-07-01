package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.LayuiTableColumnMeta;
import com.vgosoft.core.annotation.VueFormItemRule;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;

import static com.vgosoft.tool.core.VStringUtil.format;
import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * 用于动态构造@LayuiTableColumnMeta()注解的类
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-06-02 20:08
 * @version 4.0
 */

@Getter
@Setter
public class LayuiTableColumnMetaDesc extends AbstractAnnotation{

        public static final String ANNOTATION_NAME = "@LayuiTableColumnMeta";

        private String value;

        private String width;
        private int minWidth;
        private String fixed;
        private String templet;
        private String rules;
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

        private String tsType;

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
            if (VStringUtil.stringHasValue(this.getRules())) {
                this.addImports(VueFormItemRule.class.getCanonicalName());
                items.add(VStringUtil.format("\n            rules = '{'{0}'}'\n            ", this.getRules()));
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
            if (stringHasValue(align) && !this.getAlign().equals("center")) {
                items.add(format("align = \"{0}\"", align));
            }
            if (stringHasValue(tsType) && !tsType.equals("string")) {
                items.add(format("tsType = \"{0}\"", tsType));
            }
            return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
        }
}
