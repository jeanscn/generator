package org.mybatis.generator.custom.annotations;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

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

    private static final String KEY_JSON_METHOD = "vgosoftCacheKeyJson";
    private static final String KEY_JSON_MAPPER = "VGOSOFT_CACHE_KEY_MAPPER";
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

    public String toCacheableAnnotation(TopLevelClass parentElement) {
        addKeySerializationSupport(parentElement);
        return toCacheableAnnotation();
    }

    private static void addKeySerializationSupport(TopLevelClass parentElement) {
        if (parentElement.getFields().stream().anyMatch(field -> KEY_JSON_MAPPER.equals(field.getName()))) {
            return;
        }
        // Emit the serializer into the application: generator classes are not runtime dependencies.
        FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType("tools.jackson.databind.json.JsonMapper");
        Field mapper = new Field(KEY_JSON_MAPPER, mapperType);
        mapper.setVisibility(JavaVisibility.PRIVATE);
        mapper.setStatic(true);
        mapper.setFinal(true);
        mapper.setInitializationString("JsonMapper.builder()"
                + ".disable(SerializationFeature.INDENT_OUTPUT)"
                + ".enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)"
                + ".disable(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST)"
                + ".enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY).build()");
        parentElement.addField(mapper);
        parentElement.addImportedType(mapperType);
        parentElement.addImportedType("tools.jackson.databind.SerializationFeature");
        parentElement.addImportedType("tools.jackson.databind.MapperFeature");

        Method serialize = new Method(KEY_JSON_METHOD);
        serialize.setVisibility(JavaVisibility.PUBLIC);
        serialize.setFinal(true);
        serialize.setReturnType(FullyQualifiedJavaType.getStringInstance());
        serialize.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value"));
        serialize.addBodyLine("return " + KEY_JSON_MAPPER + ".writeValueAsString(value);");
        parentElement.addMethod(serialize);
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

    public String toCacheEvictAnnotation(boolean allEntries, TopLevelClass parentElement) {
        if (!allEntries) {
            addKeySerializationSupport(parentElement);
        }
        return toCacheEvictAnnotation(allEntries);
    }

    private String formatKey() {
        if (parameters.isEmpty() && !stringHasValue(key) && !stringHasValue(serviceKey)
                && !stringHasValue(otherKey) && (cacheNames == null || cacheNames.isEmpty())) {
            return "";
        }

        List<String> components = new ArrayList<>();
        components.add("'vgosoft-cache-key-v3'");
        String namespace = stringHasValue(serviceKey) ? serviceKey : "";
        components.add(spelStringLiteral(namespace));
        components.add(spelStringLiteral(stringHasValue(otherKey) ? otherKey : ""));
        components.add("T(com.vgosoft.core.util.VCurrentUserUtil).getCurrentTenantId()");

        if (stringHasValue(key)) {
            components.add("{'custom',(" + key + ")}");
        } else {
            for (int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                String type = spelStringLiteral(parameter.getType().getFullyQualifiedName());
                String value = "#p" + i;
                if ("Optional".equals(parameter.getType().getShortNameWithoutTypeArguments())) {
                    components.add("{" + type + ",(" + value + " == null ? 'null' : ("
                            + value + ".isPresent() ? 'present' : 'empty')),("
                            + value + " != null && " + value + ".isPresent() ? " + value + ".get() : null)}");
                } else {
                    components.add("{" + type + "," + value + "}");
                }
            }
        }
        String expression = "#root.target." + KEY_JSON_METHOD + "({"
                + String.join(",", components) + "})";
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
