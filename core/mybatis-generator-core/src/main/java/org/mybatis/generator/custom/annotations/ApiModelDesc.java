package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-20 15:27
 * @version 3.0
 */
public class ApiModelDesc extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@ApiModel";
    public static final  String importClass = "io.swagger.annotations.ApiModel";

    private final String value;

    private String description;

    private String parent;


    public static ApiModelDesc create(String value){
        return new ApiModelDesc(value);
    }

    public ApiModelDesc(String value) {
        super();
        this.value = value;
        this.addImports(importClass);
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(value)) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        if (VStringUtil.isNotBlank(description)) {
            this.items.add(VStringUtil.format("description = \"{0}\"", this.description));
        }
        if (VStringUtil.isNotBlank(parent)) {
            this.items.add(VStringUtil.format("parent = {0}", this.parent));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}
