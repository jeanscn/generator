package org.mybatis.generator.codegen.mybatis3.vue;

import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.core.pojo.IBaseDTO;
import org.mybatis.generator.custom.annotations.VueFormItemMetaDesc;

public class FormItemRule implements IBaseDTO {
    private static final long serialVersionUID = 1L;

    private String type;
    private boolean required = false;
    private boolean witespace;
    private String message;
    private String trigger;
    private int min = 0;
    private int max;
    private int len;
    private String pattern;

    private VueFormItemMetaDesc vueFormItemMetaDesc;

    public FormItemRule(VueFormItemMetaDesc vueFormItemMetaDesc) {
        this.vueFormItemMetaDesc = vueFormItemMetaDesc;
        if (HtmlElementTagTypeEnum.INPUT.codeName().equals(vueFormItemMetaDesc.getComponent())) {
            this.setTrigger("blur");
        } else {
            this.setTrigger("change");
        }
    }

    public FormItemRule(String message){
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isWitespace() {
        return witespace;
    }

    public void setWitespace(boolean witespace) {
        this.witespace = witespace;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public VueFormItemMetaDesc getVueFormItemMetaDesc() {
        return vueFormItemMetaDesc;
    }

    public void setVueFormItemMetaDesc(VueFormItemMetaDesc vueFormItemMetaDesc) {
        this.vueFormItemMetaDesc = vueFormItemMetaDesc;
    }
}
