package org.mybatis.generator.codegen.mybatis3.controller;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.codegen.mybatis3.controller.elements.DeleteByTableGenerator;
import org.mybatis.generator.codegen.mybatis3.controller.elements.InsertByTableGenerator;
import org.mybatis.generator.config.EnableCacheConfiguration;
import org.mybatis.generator.config.JavaControllerGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.rules.ConditionalModelRules;
import org.mybatis.generator.offline.ManagedRelationOfflineRegenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 操作审计日志注解（{@code @SystemLog}）生成范围的离线验证：
 * {@code enableSysLogAnnotation} 总开关（缺省 true）与 {@code apiPermissions}
 * 白名单命中共同决定注解生成，且独立于 Spring Security 开关。
 */
class SystemLogAnnotationGenerationTest {

    @Test
    void sysLogGeneratedForWhitelistedEndpointsByDefault() {
        IntrospectedTableMyBatis3Impl table = fixture("relation", null);
        TopLevelClass controller = controller(table);
        assertThat(controller.getMethods()).hasSize(2);
        for (Method method : controller.getMethods()) {
            assertThat(method.getAnnotations().toString()).contains("@SystemLog");
        }
    }

    @Test
    void sysLogSuppressedWhenSwitchDisabled() {
        IntrospectedTableMyBatis3Impl table = fixture("all", "false");
        TopLevelClass controller = controller(table);
        for (Method method : controller.getMethods()) {
            assertThat(method.getAnnotations().toString()).doesNotContain("@SystemLog");
        }
    }

    @Test
    void sysLogSuppressedWhenWhitelistUnset() {
        IntrospectedTableMyBatis3Impl table = fixture(null, null);
        TopLevelClass controller = controller(table);
        for (Method method : controller.getMethods()) {
            assertThat(method.getAnnotations().toString()).doesNotContain("@SystemLog");
        }
    }

    @Test
    void sysLogFollowsDynamicPermissionCode() {
        SelectByTableGeneratorConfiguration relation = relation();
        IntrospectedTableMyBatis3Impl table = fixture(relation.getUnionMethodName().toLowerCase(), null);
        TopLevelClass controller = controller(table);
        Method union = controller.getMethods().stream()
                .filter(method -> method.getName().equals(relation.getUnionMethodName())).findFirst().orElseThrow();
        Method split = controller.getMethods().stream()
                .filter(method -> method.getName().equals(relation.getSplitMethodName())).findFirst().orElseThrow();
        assertThat(union.getAnnotations().toString()).contains("@SystemLog");
        assertThat(split.getAnnotations().toString()).doesNotContain("@SystemLog");
    }

    @Test
    void sysLogIndependentOfSpringSecuritySwitch() {
        IntrospectedTableMyBatis3Impl table = fixture("all", null);
        table.getContext().setIntegrateSpringSecurity(false);
        table.setRules(new ConditionalModelRules(table));
        TopLevelClass controller = controller(table);
        for (Method method : controller.getMethods()) {
            assertThat(method.getAnnotations().toString()).contains("@SystemLog")
                    .doesNotContain("@PreAuthorize");
        }
    }

    @Test
    void sysLogMatchesWhitelistCaseInsensitively() {
        IntrospectedTableMyBatis3Impl table = fixture(" RELATION ", null);
        TopLevelClass controller = controller(table);
        for (Method method : controller.getMethods()) {
            assertThat(method.getAnnotations().toString()).contains("@SystemLog");
        }
    }

    private static IntrospectedTableMyBatis3Impl fixture(String apiPermissions, String enableSysLogAnnotation) {
        IntrospectedTableMyBatis3Impl table =
                ManagedRelationOfflineRegenerator.fixture("system", "SysMenu", "sys_menu", relation());
        table.setRemarks("菜单管理");
        JavaControllerGeneratorConfiguration controllerConfiguration =
                new JavaControllerGeneratorConfiguration(table.getContext(), table.getTableConfiguration());
        if (apiPermissions != null) {
            controllerConfiguration.addProperty(PropertyRegistry.CONTROLLER_API_PERMISSIONS, apiPermissions);
        }
        if (enableSysLogAnnotation != null) {
            controllerConfiguration.addProperty(PropertyRegistry.CONTROLLER_ENABLE_SYSLOG_ANNOTATION,
                    enableSysLogAnnotation);
        }
        table.getTableConfiguration().setJavaControllerGeneratorConfiguration(controllerConfiguration);
        table.setRules(new ConditionalModelRules(table));
        return table;
    }

    private static TopLevelClass controller(IntrospectedTableMyBatis3Impl table) {
        TopLevelClass controller = new TopLevelClass("system.controller.AbstractSysMenuController");
        for (AbstractControllerElementGenerator generator : List.of(new InsertByTableGenerator(), new DeleteByTableGenerator())) {
            ManagedRelationOfflineRegenerator.configure(generator, table);
            generator.setCommentGenerator(table.getContext().getCommentGenerator());
            generator.setServiceBeanName("sysMenuImpl");
            generator.addElements(controller);
        }
        return controller;
    }

    private static SelectByTableGeneratorConfiguration relation() {
        SelectByTableGeneratorConfiguration relation = new SelectByTableGeneratorConfiguration();
        relation.setRelationIdKey("org_role_menu");
        relation.setTableName("org_role_menu");
        relation.setMethodSuffix("ForRole");
        relation.setPrimaryKeyColumn("menu_id");
        relation.setOtherPrimaryKeyColumn("role_id");
        relation.setThisColumn(ManagedRelationOfflineRegenerator.column("menu_id", "menuId", "当前ID", false));
        IntrospectedColumn role = ManagedRelationOfflineRegenerator.column("role_id", "roleId", "角色", false);
        relation.setOtherColumn(role);
        relation.setParameterName(role.getJavaProperty());
        EnableCacheConfiguration cache = new EnableCacheConfiguration();
        cache.setCacheNames(List.of("orgRoleMenuRelations#72h"));
        relation.getCacheConfigurationList().add(cache);
        return relation;
    }
}
