package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:33
 * @version 3.0
 */
@Getter
public class ApiOperationDesc extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@ApiOperation";
    public static final String API_OPERATION = "io.swagger.annotations.ApiOperation";

    private final String value;
    @Setter
    private  String notes;
    @Setter
    private  List<String> tags = new ArrayList<>();
    @Setter
    private  Class<?> response;
    @Setter
    private  String httpMethod;
    @Setter
    private  boolean hidden;
    @Setter
    private  int code;

    public static ApiOperationDesc create(String value, String notes){
        return new ApiOperationDesc(value,notes);
    }

    public ApiOperationDesc(String value, String notes) {
        super();
        this.value = value;
        this.notes = notes;
        this.addImports(API_OPERATION);
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(value)) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        if (VStringUtil.isNotBlank(notes)) {
            this.items.add(VStringUtil.format("notes = \"{0}\"", this.notes));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

}
