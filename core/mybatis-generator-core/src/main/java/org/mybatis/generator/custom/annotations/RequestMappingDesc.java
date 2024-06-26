package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import com.vgosoft.tool.core.VStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 11:11
 * @version 3.0
 */
public class RequestMappingDesc extends AbstractAnnotation {

    private String name;
    private final List<String> value = new ArrayList<>();
    private final List<RequestMethodEnum> method = new ArrayList<>();
    private final List<String> params = new ArrayList<>();
    private List<String> headers = new ArrayList<>();
    private final List<String> consumes = new ArrayList<>();
    private final List<String> produces = new ArrayList<>();

    public static RequestMappingDesc create(String value, RequestMethodEnum method) {
        return new RequestMappingDesc(value, method);
    }

    public RequestMappingDesc(String value) {
        super();
        this.addValue(value);
    }

    public RequestMappingDesc(String value, RequestMethodEnum method) {
        super();
        this.addValue(value);
        this.addMethod(method);
    }

    @Override
    public List<String> toAnnotations() {
        if (!this.method.isEmpty()) {
            return this.method.stream().map(requestMethod -> {
                StringBuilder sb = new StringBuilder();
                if (requestMethod.equals(RequestMethodEnum.GET)) {
                    sb.append("GetMapping(");
                    this.addImports("org.springframework.web.bind.annotation.GetMapping");
                } else if (requestMethod.equals(RequestMethodEnum.DELETE)) {
                    sb.append("DeleteMapping(");
                    this.addImports("org.springframework.web.bind.annotation.DeleteMapping");
                } else if (requestMethod.equals(RequestMethodEnum.PATCH)) {
                    sb.append("PatchMapping(");
                    this.addImports("org.springframework.web.bind.annotation.PatchMapping");
                } else if (requestMethod.equals(RequestMethodEnum.POST)) {
                    sb.append("PostMapping(");
                    this.addImports("org.springframework.web.bind.annotation.PostMapping");
                } else if (requestMethod.equals(RequestMethodEnum.PUT)) {
                    sb.append("PutMapping(");
                    this.addImports("org.springframework.web.bind.annotation.PutMapping");
                } else {
                    sb.append("RequestMapping(");
                    this.addImports("org.springframework.web.bind.annotation.RequestMapping");
                }
                String nameStr = null;
                if (VStringUtil.isNotBlank(this.getName())) {
                    nameStr = "name = \"" + this.getName() + "\"";
                }
                String collect = Stream.of(nameStr,
                                array2String(this.getValue(), "value", true),
                                array2String(this.getParams(), "params", true),
                                array2String(this.getHeaders(), "headers", true),
                                array2String(this.getConsumes(), "consumes", false),
                                array2String(this.getProduces(), "produces", false))
                        .filter(VStringUtil::isNotBlank)
                        .collect(Collectors.joining(", "));

                return "@" + sb + collect + ")";
            }).collect(Collectors.toList());
        } else {
            this.addImports("org.springframework.web.bind.annotation.RequestMapping");
            StringBuilder sb = new StringBuilder();
            if (VStringUtil.isNotBlank(this.getName())) {
                sb.append("name = \"").append(this.getName()).append("\"");
            }
            String collect = Stream.of(sb.toString(),
                            array2String(this.getValue(), "value", true),
                            array2String(this.getParams(), "params", true),
                            array2String(this.getHeaders(), "headers", true),
                            array2String(this.getConsumes(), "consumes", false),
                            array2String(this.getProduces(), "produces", false))
                    .filter(VStringUtil::isNotBlank)
                    .collect(Collectors.joining(", "));
            return Collections.singletonList("@RequestMapping(" + collect + ")");
        }
    }

    private String array2String(final List<String> list, final String key, final boolean character) {
        String collect = list.stream().filter(VStringUtil::isNotBlank).map(s -> character ? "\"" + s + "\"" : s).collect(Collectors.joining(","));
        if (VStringUtil.isNotBlank(collect)) {
            return key + " = {" + collect + "}";
        }
        return null;
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

    public List<RequestMethodEnum> getMethod() {
        return method;
    }

    public void addMethod(RequestMethodEnum method) {
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
