package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * @author cen_c
 */
public final class ManagedRelationGeneration {
    private ManagedRelationGeneration() {
    }

    public static void validate(AbstractIntrospectedTable table, SelectByTableGeneratorConfiguration relation) {
        if (table.getPrimaryKeyColumns().size() != 1
                || !FullyQualifiedJavaType.getStringInstance().equals(table.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType())
                || relation.getThisColumn() == null || relation.getOtherColumn() == null
                || !FullyQualifiedJavaType.getStringInstance().equals(relation.getThisColumn().getFullyQualifiedJavaType())
                || !FullyQualifiedJavaType.getStringInstance().equals(relation.getOtherColumn().getFullyQualifiedJavaType())
                || !relation.getThisColumn().getActualColumnName().equals(relation.getPrimaryKeyColumn())
                || !relation.getOtherColumn().getActualColumnName().equals(relation.getOtherPrimaryKeyColumn())
                || relation.getPrimaryKeyColumn().equals(relation.getOtherPrimaryKeyColumn())) {
            throw new IllegalArgumentException("Managed relation requires a single String primary key and distinct String key columns: " + relation.getRelationIdKey());
        }
        if (!relation.getRelationIdKey().matches("[A-Za-z0-9_.:-]+")) {
            throw new IllegalArgumentException("Managed relationIdKey must be a stable identifier: " + relation.getRelationIdKey());
        }
    }

    public static List<String> queryCacheNames(SelectByTableGeneratorConfiguration relation) {
        return CacheConfigurationResolver.queryCacheNames(relation).stream()
                .map(CacheAnnotationDesc::normalizeRelationCacheName).distinct().toList();
    }

    public static String cacheNames(SelectByTableGeneratorConfiguration relation) {
        return cacheNames(new LinkedHashSet<>(queryCacheNames(relation)));
    }

    private static String cacheNames(Set<String> names) {
        return names.stream().map(name -> "\"" + name.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(", "));
    }

    public static void addMutation(Method method, TopLevelClass parent, SelectByTableGeneratorConfiguration relation) {
        parent.addImportedType("com.vgosoft.core.annotation.relation.RelationMutation");
        method.addAnnotation("@RelationMutation(relation = \"" + relation.getRelationIdKey()
                + "\", cacheNames = {" + cacheNames(relation) + "})");
    }

    public static void injectWriteSupport(TopLevelClass parent) {
        if (parent.getFields().stream().anyMatch(field -> field.getName().equals("relationWriteSupport"))) {
            return;
        }
        FullyQualifiedJavaType type = new FullyQualifiedJavaType("com.vgosoft.mybatis.relation.RelationWriteSupport");
        Field field = new Field("relationWriteSupport", type);
        field.setVisibility(JavaVisibility.PRIVATE);
        field.addAnnotation("@Autowired");
        parent.addField(field);
        parent.addImportedType(type);
        parent.addImportedType("org.springframework.beans.factory.annotation.Autowired");
    }

    public static void addEntityMutationAnnotations(TopLevelClass parent, AbstractIntrospectedTable table) {
        List<SelectByTableGeneratorConfiguration> managed = table.getTableConfiguration()
                .getSelectByTableGeneratorConfiguration().stream()
                .filter(SelectByTableGeneratorConfiguration::isManagedRelation).toList();
        if (managed.isEmpty()) {
            return;
        }
        Map<String, Set<String>> relations = new LinkedHashMap<>();
        for (SelectByTableGeneratorConfiguration relation : managed) {
            validate(table, relation);
            relations.computeIfAbsent(relation.getRelationIdKey(), key -> new LinkedHashSet<>())
                    .addAll(queryCacheNames(relation));
        }
        if (relations.values().stream().allMatch(Set::isEmpty)) {
            return;
        }
        for (Method method : parent.getMethods()) {
            if (method.getAnnotations().stream().noneMatch(annotation -> annotation.startsWith("@CacheEvict"))) {
                continue;
            }
            boolean relationshipWrite = method.getName().startsWith("insertByTable")
                    || method.getName().startsWith("deleteByTable");
            if (relationshipWrite) {
                continue;
            }
            for (int i = 0; i < method.getAnnotations().size(); i++) {
                String annotation = method.getAnnotations().get(i);
                if (!annotation.startsWith("@CacheEvict")) {
                    continue;
                }
                var match = Pattern.compile("cacheNames\\s*=\\s*\\{[^}]*}").matcher(annotation);
                if (!match.find()) {
                    throw new IllegalArgumentException("Managed entity mutation requires explicit CacheEvict cacheNames: " + method.getName());
                }
                Set<String> names = new LinkedHashSet<>();
                var literals = Pattern.compile("\"([^\"]+)\"").matcher(match.group());
                while (literals.find()) {
                    names.add(literals.group(1));
                }
                names.addAll(CacheConfigurationResolver.mutationCacheNames(table.getContext(),
                        table.getTableConfiguration(), table.getTableConfiguration().getDomainObjectName(), false));
                String cacheNames = names.stream().map(CacheAnnotationDesc::normalizeRelationCacheName)
                        .distinct().map(name -> "\"" + name + "\"").collect(Collectors.joining(","));
                method.getAnnotations().set(i, match.replaceFirst(java.util.regex.Matcher.quoteReplacement(
                        "cacheNames = {" + cacheNames + "}")));
            }
            method.getAnnotations().removeIf(annotation -> annotation.startsWith("@RelationMutation"));
            parent.addImportedType("com.vgosoft.core.annotation.relation.RelationMutation");
            List<String> mutations = relations.entrySet().stream()
                    .map(entry -> "@RelationMutation(relation = \"" + entry.getKey()
                            + "\", cacheNames = {" + cacheNames(entry.getValue()) + "})")
                    .toList();
            if (mutations.size() == 1) {
                method.addAnnotation(mutations.get(0));
            } else {
                parent.addImportedType("com.vgosoft.core.annotation.relation.RelationMutations");
                method.addAnnotation("@RelationMutations({" + String.join(", ", mutations) + "})");
            }
        }
    }
}
