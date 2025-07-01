package org.mybatis.generator.custom.annotations.validate;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

@Setter
@Getter
public class Min  extends AbstractAnnotation {
    private long value = Long.MAX_VALUE;
    private String message;
    private String[] groups;

    public static Min create(long value, String message, String[] groups) {
        return new Min(value, message, groups);
    }

    public Min() {
        super();
        this.addImports("javax.validation.constraints.Min");
    }

    public Min(long value, String message, String[] groups) {
        super();
        this.value = value;
        this.message = message;
        this.groups = groups;
        this.addImports("javax.validation.constraints.Min");
    }

    @Override
    public String toAnnotation() {
        if (value != Long.MAX_VALUE) {
            this.items.add("value = " + value);
        }
        if (message != null && !message.isEmpty()) {
            this.items.add("message = \"" + message + "\"");
        }
        if (groups != null && groups.length > 0) {
            this.items.add("groups = {" + String.join(", ", groups) + "}");
        }
        if (!this.items.isEmpty()) return "@Min(" + String.join(", ", items.toArray(new String[0])) + ")";
        else return "@Min";
    }
}
