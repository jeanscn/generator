package org.mybatis.generator.codegen.mybatis3.vue;

import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.core.pojo.IBaseDTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.VueFormItemMetaDesc;

@Data
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

}
