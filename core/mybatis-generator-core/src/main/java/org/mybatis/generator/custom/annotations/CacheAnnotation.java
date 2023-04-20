package org.mybatis.generator.custom.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * spring cache annotation
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-16 03:33
 * @version 3.0
 */
public class CacheAnnotation {
    private List<String> cacheNames = new ArrayList<>();
    private String unless;
    private int parameters;
    private String serviceKey;

    public CacheAnnotation() {
    }

    public CacheAnnotation(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public CacheAnnotation(String unless, int parameters, String serviceKey) {
        this.unless = unless;
        this.parameters = parameters;
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

    public int getParameters() {
        return parameters;
    }

    public void setParameters(int parameters) {
        this.parameters = parameters;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String toCacheableAnnotation() {
        StringBuilder sb = new StringBuilder("@Cacheable(");
        sb.append(formatCacheNames());
        if (unless != null) {
            sb.append(",unless = \"").append(unless).append("\"");
        }
        sb.append(",key = \"'").append(serviceKey);
        if (parameters > 0) {
            sb.append(":'");
            for (int i = 0; i < parameters; i++) {
                String pi = "#p" + i;
                sb.append(".concat(");
                sb.append(pi);
                sb.append("!=null?").append(pi).append(":'')");
            }
            sb.append("\"");
        } else {
            sb.append("'\"");
        }
        sb.append(")");
        return sb.toString();
    }

    private String formatCacheNames() {
        StringBuilder sb = new StringBuilder("cacheNames = {");
        if (cacheNames.size() == 0) {
            sb.append("\"").append(serviceKey).append("#8h\"");
        } else {
            String names = cacheNames.stream().map(c -> "\"" + c + "\"").collect(Collectors.joining(","));
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
        }
        sb.append(")");
        return sb.toString();
    }
}
