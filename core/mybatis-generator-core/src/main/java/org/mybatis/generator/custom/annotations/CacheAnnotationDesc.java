package org.mybatis.generator.custom.annotations;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * spring cache annotation
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-16 03:33
 * @version 3.0
 */
@Setter
@Getter
public class CacheAnnotationDesc {

    private static final String JSON_UTIL = "T(com.vgosoft.tool.core.JsonUtil)";
    private static final Pattern CACHE_TTL = Pattern.compile("[^#]+#[1-9][0-9]*[smhd]");

    private List<String> cacheNames = new ArrayList<>();
    private String unless;
    private String condition;
    private List<Parameter> parameters = new ArrayList<>();

    private String serviceKey;
    private String key;
    private String otherKey;

    public CacheAnnotationDesc() {
    }

    public CacheAnnotationDesc(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public CacheAnnotationDesc(String unless, String serviceKey) {
        this.unless = unless;
        this.serviceKey = serviceKey;
    }

    public void addCacheNames(String cacheNames) {
        this.cacheNames.add(cacheNames);
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
        List<String> names;
        if (cacheNames == null || cacheNames.isEmpty()) {
            if (!stringHasValue(serviceKey)) {
                throw new IllegalStateException("A cache name or service key is required");
            }
            names = List.of(formatName(serviceKey, "8h"));
        } else {
            names = cacheNames.stream()
                    .map(name -> formatName(name, "72h"))
                    .distinct()
                    .collect(Collectors.toList());
        }
        return "cacheNames = {" + names.stream()
                .map(CacheAnnotationDesc::javaStringLiteral)
                .collect(Collectors.joining(",")) + "}";
    }

    private String formatName(String name, String defaultTtl) {
        if (!stringHasValue(name)) {
            throw new IllegalArgumentException("Cache names must not be blank");
        }
        String normalized = name.trim();
        int separator = normalized.lastIndexOf('#');
        if (separator < 0) {
            return normalized + "#" + defaultTtl;
        }
        if (separator == 0 || !CACHE_TTL.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid cache name/TTL '" + name
                    + "'; expected name#<positive integer><s|m|h|d> with a single #");
        }
        String amount = normalized.substring(separator + 1, normalized.length() - 1);
        if (amount.length() > 10 || (amount.length() == 10
                && amount.compareTo(Integer.toString(Integer.MAX_VALUE)) > 0)) {
            throw new IllegalArgumentException("Invalid cache name/TTL '" + name + "'; TTL exceeds Integer.MAX_VALUE");
        }
        return normalized;
    }

    public String toCacheEvictAnnotation(boolean allEntries) {
        StringBuilder sb = new StringBuilder("@CacheEvict(");
        sb.append(formatCacheNames());
        if (allEntries) {
            sb.append(",allEntries = true");
        } else {
            sb.append(",").append(formatKey());
        }
        if (stringHasValue(condition)) {
            sb.append(",condition = ").append(javaStringLiteral(condition));
        }
        sb.append(")");
        return sb.toString();
    }

    private String formatKey() {
        if (parameters.isEmpty() && !stringHasValue(key) && !stringHasValue(serviceKey)
                && !stringHasValue(otherKey) && (cacheNames == null || cacheNames.isEmpty())) {
            return "";
        }

        List<String> components = new ArrayList<>();
        components.add("'vgosoft-cache-key-v2'");
        String namespace = stringHasValue(serviceKey) ? serviceKey : "";
        components.add(spelStringLiteral(namespace));
        components.add(spelStringLiteral(stringHasValue(otherKey) ? otherKey : ""));
        components.add("T(com.vgosoft.core.util.VCurrentUserUtil).getCurrentTenantId()");

        if (stringHasValue(key)) {
            components.add("'custom'");
            components.add(JSON_UTIL + ".toJsonString((" + key + "))");
        } else {
            for (int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                components.add(spelStringLiteral(parameter.getType().getFullyQualifiedName()));
                String value = "#p" + i;
                if ("Optional".equals(parameter.getType().getShortNameWithoutTypeArguments())) {
                    components.add("(" + value + " == null ? 'null' : (" + value
                            + ".isPresent() ? 'present:' + " + JSON_UTIL + ".toJsonString(" + value
                            + ".get()) : 'empty'))");
                } else {
                    components.add(JSON_UTIL + ".toJsonString(" + value + ")");
                }
            }
        }
        String expression = JSON_UTIL + ".toJsonString(T(java.util.Arrays).asList("
                + String.join(",", components) + "))";
        return "key = " + javaStringLiteral(expression);
    }

    private static String javaStringLiteral(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t") + "\"";
    }

    private static String spelStringLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
    }
}
