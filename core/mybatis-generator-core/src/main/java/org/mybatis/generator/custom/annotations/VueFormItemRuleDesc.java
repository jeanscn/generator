package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.VueFormItemRule;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.codegen.mybatis3.vue.FormItemRule;

/**
 * ElementPlus表单元数据描述注解
 *
 * @version 6.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VueFormItemRuleDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + VueFormItemRule.class.getSimpleName();

    private String type;
    private Boolean required;
    private Boolean witespace;
    private String message;
    private String trigger;
    private Integer min;
    private Integer max;
    private Integer len;
    private String pattern;

    public VueFormItemRuleDesc(IntrospectedColumn introspectedColumn) {
        super();
        this.addImports(VueFormItemRule.class.getCanonicalName());
    }

    public VueFormItemRuleDesc(FormItemRule formItemRule) {
        super();
        this.addImports(VueFormItemRule.class.getCanonicalName());

        if (VStringUtil.stringHasValue(formItemRule.getType())) {
            this.setType(formItemRule.getType());
        }
        if (formItemRule.isRequired()) {
            this.setRequired(true);
        }
        if (formItemRule.isWitespace()) {
            this.setWitespace(true);
        }
        if (VStringUtil.stringHasValue(formItemRule.getMessage())) {
            this.setMessage(formItemRule.getMessage());
        }
        if (VStringUtil.stringHasValue(formItemRule.getTrigger())) {
            this.setTrigger(formItemRule.getTrigger());
        }
        if (formItemRule.getMin() > 0) {
            this.setMin(formItemRule.getMin());
        }
        if (formItemRule.getMax() > 0) {
            this.setMax(formItemRule.getMax());
        }
        if (formItemRule.getLen() > 0) {
            this.setLen(formItemRule.getLen());
        }
        if (VStringUtil.stringHasValue(formItemRule.getPattern())) {
            this.setPattern(formItemRule.getPattern());
        }
        if (VStringUtil.stringHasValue(formItemRule.getType())) {
            this.setType(formItemRule.getType());
        }

    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(this.getType())) {
            items.add(VStringUtil.format("type = \"{0}\"", this.getType()));
        }
        if (this.getRequired() != null) {
            items.add(VStringUtil.format("required = {0}", this.getRequired()));
        }
        if (this.getWitespace() != null) {
            items.add(VStringUtil.format("witespace = {0}", this.getWitespace()));
        }
        if (VStringUtil.isNotBlank(this.getMessage())) {
            items.add(VStringUtil.format("message = \"{0}\"", this.getMessage()));
        }
        if (VStringUtil.isNotBlank(this.getTrigger())) {
            items.add(VStringUtil.format("trigger = \"{0}\"", this.getTrigger()));
        }
        if (this.getMin() != null) {
            items.add(VStringUtil.format("min = {0}", String.valueOf(this.getMin())));
        }
        if (this.getMax() != null) {
            items.add(VStringUtil.format("max = {0}", String.valueOf(this.getMax())));
        }
        if (this.getLen() != null) {
            items.add(VStringUtil.format("len = {0}", String.valueOf(this.getLen())));
        }
        if (VStringUtil.isNotBlank(this.getPattern())) {
            items.add(VStringUtil.format("pattern = \"{0}\"", this.getPattern()));
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

}
