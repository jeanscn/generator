package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.VueFormUploadMeta;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.config.HtmlFileAttachmentConfiguration;

/**
 * ElementPlus表单元数据描述注解
 *
 * @author cen_c
 * @version 6.0
 */
@Setter
@Getter
public class VueFormUploadMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + VueFormUploadMeta.class.getSimpleName();

    private String value = "";
    private boolean multiple = true;
    private String type = "text";
    private String location = "table";
    private int limit = 0;
    private String tips = "";

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
        if (!"text".equals(type)) {
            this.items.add(VStringUtil.format("type = \"{0}\"", this.getType()));
        }
        this.location = configuration.getLocation();
        if (!"table".equals(location)) {
            this.items.add(VStringUtil.format("location = \"{0}\"", this.getLocation()));
        }
        this.limit = configuration.getLimit();
        if (limit != 0) {
            this.items.add(VStringUtil.format("limit = {0}", this.getLimit()));
        }
        this.tips = configuration.getTips();
        if (VStringUtil.stringHasValue(this.getTips())) {
            this.items.add(VStringUtil.format("tips = \"{0}\"", this.getTips()));
        }
        this.label = configuration.getLabel();
        if (!"附件".equals(this.getLabel())) {
            this.items.add(VStringUtil.format("label = \"{0}\"", this.getLabel()));
        }
        this.restBasePath = configuration.getRestBasePath();
        if (VStringUtil.stringHasValue(this.getRestBasePath())) {
            this.items.add(VStringUtil.format("restBasePath = \"{0}\"", this.getRestBasePath()));
        }
        this.afterColumn = configuration.getAfterColumn();
        if (VStringUtil.stringHasValue(this.getAfterColumn())) {
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

}
