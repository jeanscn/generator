package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.VueFormUploadMeta;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.config.HtmlFileAttachmentConfiguration;

/**
 * ElementPlus表单元数据描述注解
 *
 * @version 6.0
 */
public class VueFormUploadMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + VueFormUploadMeta.class.getSimpleName();

    private String value = "";
    private boolean multiple = true;
    private String type = "text";
    private String location = "table";
    private int limit = 0;
    private String tip = "";

    private String label = "附件";

    private String restBasePath = "/bizcore/vbiz-file-attachment-impl";

    private String afterColumn;

    private Integer span = 24;

    private int order = 10;

    public VueFormUploadMetaDesc() {
        super();
    }

    public VueFormUploadMetaDesc(HtmlFileAttachmentConfiguration configuration) {
        super();
        this.value = configuration.getElementKey();
        this.items.add(VStringUtil.format("value = \"{0}\"", this.getValue()));

        this.multiple = configuration.isMultiple();
        if (!multiple) {
            this.items.add(VStringUtil.format("multiple = {0}", this.isMultiple()));
        }
        this.type = configuration.getType();
        if(!type.equals("text")) {
            this.items.add(VStringUtil.format("type = \"{0}\"", this.getType()));
        }
        this.location = configuration.getLocation();
        if(!location.equals("table")) {
            this.items.add(VStringUtil.format("location = \"{0}\"", this.getLocation()));
        }
        this.limit = configuration.getLimit();
        if (limit!=0) {
            this.items.add(VStringUtil.format("limit = {0}", this.getLimit()));
        }
        this.tip = configuration.getTip();
        if (!"".equals(this.getTip())) {
            this.items.add(VStringUtil.format("tip = \"{0}\"", this.getTip()));
        }
        this.label = configuration.getLabel();
        if(!"附件".equals(this.getLabel())) {
            this.items.add(VStringUtil.format("label = \"{0}\"", this.getLabel()));
        }
        this.restBasePath = configuration.getRestBasePath();
        if (!"".equals(this.getRestBasePath())) {
            this.items.add(VStringUtil.format("restBasePath = \"{0}\"", this.getRestBasePath()));
        }
        this.afterColumn = configuration.getAfterColumn();
        if (!"".equals(this.getAfterColumn())) {
            this.items.add(VStringUtil.format("afterColumn = \"{0}\"", this.getAfterColumn()));
        }
        if (this.getSpan() != 24) {
            this.items.add(VStringUtil.format("span = {0}", this.getSpan()));
        }
        if (this.getOrder() != 10) {
            this.items.add(VStringUtil.format("order = {0}", this.getOrder()));
        }
        this.addImports(VueFormUploadMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRestBasePath() {
        return restBasePath;
    }

    public void setRestBasePath(String restBasePath) {
        this.restBasePath = restBasePath;
    }

    public String getAfterColumn() {
        return afterColumn;
    }

    public void setAfterColumn(String afterColumn) {
        this.afterColumn = afterColumn;
    }

    public Integer getSpan() {
        return span;
    }

    public void setSpan(Integer span) {
        this.span = span;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
