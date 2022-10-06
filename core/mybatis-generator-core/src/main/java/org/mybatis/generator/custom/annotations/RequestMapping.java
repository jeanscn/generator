package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.constant.enums.RequestMethod;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 11:11
 * @version 3.0
 */
public class RequestMapping extends AbstractAnnotation {

    private String name;
    private final List<String> value = new ArrayList<>();
    private final List<RequestMethod> method = new ArrayList<>();
    private final List<String> params = new ArrayList<>();
    private List<String> headers = new ArrayList<>();
    private final List<String> consumes = new ArrayList<>();
    private final List<String> produces = new ArrayList<>();

    public static RequestMapping create(String value, RequestMethod method){
        return new RequestMapping(value,method);
    }

    public RequestMapping(String value, RequestMethod method) {
        super();
        this.addValue(value);
        this.addMethod(method);
    }

    @Override
    public List<String> toAnnotations() {
        return this.method.stream().map(requestMethod -> {
            StringBuilder sb = new StringBuilder();
            if (requestMethod.equals(RequestMethod.GET)) {
                sb.append("GetMapping(");
                this.addImports("org.springframework.web.bind.annotation.GetMapping");
            } else if (requestMethod.equals(RequestMethod.DELETE)) {
                sb.append("DeleteMapping(");
                this.addImports("org.springframework.web.bind.annotation.DeleteMapping");
            } else if (requestMethod.equals(RequestMethod.PATCH)) {
                sb.append("PatchMapping(");
                this.addImports("org.springframework.web.bind.annotation.PatchMapping");
            } else if (requestMethod.equals(RequestMethod.POST)) {
                sb.append("PostMapping(");
                this.addImports("org.springframework.web.bind.annotation.PostMapping");
            } else if (requestMethod.equals(RequestMethod.PUT)) {
                sb.append("PutMapping(");
                this.addImports("org.springframework.web.bind.annotation.PutMapping");
            } else {
                sb.append("RequestMapping(");
                this.addImports("org.springframework.web.bind.annotation.RequestMapping");
            }
            String nameStr = null;
            if (this.getName() != null) {
                nameStr = "name = \"" + this.getName() + "\"";
            }
            String collect = Stream.of(nameStr,
                            array2String(this.getValue(), "value",true),
                            array2String(this.getParams(), "params",true),
                            array2String(this.getHeaders(), "headers",true),
                            array2String(this.getConsumes(), "consumes",false),
                            array2String(this.getProduces(), "produces",false))
                    .filter(VStringUtil::isNotBlank)
                    .collect(Collectors.joining(", "));

            return "@" + sb + collect + ")";
        }).collect(Collectors.toList());
    }

    private String array2String(List<String> list, String key, boolean character) {
        StringBuilder sb = new StringBuilder();
        if (list.size() > 0) {
            sb.append(key).append(" = {");
            if (character) {
                sb.append(list.stream()
                        .map(v -> "\"" + v + "\"")
                        .collect(Collectors.joining(",")));
            } else {
                sb.append(String.join(",", list));
            }
            sb.append("}");
        }
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValue() {
        return value;
    }

    public void addValue(String value) {
        this.value.add(value);
    }

    public List<RequestMethod> getMethod() {
        return method;
    }

    public void addMethod(RequestMethod method) {
        this.method.add(method);
    }

    public List<String> getParams() {
        return params;
    }

    public void addParams(String param) {
        this.params.add(param);
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public void addConsumes(String consumes) {
        this.consumes.add(consumes);
    }

    public List<String> getProduces() {
        return produces;
    }

    public void addProduces(String produces) {
        this.produces.add(produces);
    }
}
