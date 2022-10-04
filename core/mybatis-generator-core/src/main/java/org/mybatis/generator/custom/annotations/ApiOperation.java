package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:33
 * @version 3.0
 */
public class ApiOperation extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@ApiOperation";

    private final String value;
    private  String notes;
    private  List<String> tags = new ArrayList<>();
    private  Class<?> response;
    private  String httpMethod;
    private  boolean hidden;
    private  int code;

    public ApiOperation(String value,String notes) {
        super();
        this.value = value;
        this.notes = notes;
        this.addImports("io.swagger.annotations.ApiOperation");
    }

    @Override
    public List<String> toAnnotations() {
        return Collections.singletonList(toAnnotation());
    }

    @Override
    public String toAnnotation() {
        StringBuilder sb = new StringBuilder();
        if (VStringUtil.isNotBlank(value)) {
            sb.append("value = \"").append(value).append("\"");
        }
        if (VStringUtil.isNotBlank(notes)) {
            if (sb.length()>0)  sb.append(",");
            sb.append("notes = \"").append(notes).append("\"");
        }
        return ANNOTATION_NAME+"("+sb+")";
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
