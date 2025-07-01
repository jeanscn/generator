package org.mybatis.generator.custom.annotations.validate;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 *  注解：@DecimalMin
 *  适用类型：数值类型、字符串
 *  验证内容：最小值（支持小数）
 *  典型场景：价格、重量等小数验证
 */

@Setter
@Getter
public class DecimalMin extends AbstractAnnotation {
    private String value;
    private String message;
    private String[] groups;

    public static DecimalMin create(String value, String message, String[] groups) {
        return new DecimalMin(value, message, groups);
    }

    public DecimalMin() {
        super();
        this.addImports("javax.validation.constraints.DecimalMin");
    }

    public DecimalMin(String value, String message, String[] groups) {
        super();
        this.value = value;
        this.message = message;
        this.groups = groups;
        this.addImports("javax.validation.constraints.DecimalMin");
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.stringHasValue(value)) {
            this.items.add("value = \"" + value + "\"");
        }
        if (VStringUtil.stringHasValue(message)) {
            this.items.add("message = \"" + message + "\"");
        }
        if (groups != null && groups.length > 0) {
            this.items.add("groups = {" + String.join(", ", groups) + "}");
        }
        if (!this.items.isEmpty()) return "@DecimalMin(" + String.join(", ", items.toArray(new String[0])) + ")";
        else return "@DecimalMin";
    }
}
