package org.mybatis.generator.custom.annotations.validate;

import com.vgosoft.core.constant.SymbolConstants;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * 注解:Size
 * 适用类型：String, Collection, Map, Array
 * 验证内容：长度/大小
 * 典型场景：字符串长度、集合大小
 *
 * @author cen_c
 */

@Setter
@Getter
public class ValidateSize extends AbstractAnnotation {

    private String min;
    private String max;
    private String message;
    private String[] groups;

    public static ValidateSize create(String min, String max, String message, String[] groups) {
        return new ValidateSize(min, max, message, groups);
    }

    public ValidateSize() {
        super();
        this.addImports("jakarta.validation.constraints.Size");
    }

    public ValidateSize(String min, String max, String message, String[] groups) {
        super();
        this.min = min;
        this.max = max;
        this.message = message;
        this.groups = groups;
        this.addImports("jakarta.validation.constraints.Size");
    }

    @Override
    public String toAnnotation() {
        if (min != null && !min.equalsIgnoreCase("") && !"0".equals(min)) {
            this.items.add("min = " + min);
        }
        if (max != null && !max.equalsIgnoreCase(SymbolConstants.EMPTY_STRING) && !"2147483647".equals(max)) {
            this.items.add("max = " + max);
        }
        if (message != null && !message.equalsIgnoreCase(SymbolConstants.EMPTY_STRING)) {
            this.items.add("message = \"" + message + "\"");
        }
        if (groups != null && groups.length > 0) {
            this.items.add("groups = {" + String.join(", ", groups) + "}");
        }
        if (!this.items.isEmpty()) {
            return "@Size(" + String.join(", ", items.toArray(new String[0])) + ")";
        } else {
            return "@Size";
        }
    }

}
