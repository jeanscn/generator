package org.mybatis.generator.custom.annotations.validate;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * 注解：@Pattern
 * 适用类型：字符串
 * 验证内容：正则表达式匹配
 * 典型场景：手机号、身份证、邮编、IP 等格式校验
 *
 * @author vgosoft-agent
 */
@Setter
@Getter
public class ValidatePattern extends AbstractAnnotation {

    private String regexp;
    private String message;
    private String[] groups;

    public static ValidatePattern create(String regexp, String message, String[] groups) {
        return new ValidatePattern(regexp, message, groups);
    }

    public ValidatePattern() {
        super();
        this.addImports("jakarta.validation.constraints.Pattern");
    }

    public ValidatePattern(String regexp, String message, String[] groups) {
        super();
        this.regexp = regexp;
        this.message = message;
        this.groups = groups;
        this.addImports("jakarta.validation.constraints.Pattern");
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.stringHasValue(regexp)) {
            this.items.add("regexp = \"" + regexp + "\"");
        }
        if (VStringUtil.stringHasValue(message)) {
            this.items.add("message = \"" + message + "\"");
        }
        if (groups != null && groups.length > 0) {
            this.items.add("groups = {" + String.join(", ", groups) + "}");
        }
        if (!this.items.isEmpty()) {
            return "@Pattern(" + String.join(", ", items.toArray(new String[0])) + ")";
        } else {
            return "@Pattern";
        }
    }
}
