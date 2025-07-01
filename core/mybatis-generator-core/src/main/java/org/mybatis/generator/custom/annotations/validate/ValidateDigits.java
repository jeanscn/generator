package org.mybatis.generator.custom.annotations.validate;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-05-03 18:56
 * @version 3.0
 */

@Setter
@Getter
public class ValidateDigits extends AbstractAnnotation {

    private String integer;
    private String fraction;
    private String message;
    private String[] groups;

    public static ValidateDigits create(String integer, String fraction, String message, String[] groups){
        return new ValidateDigits(integer,fraction,message,groups);
    }
    public ValidateDigits() {
        super();
        this.addImports("javax.validation.constraints.Digits");
    }

    public ValidateDigits(String integer, String fraction, String message, String[] groups) {
        super();
        this.integer = integer;
        this.fraction = fraction;
        this.message = message;
        this.groups = groups;
        this.addImports("javax.validation.constraints.Digits");
    }

    @Override
    public String toAnnotation() {
        if (integer!=null && !integer.equalsIgnoreCase("")) {
            this.items.add("integer = "+integer);
        }
        if (fraction!=null && !fraction.equalsIgnoreCase("")) {
            this.items.add("fraction = "+fraction);
        }
        if (message!=null && !message.equalsIgnoreCase("")) {
            this.items.add("message = \""+message+"\"");
        }
        if (groups!=null && groups.length>0) {
            this.items.add("groups = {"+String.join(", ",groups)+"}");
        }
        if (!this.items.isEmpty()) return "@Digits("+ String.join(", ",items.toArray(new String[0])) +")";
        else return "@Digits";
    }

}
