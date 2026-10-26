package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.EnableCacheConfiguration;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * @author vgosoft-agent
 */
public final class CacheConfigurationResolver {

    private CacheConfigurationResolver() {
    }

    public static String sqlMutationCondition(TableConfiguration table) {
        String tableName = table.getTableName();
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalStateException("A SQL mutation cache condition requires a table name");
        }
        String condition = "#p0.table == null || #p0.table.trim().equalsIgnoreCase("
                + spelLiteral(tableName.trim()) + ")";
        String alias = table.getAlias();
        if (alias != null && !alias.isBlank()) {
            condition += " || #p0.table.trim().equalsIgnoreCase("
                    + spelLiteral(tableName.trim() + " as " + alias.trim()) + ")";
        }
        return condition;
    }

    private static String spelLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    public static List<String> queryCacheNames(SelectByTableGeneratorConfiguration configuration) {
        Set<String> names = new LinkedHashSet<>();
        for (List<String> group : queryCacheNameGroups(configuration)) {
            names.addAll(group);
        }
        return new ArrayList<>(names);
    }

    public static List<List<String>> queryCacheNameGroups(SelectByTableGeneratorConfiguration configuration) {
        List<List<String>> groups = new ArrayList<>();
        List<EnableCacheConfiguration> caches = configuration.getCacheConfigurationList();
        if (caches == null) {
            return groups;
        }
        for (EnableCacheConfiguration cache : caches) {
            if (!cache.isEnableCache()) {
                continue;
            }
            List<String> configuredNames = cache.getCacheNames();
            if (configuredNames == null || configuredNames.isEmpty()) {
                groups.add(List.of(defaultQueryCacheName(configuration)));
            } else {
                groups.add(new ArrayList<>(new LinkedHashSet<>(configuredNames)));
            }
        }
        return groups;
    }

    public static List<String> relationTableCacheNames(Context context, String relationTableName) {
        Set<String> names = new LinkedHashSet<>();
        for (TableConfiguration table : context.getTableConfigurations()) {
            for (SelectByTableGeneratorConfiguration configuration : table.getSelectByTableGeneratorConfiguration()) {
                if (sameTable(configuration.getTableName(), relationTableName)) {
                    names.addAll(queryCacheNames(configuration));
                }
            }
        }
        return new ArrayList<>(names);
    }

    public static List<String> mutationCacheNames(Context context, TableConfiguration table, String entityCacheName,
                                           boolean includeEntityCache) {
        Set<String> names = new LinkedHashSet<>();
        if (includeEntityCache) {
            names.add(entityCacheName + "#8h");
        }

        for (TableConfiguration owner : context.getTableConfigurations()) {
            boolean sameEntity = owner.equals(table);
            boolean mappedDependency = dependsOnResultMap(context, owner, table, new HashSet<>());
            if (sameEntity || mappedDependency) {
                for (SelectByTableGeneratorConfiguration configuration : owner.getSelectByTableGeneratorConfiguration()) {
                    if (sameEntity || !configuration.isReturnPrimaryKey()) {
                        names.addAll(queryCacheNames(configuration));
                    }
                }
                if (mappedDependency && owner.getVoCacheGeneratorConfiguration() != null
                        && owner.getVoCacheGeneratorConfiguration().isGenerate()
                        && owner.getSelectByColumnGeneratorConfigurations().stream()
                        .anyMatch(configuration -> !configuration.isReturnPrimaryKey())) {
                    names.add(owner.getDomainObjectName() + "#8h");
                }
            }
        }
        names.addAll(relationTableCacheNames(context, table.getTableName()));
        return new ArrayList<>(names);
    }

    private static boolean dependsOnResultMap(Context context, TableConfiguration owner,
                                              TableConfiguration changed, Set<TableConfiguration> visited) {
        if (!visited.add(owner)) {
            return false;
        }
        // BLOB queries use ResultMapWithBLOBs instead of the relation ResultMap.
        if (context.getIntrospectedTables().stream()
                .anyMatch(introspected -> owner.equals(introspected.getTableConfiguration())
                        && introspected.getRules().generateResultMapWithBlobs())) {
            return false;
        }
        for (RelationGeneratorConfiguration relation : owner.getRelationGeneratorConfigurations()) {
            if (!relation.isSubSelected()) {
                continue;
            }
            for (TableConfiguration target : context.getTableConfigurations()) {
                if (referencesMapper(relation.getSelect(), target)
                        && (target.equals(changed)
                        || usesChangedRelationTable(target, relation.getSelect(), changed.getTableName())
                        || dependsOnResultMap(context, target, changed, visited))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean referencesMapper(String select, TableConfiguration target) {
        if (target.getJavaClientGeneratorConfiguration() == null || target.getDomainObjectName() == null) {
            return false;
        }
        String mapper = target.getJavaClientGeneratorConfiguration().getTargetPackage()
                + "." + target.getDomainObjectName() + "Mapper.";
        return select.startsWith(mapper);
    }

    private static boolean usesChangedRelationTable(TableConfiguration target, String select, String changedTable) {
        return target.getSelectByTableGeneratorConfiguration().stream()
                .anyMatch(query -> select.endsWith("." + query.getMethodName())
                        && sameTable(query.getTableName(), changedTable));
    }

    static boolean hasEnabledQueryCache(TableConfiguration table) {
        return table.getSelectByTableGeneratorConfiguration().stream()
                .anyMatch(configuration -> !queryCacheNames(configuration).isEmpty());
    }

    static String defaultQueryCacheName(SelectByTableGeneratorConfiguration configuration) {
        if (configuration.getRelationIdKey() != null && !configuration.getRelationIdKey().isBlank()) {
            return configuration.getRelationIdKey();
        }
        if (configuration.getParameterName() == null || configuration.getThisColumn() == null
                || configuration.getThisColumn().getJavaProperty() == null) {
            throw new IllegalStateException("A selectByTable cache needs parameterName and thisColumn");
        }
        return configuration.getParameterName()
                + JavaBeansUtil.getFirstCharacterUppercase(configuration.getThisColumn().getJavaProperty())
                + "sCache";
    }

    private static boolean sameTable(String first, String second) {
        return first != null && second != null && first.trim().equalsIgnoreCase(second.trim());
    }
}
