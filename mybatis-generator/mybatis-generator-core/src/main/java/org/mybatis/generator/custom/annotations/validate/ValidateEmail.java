package org.mybatis.generator.custom.annotations.validate;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * 注解：@Email
 * 适用类型：字符串
 * 验证内容：电子邮箱格式
 *
 * @author vgosoft-agent
 */
@Setter
@Getter
public class ValidateEmail extends AbstractAnnotation {

    private String message;
    private String[] groups;

    public static ValidateEmail create(String message, String[] groups) {
        return new ValidateEmail(message, groups);
    }

    public ValidateEmail() {
        super();
        this.addImports("jakarta.validation.constraints.Email");
    }

    public ValidateEmail(String message, String[] groups) {
        super();
        this.message = message;
        this.groups = groups;
        this.addImports("jakarta.validation.constraints.Email");
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
            return "@Email(" + String.join(", ", items.toArray(new String[0])) + ")";
        } else {
            return "@Email";
        }
    }
}
