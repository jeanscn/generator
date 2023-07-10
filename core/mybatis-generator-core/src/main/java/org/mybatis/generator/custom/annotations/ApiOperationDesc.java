package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:33
 * @version 3.0
 */
public class ApiOperationDesc extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@ApiOperation";
    public static final String importClass = "io.swagger.annotations.ApiOperation";

    private final String value;
    private  String notes;
    private  List<String> tags = new ArrayList<>();
    private  Class<?> response;
    private  String httpMethod;
    private  boolean hidden;
    private  int code;

    public static ApiOperationDesc create(String value, String notes){
        return new ApiOperationDesc(value,notes);
    }

    public ApiOperationDesc(String value, String notes) {
        super();
        this.value = value;
        this.notes = notes;
        this.addImports(importClass);
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

    public String getValue() {
        return value;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Class<?> getResponse() {
        return response;
    }

    public void setResponse(Class<?> response) {
        this.response = response;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
