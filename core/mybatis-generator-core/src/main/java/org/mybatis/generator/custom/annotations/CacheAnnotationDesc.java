package org.mybatis.generator.custom.annotations;

import org.mybatis.generator.api.dom.java.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.vgosoft.tool.core.VStringUtil.format;
import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * spring cache annotation
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-16 03:33
 * @version 3.0
 */
public class CacheAnnotationDesc {

    private List<String> cacheNames = new ArrayList<>();
    private String unless;
    private List<Parameter> parameters = new ArrayList<>();

    private String serviceKey;

    private String key;

    public CacheAnnotationDesc() {
    }

    public CacheAnnotationDesc(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public CacheAnnotationDesc(String unless, String serviceKey) {
        this.unless = unless;
        this.serviceKey = serviceKey;
    }

    public List<String> getCacheNames() {
        return cacheNames;
    }

    public void addCacheNames(String cacheNames) {
        this.cacheNames.add(cacheNames);
    }

    public String getUnless() {
        return unless;
    }

    public void setUnless(String unless) {
        this.unless = unless;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setCacheNames(List<String> cacheNames) {
        this.cacheNames = cacheNames;
    }

    public String toCacheableAnnotation() {
        StringBuilder sb = new StringBuilder("@Cacheable(");
        sb.append(formatCacheNames());
        if (unless != null) {
            sb.append(",unless = \"").append(unless).append("\"");
        }
        sb.append(",").append(formatKey());
        sb.append(")");
        return sb.toString();
    }

    private String formatCacheNames() {
        StringBuilder sb = new StringBuilder("cacheNames = {");
        if (cacheNames.isEmpty()) {
            sb.append("\"").append(serviceKey).append("#8h\"");
        } else {
            String names = cacheNames.stream().map(c -> "\"" + c + "#72h\"").collect(Collectors.joining(","));
            sb.append(names);
        }
        sb.append("}");
        return sb.toString();
    }

    public String toCacheEvictAnnotation(boolean allEntries) {
        StringBuilder sb = new StringBuilder("@CacheEvict(");
        sb.append(formatCacheNames());
        if (allEntries) {
            sb.append(",allEntries = true");
        } else {
            sb.append(",").append(formatKey());
        }
        sb.append(")");
        return sb.toString();
    }

    private String formatKey() {
        if (parameters.isEmpty() && !stringHasValue(key)) {
            return "";
        }
        StringBuilder sb = new StringBuilder("key = \"'");
        sb.append(serviceKey==null? String.join("", cacheNames):serviceKey);
        sb.append(":'");
        if (stringHasValue(key)) {
            sb.append(format(".concat({0})", key));
        } else {
            for (int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                if (parameter.getType().getShortName().equals("String")) {
                    sb.append(format(".concat(#p{0}!=null?#p{0}:'''')", i));
                } else if (parameter.getType().getShortNameWithoutTypeArguments().equals("Optional")) {
                    sb.append(format(".concat(#p{0}!=null && #p{0}.isPresent()?#p{0}.get().toString():'''')", i));
                } else {
                    sb.append(format(".concat(#p{0}!=null?#p{0}.toString():'''')", i));
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
