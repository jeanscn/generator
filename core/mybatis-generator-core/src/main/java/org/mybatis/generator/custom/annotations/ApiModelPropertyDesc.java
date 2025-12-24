package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-20 14:17
 * @version 3.0
 */
@Getter
@Setter
public class ApiModelPropertyDesc extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@ApiModelProperty";
    public static final  String API_MODEL_PROPERTY = "io.swagger.annotations.ApiModelProperty";

    private final String value;

    private String example;

    private String hidden;

    private String required;


    public static ApiModelPropertyDesc create(String value){
        return new ApiModelPropertyDesc(value);
    }

    public ApiModelPropertyDesc(String value) {
        super();
        this.value = value;
        this.addImports(API_MODEL_PROPERTY);
    }

    public ApiModelPropertyDesc(String value, String example) {
        this.value = value;
        this.example = example;
        this.addImports(API_MODEL_PROPERTY);
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

}
