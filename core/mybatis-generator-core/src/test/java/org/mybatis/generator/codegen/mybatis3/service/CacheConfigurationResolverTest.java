package org.mybatis.generator.codegen.mybatis3.service;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.EnableCacheConfiguration;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;

import java.util.ArrayList;
import java.util.List;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigurationResolverTest {

    @Test
    void shouldMatchSqlMutationTablesBeforeAndAfterDefaultAliasAssignment() {
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration user = table(context, "org_user");
        user.setAlias("usr");
        String condition = CacheConfigurationResolver.sqlMutationCondition(user);
        for (String actual : new String[]{null, "org_user", "org_user as usr", " ORG_USER AS USR "}) {
            assertThat(evaluateTableCondition(condition, actual)).as("table=%s", actual).isTrue();
        }
        for (String actual : List.of("org_team", "org_team as usr", "org_user as other", "org_user_extra")) {
            assertThat(evaluateTableCondition(condition, actual)).as("table=%s", actual).isFalse();
        }
        user.setAlias(null);
        assertThat(evaluateTableCondition(CacheConfigurationResolver.sqlMutationCondition(user), "org_user as usr"))
                .isFalse();
    }

    private Boolean evaluateTableCondition(String condition, String tableName) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("p0", new SqlTableParameter(tableName));
        return new SpelExpressionParser().parseExpression(condition)
                .getValue(context, Boolean.class);
    }

    public static class SqlTableParameter {
        private final String table;

        public SqlTableParameter(String table) {
            this.table = table;
        }

        public String getTable() {
            return table;
        }
    }

    @Test
    void shouldResolveOnlyEnabledActualCachesForARelationTable() {
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration role = table(context, "role");

        SelectByTableGeneratorConfiguration forward = query("userIds", "roleId", "user_role");
        forward.getCacheConfigurationList().add(cache(List.of("userRoleCache#24h"), true));
        role.getSelectByTableGeneratorConfiguration().add(forward);

        SelectByTableGeneratorConfiguration disabled = query("groupIds", "roleId", "user_role");
        disabled.getCacheConfigurationList().add(cache(List.of("disabledCache"), false));
        role.getSelectByTableGeneratorConfiguration().add(disabled);

        SelectByTableGeneratorConfiguration noReverseConfigured = query("permissionIds", "roleId", "user_role");
        noReverseConfigured.getCacheConfigurationList().add(cache(List.of(), true));
        role.getSelectByTableGeneratorConfiguration().add(noReverseConfigured);

        assertThat(CacheConfigurationResolver.relationTableCacheNames(context, "USER_ROLE"))
                .containsExactly("userRoleCache#24h", "permissionIdsRoleIdsCache");
    }

    @Test
    void shouldKeepEnabledCacheGroupsAvailableForNonRepeatableAnnotationGeneration() {
        SelectByTableGeneratorConfiguration query = query("roleIds", "userId", "user_role");
        query.getCacheConfigurationList().add(cache(List.of("primary", "secondary"), true));
        query.getCacheConfigurationList().add(cache(List.of("primary", "tertiary"), true));

        assertThat(CacheConfigurationResolver.queryCacheNameGroups(query))
                .containsExactly(List.of("primary", "secondary"), List.of("primary", "tertiary"));
    }

    @Test
    void shouldIncludeOwnPrimaryKeyOnlyQueriesWithoutCachePo() {
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration role = table(context, "org_role");
        SelectByTableGeneratorConfiguration ids = query("permissionId", "roleId", "org_r_role_permission");
        ids.setReturnTypeParam("primaryKey");
        ids.getCacheConfigurationList().add(cache(List.of("permissionIdRoleIdsCache#12h"), true));
        role.getSelectByTableGeneratorConfiguration().add(ids);

        assertThat(CacheConfigurationResolver.mutationCacheNames(context, role, "OrgRole", false))
                .containsExactly("permissionIdRoleIdsCache#12h");
    }

    @Test
    void shouldIncludeLocalFullEntityAndConfiguredDependentRelationCachesOnly() {
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration user = table(context, "user");
        SelectByTableGeneratorConfiguration userRelations = query("roleIds", "userId", "user_role");
        userRelations.getCacheConfigurationList().add(cache(List.of(), true));
        user.getSelectByTableGeneratorConfiguration().add(userRelations);

        TableConfiguration role = table(context, "role");
        SelectByTableGeneratorConfiguration dependsOnUser = query("userIds", "roleId", "user");
        dependsOnUser.getCacheConfigurationList().add(cache(List.of("rolesByUser"), true));
        role.getSelectByTableGeneratorConfiguration().add(dependsOnUser);

        SelectByTableGeneratorConfiguration unrelated = query("departmentIds", "roleId", "user_department");
        unrelated.getCacheConfigurationList().add(cache(List.of("unrelated"), true));
        role.getSelectByTableGeneratorConfiguration().add(unrelated);

        assertThat(CacheConfigurationResolver.mutationCacheNames(context, user, "User", false))
                .containsExactly("roleIdsUserIdsCache", "rolesByUser");
    }

    @Test
    void shouldEvictCachedEntityGraphsDependingOnTheChangedMapper() {
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration user = table(context, "user_");
        user.setDomainObjectName("User");
        JavaClientGeneratorConfiguration mapper = new JavaClientGeneratorConfiguration();
        mapper.setTargetPackage("sample.dao");
        user.setJavaClientGeneratorConfiguration(mapper);
        TableConfiguration department = table(context, "department_");
        SelectByTableGeneratorConfiguration query = query("teamId", "departmentId", "department_team");
        query.getCacheConfigurationList().add(cache(List.of("departmentsWithUsers"), true));
        department.getSelectByTableGeneratorConfiguration().add(query);

        RelationGeneratorConfiguration relation = new RelationGeneratorConfiguration();
        relation.setColumn("id_");
        relation.setSelect("sample.dao.UserMapper.selectByDepartmentId");
        department.getRelationGeneratorConfigurations().add(relation);

        assertThat(CacheConfigurationResolver.mutationCacheNames(context, user, "User", false))
                .containsExactly("departmentsWithUsers");
    }

    private TableConfiguration table(Context context, String name) {
        TableConfiguration table = new TableConfiguration(context, new ArrayList<>());
        table.setTableName(name);
        context.addTableConfiguration(table);
        return table;
    }

    private SelectByTableGeneratorConfiguration query(String parameterName, String thisColumn, String relationTable) {
        SelectByTableGeneratorConfiguration configuration = new SelectByTableGeneratorConfiguration();
        configuration.setParameterName(parameterName);
        configuration.setTableName(relationTable);
        IntrospectedColumn column = new IntrospectedColumn();
        column.setJavaProperty(thisColumn);
        configuration.setThisColumn(column);
        return configuration;
    }

    private EnableCacheConfiguration cache(List<String> names, boolean enabled) {
        EnableCacheConfiguration configuration = new EnableCacheConfiguration();
        configuration.setCacheNames(names);
        configuration.setEnableCache(enabled);
        return configuration;
    }
}
