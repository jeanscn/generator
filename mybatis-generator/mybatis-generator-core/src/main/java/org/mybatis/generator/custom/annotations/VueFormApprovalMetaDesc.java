package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.VueFormApprovalMeta;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.config.HtmlApprovalCommentConfiguration;

public class VueFormApprovalMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + VueFormApprovalMeta.class.getSimpleName();

    private int rows = 3;
    private String elementKey = "";
    private String placeholder = "请输入审批意见";
    private String busLocation = "审批意见";
    private String label = "审批意见";

    private int span = 24;

    private String afterColumn = "";

    private String tips = "";

    public VueFormApprovalMetaDesc() {
        super();
    }

    public VueFormApprovalMetaDesc(HtmlApprovalCommentConfiguration configuration) {
        super();
        this.rows = configuration.getRows();
        this.elementKey = configuration.getElementKey();
        this.placeholder = configuration.getPlaceholder();
        this.busLocation = configuration.getLocationTag();
        this.label = configuration.getLabel();
        this.span = configuration.getSpan();
        this.afterColumn = configuration.getAfterColumn();
        this.tips = configuration.getTips();
    }

    @Override
    public String toAnnotation() {
        if (this.rows != 3) {
            items.add("rows = " + this.rows);
        }
        if (!this.elementKey.isEmpty()) {
            items.add("elementKey = \"" + this.elementKey + "\"");
        }
        if (VStringUtil.stringHasValue(this.placeholder) && !this.placeholder.equals("请输入审批意见")) {
            items.add("placeholder = \"" + this.placeholder + "\"");
        }
        if (VStringUtil.stringHasValue(this.busLocation) && !this.busLocation.equals("审批意见")) {
            items.add("busLocation = \"" + this.busLocation + "\"");
        }
        if (VStringUtil.stringHasValue(this.label) && !this.label.equals("审批意见")) {
            items.add("label = \"" + this.label + "\"");
        }
        if (this.span>0 && this.span != 24) {
            items.add("span = " + this.span);
        }
        if (VStringUtil.stringHasValue(this.afterColumn)) {
            items.add("afterColumn = \"" + this.afterColumn + "\"");
        }
        if (VStringUtil.stringHasValue(this.tips)) {
            items.add("tips = \"" + this.tips + "\"");
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    public String getAnnotationName() {
        return ANNOTATION_NAME;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getElementKey() {
        return elementKey;
    }

    public void setElementKey(String elementKey) {
        this.elementKey = elementKey;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getBusLocation() {
        return busLocation;
    }

    public void setBusLocation(String busLocation) {
        this.busLocation = busLocation;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getSpan() {
        return span;
    }

    public void setSpan(int span) {
        this.span = span;
    }

    public String getAfterColumn() {
        return afterColumn;
    }

    public void setAfterColumn(String afterColumn) {
        this.afterColumn = afterColumn;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
