package org.mybatis.generator.custom.annotations.validate;

import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-05-03 02:59
 * @version 3.0
 */
public class ValidateSize extends AbstractAnnotation {

    private String min;
    private String max;
    private String message;
    private String[] groups;

    public static ValidateSize create(String min, String max, String message, String[] groups){
        return new ValidateSize(min,max,message,groups);
    }
    public ValidateSize() {
        super();
        this.addImports("javax.validation.constraints.Size");
    }

    public ValidateSize(String min, String max, String message, String[] groups) {
        super();
        this.min = min;
        this.max = max;
        this.message = message;
        this.groups = groups;
        this.addImports("javax.validation.constraints.Size");
    }

    @Override
    public String toAnnotation() {
        if (min!=null && !min.equalsIgnoreCase("") && !min.equalsIgnoreCase("0")) {
            this.items.add("min = "+min);
        }
        if (max!=null && !max.equalsIgnoreCase("") && !max.equalsIgnoreCase("2147483647")) {
            this.items.add("max = "+max);
        }
        if (message!=null && !message.equalsIgnoreCase("")) {
            this.items.add("message = \""+message+"\"");
        }
        if (groups!=null && groups.length>0) {
            this.items.add("groups = {"+String.join(", ",groups)+"}");
        }
        if (!this.items.isEmpty()) return "@Size("+ String.join(", ",items.toArray(new String[0])) +")";
        else return "@Size";
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getGroups() {
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }
}
