package org.mybatis.generator.custom.annotations.validate;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * 注解：@DecimalMax
 * 适用类型：数值类型、字符串
 * 验证内容：最大值（支持小数）
 * 典型场景：价格、重量等小数验证
 */

@Setter
@Getter
public class DecimalMax extends AbstractAnnotation {
    private String value;
    private String message;
    private String[] groups;

    public static DecimalMax create(String value, String message, String[] groups) {
        return new DecimalMax(value, message, groups);
    }

    public DecimalMax() {
        super();
        this.addImports("javax.validation.constraints.DecimalMax");
    }

    public DecimalMax(String value, String message, String[] groups) {
        super();
        this.value = value;
        this.message = message;
        this.groups = groups;
        this.addImports("javax.validation.constraints.DecimalMax");
    }

    @Override
    public String toAnnotation() {
        if (value != null && !value.isEmpty()) {
            this.items.add("value = \"" + value + "\"");
        }
        if (message != null && !message.isEmpty()) {
            this.items.add("message = \"" + message + "\"");
        }
        if (groups != null && groups.length > 0) {
            this.items.add("groups = {" + String.join(", ", groups) + "}");
        }
        if (!this.items.isEmpty()) return "@DecimalMax(" + String.join(", ", items.toArray(new String[0])) + ")";
        else return "@DecimalMax";
    }
}
