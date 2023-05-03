package org.mybatis.generator.custom.annotations.validate;

import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-05-03 04:49
 * @version 3.0
 */
public class NotNull extends AbstractAnnotation {

    private String message;
    private String[] groups;

    public static NotNull create(String message, String[] groups){
        return new NotNull(message,groups);
    }

    public NotNull() {
        super();
        this.addImports("javax.validation.constraints.NotNull");
    }

    public NotNull(String message, String[] groups) {
        super();
        this.message = message;
        this.groups = groups;
        this.addImports("javax.validation.constraints.NotNull");
    }

    @Override
    public String toAnnotation() {
        if (message!=null && !message.equalsIgnoreCase("")) {
            this.items.add("message = \""+message+"\"");
        }
        if (groups!=null && groups.length>0) {
            this.items.add("groups = {"+String.join(", ",groups)+"}");
        }
        if (this.items.size()>0) return "@NotNull("+ String.join(", ",items.toArray(new String[0])) +")";
        else return "@NotNull";
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
