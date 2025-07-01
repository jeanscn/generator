package org.mybatis.generator.custom.annotations.validate;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

@Setter
@Getter
public class Max  extends AbstractAnnotation {
    private long value = Long.MAX_VALUE;
    private String message;
    private String[] groups;

    public static Max create(long value, String message, String[] groups) {
        return new Max(value, message, groups);
    }

    public Max() {
        super();
        this.addImports("javax.validation.constraints.Max");
    }

    public Max(long value, String message, String[] groups) {
        super();
        this.value = value;
        this.message = message;
        this.groups = groups;
        this.addImports("javax.validation.constraints.Max");
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
        if (!this.items.isEmpty()) return "@Max(" + String.join(", ", items.toArray(new String[0])) + ")";
        else return "@Max";
    }
}
