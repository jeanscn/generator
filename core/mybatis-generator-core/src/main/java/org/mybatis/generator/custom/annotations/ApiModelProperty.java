package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-20 14:17
 * @version 3.0
 */
public class ApiModelProperty extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@ApiModelProperty";
    public static final  String importClass = "io.swagger.annotations.ApiModelProperty";

    private final String value;

    private String example;

    private String hidden;

    private String required;


    public static ApiModelProperty create(String value){
        return new ApiModelProperty(value);
    }

    public ApiModelProperty(String value) {
        super();
        this.value = value;
        this.addImports(importClass);
    }

    public ApiModelProperty(String value, String example) {
        this.value = value;
        this.example = example;
        this.addImports(importClass);
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(value)) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        if (VStringUtil.isNotBlank(hidden)) {
            this.items.add(VStringUtil.format("hidden = {0}", this.hidden));
        }
        if (VStringUtil.isNotBlank(example)) {
            this.items.add(VStringUtil.format("example = \"{0}\"", this.example));
        }
        if (VStringUtil.isNotBlank(required)) {
            this.items.add(VStringUtil.format("required = {0}", this.required));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

    public String getValue() {
        return value;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getHidden() {
        return hidden;
    }

    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }
}
