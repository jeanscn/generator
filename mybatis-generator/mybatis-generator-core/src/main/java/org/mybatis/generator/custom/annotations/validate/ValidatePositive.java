package org.mybatis.generator.custom.annotations.validate;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * 注解：@Positive
 * 适用类型：数值类型
 * 验证内容：严格大于 0 的数值
 *
 * @author vgosoft-agent
 */
@Setter
@Getter
public class ValidatePositive extends AbstractAnnotation {

    private String message;
    private String[] groups;

    public static ValidatePositive create(String message, String[] groups) {
        return new ValidatePositive(message, groups);
    }

    public ValidatePositive() {
        super();
        this.addImports("jakarta.validation.constraints.Positive");
    }

    public ValidatePositive(String message, String[] groups) {
        super();
        this.message = message;
        this.groups = groups;
        this.addImports("jakarta.validation.constraints.Positive");
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.stringHasValue(message)) {
            this.items.add("message = \"" + message + "\"");
        }
        if (groups != null && groups.length > 0) {
            this.items.add("groups = {" + String.join(", ", groups) + "}");
        }
        if (!this.items.isEmpty()) {
            return "@Positive(" + String.join(", ", items.toArray(new String[0])) + ")";
        } else {
            return "@Positive";
        }
    }
}
